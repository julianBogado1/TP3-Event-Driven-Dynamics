from classes.particle import Particle
from classes.event import Event
from classes.wall import Wall
import math

Collideable = Particle | Wall

# Fake
def setup_simulation(L: float) -> tuple[list[Particle], list[Wall]]:
    return [], []

# Fake
def collision_time(p1: Particle, p2: Collideable) -> Event:
    return Event(0, [])

# Fake
def p_collision(a: Particle, b: Particle):
    return (a.velocity, b.velocity)

def w_collision(a: Particle, b: Wall):
    return a.velocity

# Fake
def write_output(_: str):
    return

Invalid = Event(0, [])

class PriorityQueue(list[Event]):
    pass

L = 0
steps = 0
particles: list[Particle]
collideables: list[Collideable]
queue: PriorityQueue

def compute_collision_time(p: Particle):
    for c in collideables:
        event = collision_time(p, c)
        if event.time < math.inf:
            queue.append(event)

def set_initial_collision_time():
    for p in particles:
        for c in collideables:
            event = collision_time(p, c)
            if event.time < math.inf:
                queue.append(event)

def advance_particles(time: float):
    for p in particles:
        p.position = p.position + p.velocity * time

def collide(p: Particle, c: Collideable):
    if isinstance(c, Wall):
        p.velocity = w_collision(p, c)
    else:
        p.velocity, c.velocity = p_collision(p, c)

def run_simulation(steps: int, L: float):
    setup_simulation(L)

    queue = PriorityQueue()
    set_initial_collision_time()

    for _ in range(steps):
        event: Event | None = None
        while not event or event == Invalid:
            event = queue.pop()

        advance_particles(event.time)
        collide(event.a, event.b)

        compute_collision_time(event.a)
        if isinstance(event.b, Particle):
            compute_collision_time(event.b)

def main():
    run_simulation(steps, L)
    write_output('output.txt')
