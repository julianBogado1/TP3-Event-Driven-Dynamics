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
    """Index del primer evento después de 60 s"""
    for i, ev in enumerate(parse_events()):
        if float(ev[0]) > 60:
            return i
    return -1

def compute_quadratic_displacement():
    stationary_idx = get_stationary()
    if stationary_idx == -1:
        raise ValueError("No hay eventos después de 60 s")

    # posiciones iniciales en el estado estacionario
    init_positions = {}
    for i, particle in enumerate(parse_steps(stationary_idx)):
        pid, x, y = i, float(particle[1]), float(particle[2])
        init_positions[pid] = np.array([x, y])

    times = []
    msd_values = []

    # Recorremos los pasos siguientes
    steps_dir = "simulations/steps"
    # asumiendo que los nombres son enteros consecutivos
    step_files = sorted(
        [int(f.split(".")[0]) for f in os.listdir(steps_dir) if f.endswith(".txt")]
    )
    for step in step_files:
        if step < stationary_idx:
            continue
        # tiempo real de este step: usar events.txt para obtener timestamp
        t = float(parse_events()[step][0])
        data = parse_steps(step)
        squared_displacements = []
        for i, particle in enumerate(data):
            pid, x, y = i, float(particle[1]), float(particle[2])
            if pid in init_positions:
                diff = np.array([x, y]) - init_positions[pid]
                squared_displacements.append(diff.dot(diff))
        if squared_displacements:
            msd = np.mean(squared_displacements)
            times.append(t)
            msd_values.append(msd)

    # Graficar
    plt.plot(times, msd_values, marker='o')
    plt.xlabel("Tiempo (s)")
    plt.ylabel("MSD")
    plt.title("Desplazamiento cuadrático medio desde t0=60 s")
    plt.grid(True)
    plt.show()

if __name__ == "__main__":
    compute_quadratic_displacement()
