from dataclasses import dataclass

from classes.vector import Vector

@dataclass(frozen=True, init=False)
class Particle:
    position: Vector
    velocity: Vector
    radius: float

    def __init__(self, x: float, y: float, vx: float, vy: float):
        object.__setattr__(self, 'position', Vector(x, y))
        object.__setattr__(self, 'velocity', Vector(vx, vy))
        object.__setattr__(self, 'radius', 0.0015)
