from dataclasses import dataclass

@dataclass(frozen=True, init=False)
class Event:
    id: int
    time: float
    type: str
    a: int
    b: int

    def __init__(self, id: int, data: list[str]):
        object.__setattr__(self, 'id', id)
        object.__setattr__(self, 'time', float(data[0]))
        object.__setattr__(self, 'type', data[1])
        object.__setattr__(self, 'a', int(data[2]))
        object.__setattr__(self, 'b', int(data[3]))
