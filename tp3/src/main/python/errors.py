from typing import Callable

import matplotlib.pyplot as plt
import numpy as np

FIT = Callable[[np.ndarray, float], np.ndarray]

def fitter(X: np.ndarray, Y: np.ndarray, F: FIT, LEFT: float, RIGHT: float) -> tuple[np.ndarray, np.ndarray, float, float]:
    C = np.linspace(LEFT, RIGHT, 1000)
    E = np.array([np.sum((Y - F(X, c)) ** 2) for c in C])

    MIN = np.argmin(E)
    MIN_X = C[MIN]
    MIN_Y = E[MIN]

    return C, E, MIN_X, MIN_Y

def plot(C: np.ndarray, E: np.ndarray, MIN_X: float, MIN_Y: float):
    plt.plot(C, E) # pyright: ignore[reportUnknownMemberType]

    plt.axhline(y=MIN_Y, color='black', linestyle='--') # pyright: ignore[reportUnknownMemberType]
    plt.axvline(x=MIN_X, color='black', linestyle='--') # pyright: ignore[reportUnknownMemberType]

    plt.xlabel('c') # pyright: ignore[reportUnknownMemberType]
    plt.ylabel('E(c)') # pyright: ignore[reportUnknownMemberType]

    plt.show() # pyright: ignore[reportUnknownMemberType]

def origin(P1: tuple[float, float], P2: tuple[float, float]):
    M = (P2[1] - P1[1]) / (P2[0] - P1[0])
    print(M)
    return P1[1] - M * P1[0]

def LINEAR(B: float):
    def f(X: np.ndarray, c: float):
        return c * X + B
    return f
