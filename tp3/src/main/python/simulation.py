import matplotlib.pyplot as plt
import re

def read_data(filename):
    walls = []
    particles = []
    with open(filename, "r") as f:
        lines = f.readlines()

    mode = None
    for line in lines:
        line = line.strip()
        if not line:
            continue

        if line.startswith("Walls:"):
            mode = "walls"
            continue
        elif line.startswith("Particles:"):
            mode = "particles"
            continue

        if mode == "walls":
            # Match two points: (x1, y1), (x2, y2)
            matches = re.findall(r"\(([^,]+), ([^)]+)\)", line)
            if matches and len(matches) == 2:
                (x1, y1), (x2, y2) = matches
                walls.append(((float(x1), float(y1)), (float(x2), float(y2))))

        elif mode == "particles":
            # Match: (x, y), (vx, vy), r
            matches = re.findall(r"\(([^,]+), ([^)]+)\)", line)
            radius_match = re.findall(r", ([0-9.]+)$", line)
            if matches and len(matches) >= 1 and radius_match:
                (x, y) = matches[0]
                r = float(radius_match[0])
                particles.append((float(x), float(y), r))

    return walls, particles


def plot_scene(walls, particles):
    fig, ax = plt.subplots()

    # Draw walls
    for (x1, y1), (x2, y2) in walls:
        ax.plot([x1, x2], [y1, y2], color="black")

    # Draw particles
    for x, y, r in particles:
        circle = plt.Circle((x, y), r, color="blue", alpha=0.6)
        ax.add_patch(circle)

    ax.set_aspect("equal", adjustable="box")
    plt.xlabel("X")
    plt.ylabel("Y")
    plt.title("Particles inside Walls")
    plt.show()


if __name__ == "__main__":
    walls, particles = read_data("../resources/particles.txt")
    plot_scene(walls, particles)
