import os
import time
import argparse

import numpy as np

from tqdm import tqdm

from matplotlib import pyplot as plt

import frames
import resources
from classes.event import Event
from streaming import SequentialStreamingExecutor as Executor

INTERVAL = 5.0
M = 1.0
R = 1.5e-3
X_MAX = 0.18
Y_MAX = 0.09

CHAMBERS: dict[int, tuple[str, str]] = {
    0: ('left', 'horizontal'),
    1: ('center', 'vertical'),
    2: ('right', 'horizontal'),
    3: ('right', 'vertical'),
    4: ('right', 'horizontal'),
    5: ('center', 'vertical'),
    6: ('left', 'horizontal'),
    7: ('left', 'vertical')
}

def main(cut: int = 60, dry: bool = False):
    with open(resources.path('events.txt'), 'r') as file:
        events = [
            Event(i, line.strip().split(' '))
            for i, line in enumerate(file)
                if line.strip() and line.strip().split(' ')[1] in ['WALL', 'VERTEX']
        ]

    checkpoints: list[int] = []
    previous = 0.0

    for i, e in enumerate(events):
        if e.time > previous + INTERVAL:
            checkpoints.append(i)
            previous = e.time

    executor = Executor(frames.next, [e.id for e in events])

    with open(resources.path("setup.txt")) as f:
        count, L = [*map(float, f.readline().strip().split())]
        count = int(count)

    pressures_izq: list[float] = []
    pressures_der: list[float] = []
    times: list[float] = []

    total_impulse_izq = 0.0
    total_impulse_cen = 0.0
    total_impulse_der = 0.0

    for i, particles in tqdm(enumerate(executor.stream()), total=len(events)):
        if i in checkpoints:
            PL = total_impulse_izq / (INTERVAL * Y_MAX * 3)
            PR = total_impulse_der / (INTERVAL * (Y_MAX * 2 + L))

            pressures_izq.append(PL)
            pressures_der.append(PR)
            times.append(events[i].time)

            total_impulse_izq = 0.0
            total_impulse_cen = 0.0
            total_impulse_der = 0.0

        P = particles[events[i].a - 2]
        # x, y = p.position.tuple()
        vx, vy = P.velocity.tuple()

        if events[i].type != 'WALL':
            total_impulse_cen += 2 * M * P.velocity.norm()
            continue

        WALL_ID = events[i].b
        chamber, orientation = CHAMBERS[WALL_ID]

        J = 2 * M * abs(vx if orientation == 'horizontal' else vy)

        if chamber == 'left':
            total_impulse_izq += J
        elif chamber == 'center':
            total_impulse_cen += J
        elif chamber == 'right':
            total_impulse_der += J

    I0 = next(i for i, t in enumerate(times) if t >= cut)

    prom_izq = np.mean(pressures_izq[I0:])
    prom_der = np.mean(pressures_der[I0:])

    if not dry:
        dir = resources.path("pressure", str(L))
        os.makedirs(dir, exist_ok=True)

        file = resources.path(dir, f"{int(time.time())}.txt")
        with open(file, "w") as f:
            f.write(f"{prom_izq} {prom_der}\n")

    return I0, pressures_izq, prom_izq, pressures_der, prom_der, times

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("-c", type=float, default=60, help="Balance cut time")
    parser.add_argument("-p", action="store_true", help="Show plot")
    parser.add_argument("--dry", action="store_true", help="Don't output results")
    args = parser.parse_args()

    i, pl, al, pr, ar, times = main(args.c, args.dry)

    if args.p:
        plt.axvline(x=args.c, color='black', linestyle='-', label='Estacionario') # pyright: ignore[reportUnknownMemberType]

        plt.plot(times, pl, color='b', label='Recinto izquierdo') # pyright: ignore[reportUnknownMemberType]
        plt.axhline(y=float(al), color='b', linestyle='--', label='Promedio izquierdo') # pyright: ignore[reportUnknownMemberType]

        plt.plot(times, pr, color='r', label='Recinto derecho') # pyright: ignore[reportUnknownMemberType]
        plt.axhline(y=float(ar), color='r', linestyle='--', label='Promedio derecho') # pyright: ignore[reportUnknownMemberType]

        plt.xticks(fontsize=24) # pyright: ignore[reportArgumentType, reportUnknownMemberType]
        plt.yticks(fontsize=24) # pyright: ignore[reportUnknownMemberType]

        plt.xlabel(r"$t$ $(s)$", fontsize=24) # pyright: ignore[reportUnknownMemberType]
        plt.ylabel(r"$P$ $(N/m)$", fontsize=24) # pyright: ignore[reportUnknownMemberType]

        plt.legend() # pyright: ignore[reportUnknownMemberType]

        plt.show() # pyright: ignore[reportUnknownMemberType]
