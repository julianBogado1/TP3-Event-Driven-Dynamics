import os
import sys
from typing import Callable

import matplotlib.pyplot as plt

import numpy as np

import resources

Fx = Callable[[float], tuple[float, float]]
Study = dict[str, Fx]

LENGTHS: Study = {
    '0.03': lambda p: (0.03, p),
    '0.05': lambda p: (0.05, p),
    '0.07': lambda p: (0.07, p),
    '0.09': lambda p: (0.09, p)
}

AREA: Study = {
    '0.03': lambda p: (0.009999, p),
    '0.05': lambda p: (0.011799, p),
    '0.07': lambda p: (0.013599, p),
    '0.09': lambda p: (0.015399, p)
}

AREANT: Study = {
    '0.03': lambda p: (100.0100010001, p),
    '0.05': lambda p: ( 84.7529451648, p),
    '0.07': lambda p: ( 73.5348187367, p),
    '0.09': lambda p: ( 64.9392817715, p)
}

STUDIES = {
    'length': (LENGTHS, r"$L$ $[m]$", r"$P$ $[N/m^2]$", False),
    'area': (AREA, r"$A$ $[m^2]$", r"$P$ $[N/m^2]$", False),
    'areant': (AREANT, r"$A^{-1}$ $[1/m^2]$", r"$P$ $[N/m^2]$", True)
}

def main(val: Study):
    X:   list[float]       = []
    Y:   list[float]       = []
    ERR: list[np.floating] = []

    for L in os.listdir(resources.path('pressure')):
        F = val.get(L, None)

        if F is None:
            print(f"Skipping L={L} (no study function)")
            continue

        pressures: list[float] = []
        for filename in os.listdir(resources.path(f'pressure', L)):
            with open(resources.path(f'pressure', L, filename), 'r') as file:
                left, _ = [*map(float, file.readline().strip().split())]
                pressures.append(left)

        x, y = F(float(np.mean(pressures)))
        X.append(x)
        Y.append(y)
        ERR.append(np.std(pressures))

    return X, Y, ERR

if __name__ == '__main__':
    if len(sys.argv) < 2:
        print("Usage: pa.py <length|area|areant>")
        sys.exit(1)

    STUDY = STUDIES.get(sys.argv[1].lower())
    if STUDY is None:
        print("Invalid argument. Use one of: length, area, areant")
        sys.exit(1)

    VALUES, LABEL_X, LABEL_Y, FIT = STUDY

    X, Y, ERR = main(VALUES)

    plt.errorbar(X, Y, yerr=ERR, fmt='o') # pyright: ignore[reportUnknownMemberType]

    if FIT:
        M, B = np.polyfit(X, Y, 1)
        plt.plot(X, M * np.array(X) + B, color='red') # pyright: ignore[reportUnknownMemberType]

    plt.xticks(X) # pyright: ignore[reportUnknownMemberType]
    plt.xlabel(LABEL_X) # pyright: ignore[reportUnknownMemberType]

    # plt.yticks(Y) # pyright: ignore[reportUnknownMemberType]
    plt.ylabel(LABEL_Y) # pyright: ignore[reportUnknownMemberType]

    plt.grid(True) # pyright: ignore[reportUnknownMemberType]
    plt.show() # pyright: ignore[reportUnknownMemberType]
