from dataclasses import dataclass

from classes.vector import Vector

@dataclass(frozen=True, init=False)
class Wall:
    start: Vector
    end: Vector

    def __init__(self, x1: float, y1: float, x2: float, y2: float):
        object.__setattr__(self, 'start', Vector(x1, y1))
        object.__setattr__(self, 'end', Vector(x2, y2))
