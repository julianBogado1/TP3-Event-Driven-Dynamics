import sys

import numpy as np

from errors import fitter, origin, plot, FIT, LINEAR
from pa import main as pressure_math, study

def main(F: FIT):
    STUDY = study('areant')

    if STUDY is None:
        sys.exit(1)

    RESULTS = pressure_math(STUDY[0])
    X = np.array(RESULTS[0])
    Y = np.array(RESULTS[1])

    C, E, MIN_X, MIN_Y = fitter(X, Y, F, 0, 0.027)

    plot(C, E, MIN_X, MIN_Y)

if __name__ == "__main__":
    P1 = (float(sys.argv[1]), float(sys.argv[2]))
    P2 = (float(sys.argv[3]), float(sys.argv[4]))

    B = origin(P1, P2)
    print(f'B: {B:.4f}')

    main(LINEAR(B))
