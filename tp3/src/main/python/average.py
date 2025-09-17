import os
import sys
import time

import numpy as np

from tqdm import tqdm

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

def main():
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
            P_izq = (total_impulse_izq / (INTERVAL * Y_MAX)) + (total_impulse_cen / INTERVAL * (Y_MAX - L))
            P_der = total_impulse_der / (INTERVAL * Y_MAX)

            pressures_izq.append(P_izq)
            pressures_der.append(P_der)
            times.append(events[i].time)

            total_impulse_izq = 0.0
            total_impulse_cen = 0.0
            total_impulse_der = 0.0

        p = particles[events[i].a - 2]
        # x, y = p.position.tuple()
        vx, vy = p.velocity.tuple()

        if events[i].type != 'WALL':
            total_impulse_cen += 2 * M * p.velocity.norm()
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

    cut = int(sys.argv[1]) if len(sys.argv) > 1 else 60
    I0 = next(i for i, t in enumerate(times) if t >= cut)

    prom_izq = np.mean(pressures_izq[I0:])
    prom_der = np.mean(pressures_der[I0:])

    dir = resources.path("pressure", str(L))
    os.makedirs(dir, exist_ok=True)

    file = resources.path(dir, f"{int(time.time())}.txt")
    with open(file, "w") as f:
        f.write(f"{prom_izq} {prom_der}\n")

    return pressures_izq, pressures_der, times

if __name__ == "__main__":
    pressures_izq, pressures_der, times = main()

    # We could plot here
