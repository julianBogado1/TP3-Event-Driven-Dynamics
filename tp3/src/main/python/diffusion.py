import os
import numpy as np
import matplotlib.pyplot as plt

def parse_events():
    with open("simulations/events.txt") as f:
        return [line.strip().split() for line in f]

def parse_steps(step):
    with open(f"simulations/steps/{step}.txt") as f:
        return [line.strip().split() for line in f]

def get_stationary():
    for i, event in enumerate(parse_events()):
        if float(event[0]) > 60:
            return i
    return -1

def compute_quadratic_displacement(step_skip=50):
    stationary_idx = get_stationary()
    if stationary_idx == -1:
        raise ValueError("No hay eventos después de 60 s")

    init_positions = {}
    for i, p in enumerate(parse_steps(stationary_idx)):
        pid, x, y = i, float(p[1]), float(p[2])
        init_positions[pid] = np.array([x, y])

    steps_dir = "simulations/steps"
    step_files = sorted(
        [int(f.split(".")[0]) for f in os.listdir(steps_dir) if f.endswith(".txt")]
    )
    events = parse_events()

    times, msd_values = [], []
    for idx in range(stationary_idx, len(step_files), step_skip):
        step = step_files[idx]
        t = float(events[step][0])
        data = parse_steps(step)
        sq_disp = []
        for i, particle in enumerate(data):
            pid, x, y = i, float(particle[1]), float(particle[2])
            if pid in init_positions:
                diff = np.array([x, y]) - init_positions[pid]
                sq_disp.append(diff.dot(diff))
        if sq_disp:
            times.append(t)
            msd_values.append(np.mean(sq_disp))
    return np.array(times), np.array(msd_values)

def bin_and_average(times, msd, bin_size=5):
    """Agrupa datos en bins de tamaño bin_size en tiempo"""
    t_min, t_max = times.min(), times.max()
    bins = np.arange(t_min, t_max + bin_size, bin_size)
    bin_centers, mean_msd, std_msd = [], [], []
    for i in range(len(bins)-1):
        mask = (times >= bins[i]) & (times < bins[i+1])
        if np.any(mask):
            bin_centers.append(times[mask].mean())
            mean_msd.append(msd[mask].mean())
            std_msd.append(msd[mask].std())
    return np.array(bin_centers), np.array(mean_msd), np.array(std_msd)

def linear_fit(x, y):
    """
    Ajuste lineal por mínimos cuadrados (sin usar np.polyfit).
    x, y: arrays 1D numéricos (floats). Devuelve (slope, intercept).
    """
    x = np.asarray(x, dtype=float)
    y = np.asarray(y, dtype=float)

    n = x.size
    if n == 0:
        raise ValueError("No hay puntos para ajustar.")
    if n == 1:
        raise ValueError("Se necesita al menos 2 puntos para ajustar una recta.")

    sum_x = x.sum()
    sum_y = y.sum()
    sum_x2 = (x * x).sum()
    sum_xy = (x * y).sum()

    denom = n * sum_x2 - sum_x * sum_x
    if np.isclose(denom, 0.0):
        # x's tienen varianza (prácticamente) cero: no se puede determinar pendiente
        raise ValueError("Denominador cero en ajuste lineal (x constantes).")

    slope = (n * sum_xy - sum_x * sum_y) / denom
    intercept = (sum_y - slope * sum_x) / n

    return slope, intercept

def plot_binned(times_b, mean_msd, std_msd, slope, intercept):
    plt.errorbar(times_b, mean_msd, yerr=std_msd,
                 fmt='o', capsize=4, label='Promedio por bin')
    # unir los promedios con una línea
    plt.plot(times_b, mean_msd, '-', color='C0')
    # recta del ajuste lineal
    plt.plot(times_b, slope*times_b + intercept, 'r-', label='Ajuste lineal')
    plt.xlabel("Tiempo (s)")
    plt.ylabel("MSD")
    plt.title("MSD binned desde t0=60 s")
    plt.legend()
    plt.grid(True)
    # plt.show()
    plt.savefig("diffusion_msd.png")

def plot_error_vs_a(x, y):
    x = np.asarray(x, float)
    y = np.asarray(y, float)

    # calculate the optimal slope analytically
    a_opt = np.sum(x * y) / np.sum(x ** 2)

    # explore a range around that value
    # a_vals = np.linspace(a_opt * 0.1, a_opt * 10, 400)
    a_vals = np.linspace(a_opt - (a_opt/2), a_opt + (a_opt/2), 400)
    errors = [np.sum((y - a * x) ** 2) for a in a_vals]

    plt.figure()
    plt.plot(a_vals, errors)
    plt.axvline(a_opt, color='r', linestyle='--', label=f'a óptimo = {a_opt:.3e}')
    plt.axhline(np.min(errors), color='g', linestyle='--', label=f'E(a óptimo) = {np.min(errors):.3e}')
    plt.xlabel("a (pendiente)")
    plt.ylabel("E(a)")
    plt.title("Error del ajuste lineal E(a) = Σ [y - a x]^2")
    plt.legend()
    plt.grid(True)
    # plt.show()
    plt.savefig("diffusion_error_vs_a.png")

if __name__ == "__main__":
    times, msd = compute_quadratic_displacement(step_skip=50)
    t_b, mean_msd, std_msd = bin_and_average(times, msd, bin_size=5)
    slope, intercept = linear_fit(t_b, mean_msd)
    print(f"Pendiente: {slope}, Intercepto: {intercept}")
    plot_binned(t_b, mean_msd, std_msd, slope, intercept)
    plot_error_vs_a(t_b, mean_msd)
