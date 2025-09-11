from typing import Callable

import time

import matplotlib.pyplot as plt
from matplotlib.patches import Circle
from matplotlib.animation import FuncAnimation

from tqdm import tqdm

import frames
import resources
from streaming import SequentialStreamingExecutor as Executor

from classes.wall import Wall
from classes.particle import Particle

abar = None
def main():
    global abar

    with open(resources.path("setup.txt")) as f:
        count, _ = [*map(float, f.readline().strip().split())]
        count = int(count)

        walls = [Wall(*map(float, line.strip().split())) for line in f]

    executor = Executor(frames.next, frames.count())

    fig, ax = plt.subplots() # pyright: ignore[reportUnknownMemberType]
    ax.set_aspect('equal', adjustable="box")

    for wall in walls:
        ax.plot([wall.start.x, wall.end.x], [wall.start.y, wall.end.y], color="black") # pyright: ignore[reportUnknownMemberType]

    circles: list[Circle] = []
    for _ in range(count):
        c = Circle((0, 0), radius=0, color="blue")
        ax.add_patch(c)
        circles.append(c)

    def update(particles: list[Particle]):
        global abar

        if abar is not None and abar.n % abar.total == 0:
            abar.reset()

        for i, particle in enumerate(particles):
            circles[i].center = particle.position.tuple()
            circles[i].radius = particle.radius

        if abar is not None:
            abar.update()

        return circles

    ani = FuncAnimation( # pyright: ignore[reportUnusedVariable]
        fig,
        update,
        frames=executor.stream(),
        save_count=frames.count(),
        interval=5,
        blit=True,
        repeat=True
    )

    if False:
        abar = tqdm(total=frames.count())
        plt.show() # pyright: ignore[reportUnknownMemberType]
        abar.close()

    if True:        
        print("Saving animation...")

        filename = resources.path(f"{int(time.time())}.mp4")
        with tqdm(total=frames.count()) as sbar:
            callback: Callable[[int, int], bool | None] = lambda _i, _n: sbar.update()
            ani.save(filename, writer='ffmpeg', fps=60, dpi=300, progress_callback=callback)

        print(f"Animation saved at {filename}")

if __name__ == "__main__":
    main()
