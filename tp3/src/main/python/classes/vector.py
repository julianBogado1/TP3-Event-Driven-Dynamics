from dataclasses import dataclass

@dataclass(frozen=True)
class Vector:
    x: float
    y: float

    def tuple(self) -> tuple[float, float]:
        return (self.x, self.y)

    def __add__(self, other: 'Vector') -> 'Vector':
        return Vector(self.x + other.x, self.y + other.y)

    def __sub__(self, other: 'Vector') -> 'Vector':
        return Vector(self.x - other.x, self.y - other.y)

    def __mul__(self, other: float) -> 'Vector':
        return Vector(self.x * other, self.y * other)

    def __div__(self, other: float) -> 'Vector':
        return Vector(self.x / other, self.y / other)

    def dot(self, other: 'Vector') -> float:
        return self.x * other.x + self.y * other.y

    def norm(self) -> float:
        return (self.x**2 + self.y**2) ** 0.5

    def __eq__(self, other: object) -> bool:
        if not isinstance(other, Vector):
            return NotImplemented
        return self.x == other.x and self.y == other.y
