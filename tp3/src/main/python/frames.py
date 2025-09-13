from functools import cache

from classes.particle import Particle
import resources

@cache
def checkpoints():
    with open(resources.path('events.txt'), 'r') as file:
        event_times = [float(line.strip().split(' ')[0]) for line in file if line.strip()]

    checkpoints: list[int] = []
    previous = 0.0

    for i, time in enumerate(event_times):
        if time > previous + 0.015:
            checkpoints.append(i)
            previous = time

    return checkpoints

def next(f: int):
    """
    Reads the input file for a given frame.
    """
    file_path = resources.path('steps', f"{f}.txt")
    with open(file_path, 'r') as file:
        # Iterate through the lines and convert them to Particles
        return f, [Particle(*map(float, line.strip().split())) for line in file]

@cache
def count():
    """
    Returns the number of animations steps.
    """
    return 1 + len(checkpoints()) # initial state
