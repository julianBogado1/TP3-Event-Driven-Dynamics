import os
import sys

import matplotlib.pyplot as plt

import numpy as np

import resources

LENGTHS = {
    '0.03': 0.03,
    '0.05': 0.05,
    '0.07': 0.07,
    '0.09': 0.09
}

AREA = {
    '0.03': 0.009999,
    '0.05': 0.011799,
    '0.07': 0.013599,
    '0.09': 0.015399
}

AREANT = {
    '0.03': 100.0100010001,
    '0.05':  84.7529451648,
    '0.07':  73.5348187367,
    '0.09':  64.9392817715
}

STUDIES = {
    'length': (LENGTHS, r"$L$ $[m]$", False),
    'area': (AREA, r"$A$ $[m^2]$", False),
    'areant': (AREANT, r"$A^{-1}$ $[1/m^2]$", True)
}

def main(val: dict[str, float]):
    X:   list[float]       = []
    Y:   list[np.floating] = []
    ERR: list[np.floating] = []

    for L in os.listdir(resources.path('pressure')):
        INV = val.get(L, 0)

        if INV == 0:
            print(f"Skipping L={L} (no area data)")
            continue

        pressures: list[float] = []
        for filename in os.listdir(resources.path(f'pressure', L)):
            with open(resources.path(f'pressure', L, filename), 'r') as file:
                left, _ = [*map(float, file.readline().strip().split())]
                pressures.append(left)

        X.append(INV)
        Y.append(np.mean(pressures))
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

    VALUES, LABEL, FIT = STUDY

    X, Y, ERR = main(VALUES)

    plt.errorbar(X, Y, yerr=ERR, fmt='o') # pyright: ignore[reportUnknownMemberType]

    if FIT:
        M, B = np.polyfit(X, Y, 1)
        plt.plot(X, M * np.array(X) + B, color='red') # pyright: ignore[reportUnknownMemberType]

    plt.xticks(X) # pyright: ignore[reportUnknownMemberType]
    plt.xlabel(LABEL) # pyright: ignore[reportUnknownMemberType]

    # plt.yticks(Y) # pyright: ignore[reportUnknownMemberType]
    plt.ylabel(r"$P$ $[N/m^2]$") # pyright: ignore[reportUnknownMemberType]

    plt.grid(True) # pyright: ignore[reportUnknownMemberType]
    plt.show() # pyright: ignore[reportUnknownMemberType]
