from dataclasses import dataclass

@dataclass(frozen=True)
class Vector:
    x: float
    y: float

    def tuple(self) -> tuple[float, float]:
        return (self.x, self.y)
