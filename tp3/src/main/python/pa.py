import os

import matplotlib.pyplot as plt

import numpy as np

import resources

AREANT = {
    '0.03': 100.0100010001,
    '0.05':  84.7529451648,
    '0.07':  73.5348187367,
    '0.09':  64.9392817715
}

def main():
    X:   list[float]       = []
    Y:   list[np.floating] = []
    ERR: list[np.floating] = []

    for L in os.listdir(resources.path('pressure')):
        INV = AREANT.get(L, 0)

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
    X, Y, ERR = main()

    plt.errorbar(X, Y, yerr=ERR, fmt='o') # pyright: ignore[reportUnknownMemberType]

    plt.xticks(X) # pyright: ignore[reportUnknownMemberType]
    plt.xlabel(r"$A^{-1}$ $[1/m^2]$") # pyright: ignore[reportUnknownMemberType]

    # plt.yticks(Y) # pyright: ignore[reportUnknownMemberType]
    plt.ylabel(r"$P$ $[N/m^2]$") # pyright: ignore[reportUnknownMemberType]

    plt.grid(True) # pyright: ignore[reportUnknownMemberType]
    plt.show() # pyright: ignore[reportUnknownMemberType]
