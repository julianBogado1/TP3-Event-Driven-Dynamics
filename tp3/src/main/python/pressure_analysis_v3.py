#!/usr/bin/env python3
"""
Impulse-Based Pressure Evolution Analysis

Calculates pressure using actual momentum transfer from wall collisions.
For each wall collision event, extracts particle velocity from corresponding
step file and calculates real impulse = 2 * |v_normal| * mass.

Format of events.txt:
time collision_type particle_id target_id

Example:
0,123456789 WALL 42 3      # Particle 42 hits wall 3 at time 0.123456789
0,234567890 PARTICLE 15 87 # Particle 15 hits particle 87 at time 0.234567890

Physics:
- Elastic wall collision: impulse = 2 * |v_normal| * mass
- Pressure = impulse_rate / wall_length (N/m in 2D)
- Event line N corresponds to step file N.txt
"""

import numpy as np
import matplotlib.pyplot as plt
from dataclasses import dataclass
from tqdm import tqdm
import resources
import os
import json
import sys
from datetime import datetime
from typing import Optional, Dict, List, Tuple

MAX_FILES = 2 ** 31 - 1; 

MAGIC_NUMBER = 0.09
RIGHT_CHAMBER_HEIGHT = 0.05  # meters
LEFT_CHAMBER_WALL_IDS = [0, 6, 7]  # Exclude connector walls 2 and 4 to avoid bias
RIGHT_CHAMBER_WALL_IDS = [2, 3, 4]               # Exclude connector walls 1 and 45 to avoid bias
TIME_PER_BIN = 5.0  # seconds

@dataclass
class Vector:
    x: float
    y: float

    def __str__(self):
        return f"({self.x:.6f}, {self.y:.6f})"

@dataclass
class WallCollisionEvent:
    time: float
    particle_id: int
    wall_id: int
    velocity: Vector
    step_index: int
    impulse: float = 0.0


@dataclass 
class PressureData:
    time: list[float]
    left_pressure: list[float]
    right_pressure: list[float]


class ImpulsePressureAnalyzer:
    """Impulse-based pressure analysis using actual momentum transfer from wall collisions"""
    
    def __init__(self, time_bin_size: float = 1):
        self.time_bin_size = time_bin_size  # Fixed time bins in seconds
        self.particle_mass = 1.0  # kg
        
        # Wall chamber mapping (from Wall.java generate method)
        self.wall_chambers = {
            0: 'left',    # Bottom wall (left)
            1: 'left',    # Right wall (left, bottom)
            2: 'right',   # Bottom connector (right chamber)
            3: 'right',   # Right wall (right)
            4: 'right',   # Top connector (right chamber)
            5: 'left',    # Right wall (left, top)
            6: 'left',    # Top wall (left)
            7: 'left'     # Left wall
        }

        # Wall orientation mapping
        self.wall_orientations = {
            0: 'horizontal',  # Bottom wall  (left)
            1: 'vertical',    # Connector    (both/none)
            2: 'horizontal',  # Bottom wall  (right)
            3: 'vertical',    # Right wall   (right)
            4: 'horizontal',  # Top wall     (right)
            5: 'vertical',    # Connector    (both/none)
            6: 'horizontal',  # Top wall     (left)
            7: 'vertical'     # Left wall    (left)
        }

        # Individual wall lengths (from Simulation.java generateBox method)
        self.wall_lengths = {
            0: MAGIC_NUMBER,                             # Bottom wall  (left)
            1: (MAGIC_NUMBER - RIGHT_CHAMBER_HEIGHT)/2,  # Connector    (both/none)
            2: MAGIC_NUMBER,                             # Bottom wall  (right)
            3: RIGHT_CHAMBER_HEIGHT,                     # Right wall   (right)
            4: MAGIC_NUMBER,                             # Top wall     (right)
            5: (MAGIC_NUMBER - RIGHT_CHAMBER_HEIGHT)/2,  # Connector    (both/none)
            6: MAGIC_NUMBER,                             # Top wall     (left)
            7: MAGIC_NUMBER                              # Left wall    (left)
        }

        # Cache for loaded step files to avoid re-reading
        self.step_cache: Dict[int, List[Tuple[int, Vector]]] = {}
    
    def parse_float(self, value_str: str) -> float:
        """Parse float with comma as decimal separator"""
        return float(value_str.replace(',', '.'))

    def get_particle_velocity(self, step_file_path: str, particle_id: int) -> Optional[Vector]:
        """Extract particle velocity from step file"""
        try:
            with open(step_file_path, 'r') as f:
                for line in f:
                    parts = line.strip().split()
                    if len(parts) >= 5:
                        # Format: x y vx vy radius
                        vx = self.parse_float(parts[2])
                        vy = self.parse_float(parts[3])
                        return Vector(vx, vy)
                    # If we only have 4 parts, assume it's a different format
                    # We need to identify particles by line number (particle_id)
        except FileNotFoundError:
            print(f"Warning: Step file {step_file_path} not found")
            return None
        except Exception as e:
            print(f"Error reading step file {step_file_path}: {e}")
            return None

        return None

    def load_step_file_particles(self, step_index: int) -> Dict[int, Vector]:
        """Load all particles from a step file, mapping particle index to velocity"""
        if step_index in self.step_cache:
            return {pid: vel for pid, vel in self.step_cache[step_index]}

        step_file = resources.path(f"steps/{step_index}.txt")
        particles = {}

        try:
            with open(step_file, 'r') as f:
                for particle_idx, line in enumerate(f):
                    parts = line.strip().split()
                    if len(parts) >= 5:
                        # Format: x y vx vy radius
                        vx = self.parse_float(parts[2])
                        vy = self.parse_float(parts[3])
                        # Particle IDs start from 1, but file lines start from 0
                        particles[particle_idx + 1] = Vector(vx, vy)

            # Cache the loaded data
            self.step_cache[step_index] = list(particles.items())

        except FileNotFoundError:
            print(f"Warning: Step file {step_file} not found")
        except Exception as e:
            print(f"Error reading step file {step_file}: {e}")

        return particles

    def calculate_wall_impulse(self, velocity: Vector, wall_id: int) -> float:
        """Calculate impulse = 2 * |v_normal| * mass for wall collision"""

        orientation = self.wall_orientations[wall_id]

        # Extract normal velocity component
        if orientation == 'horizontal':
            v_normal = abs(velocity.y)  # vy for horizontal walls
        else:  # vertical
            v_normal = abs(velocity.x)  # vx for vertical walls

        # impulse = 2 * |v_normal| * mass (elastic collision)
        impulse = 2.0 * v_normal * self.particle_mass

        return impulse
    
    def load_wall_collisions_with_velocity(self, max_events: Optional[int] = None) -> List[WallCollisionEvent]:
        """Load wall collisions and corresponding particle velocities"""

        print("Loading wall collisions with velocity data...")
        wall_collisions: List[WallCollisionEvent] = []

        with open(resources.path("events.txt")) as f:
            for line_idx, line in enumerate(f):
                if max_events and len(wall_collisions) >= max_events:
                    break

                parts = line.strip().split()

                if len(parts) >= 4 and parts[1] == "WALL":
                    try:
                        time = self.parse_float(parts[0])
                        particle_id = int(parts[2])
                        wall_id = int(parts[3])

                        # Load corresponding step file to get particle velocity
                        step_particles = self.load_step_file_particles(line_idx)

                        if particle_id in step_particles:
                            velocity = step_particles[particle_id]

                            # Calculate impulse for this collision
                            impulse = self.calculate_wall_impulse(velocity, wall_id)

                            wall_collisions.append(WallCollisionEvent(
                                time=time,
                                particle_id=particle_id,
                                wall_id=wall_id,
                                velocity=velocity,
                                step_index=line_idx,
                                impulse=impulse
                            ))
                        else:
                            print(f"Warning: Particle {particle_id} not found in step {line_idx}")

                    except ValueError as e:
                        print(f"Error parsing wall collision at line {line_idx + 1}: {line.strip()}")
                        print(f"Error: {e}")
                        continue
                    except Exception as e:
                        print(f"Error processing wall collision at line {line_idx + 1}: {e}")
                        continue

        print(f"Loaded {len(wall_collisions)} wall collision events with impulse data")
        return wall_collisions
    
    def analyze_collision_distribution(self, wall_collisions: List[WallCollisionEvent]):
        """Analyze the distribution of collision types and wall hits"""
        
        if not wall_collisions:
            return

        # Analyze wall collision distribution and impulses
        wall_counts: Dict[int, int] = {}
        wall_impulses: Dict[int, float] = {}

        for collision in wall_collisions:
            wall_id = collision.wall_id
            wall_counts[wall_id] = wall_counts.get(wall_id, 0) + 1
            wall_impulses[wall_id] = wall_impulses.get(wall_id, 0.0) + collision.impulse
        
        print(f"\\nWall Collision Analysis:")
        print(f"Total wall collisions: {len(wall_collisions)}")

        print(f"\\nWall Collision Distribution:")
        left_total_collisions = 0
        right_total_collisions = 0
        left_total_impulse = 0.0
        right_total_impulse = 0.0

        for wall_id in sorted(wall_counts.keys()):
            count = wall_counts[wall_id]
            total_impulse = wall_impulses[wall_id]
            avg_impulse = total_impulse / count if count > 0 else 0.0
            chamber = self.wall_chambers.get(wall_id, 'unknown')

            print(f"Wall {wall_id} ({chamber}): {count} collisions, "
                  f"total impulse: {total_impulse:.6f} kg⋅m/s, "
                  f"avg impulse: {avg_impulse:.6f} kg⋅m/s")

            if chamber == 'left':
                left_total_collisions += count
                left_total_impulse += total_impulse
            elif chamber == 'right':
                right_total_collisions += count
                right_total_impulse += total_impulse

        print(f"\\nChamber Totals:")
        print(f"Left chamber: {left_total_collisions} collisions, {left_total_impulse:.6f} kg⋅m/s total impulse")
        print(f"Right chamber: {right_total_collisions} collisions, {right_total_impulse:.6f} kg⋅m/s total impulse")

        if right_total_collisions > 0:
            print(f"Collision ratio (Left/Right): {left_total_collisions/right_total_collisions:.3f}")
        if right_total_impulse > 0:
            print(f"Impulse ratio (Left/Right): {left_total_impulse/right_total_impulse:.3f}")

    def bin_impulses_by_time_and_wall(self, wall_collisions: List[WallCollisionEvent]) -> Tuple[List[float], Dict[int, np.ndarray]]:
        """Bin wall collisions by time and wall, calculating impulse per wall per bin"""

        if not wall_collisions:
            return [], {}

        max_time = max(collision.time for collision in wall_collisions)
        n_bins = int(np.ceil(max_time / self.time_bin_size))
        time_centers = [(i + 0.5) * self.time_bin_size for i in range(n_bins)]

        # Initialize bins for each wall
        wall_impulse_bins = {}
        for wall_id in range(8):  # 8 walls total
            wall_impulse_bins[wall_id] = np.zeros(n_bins)

        print(f"\\nTime Binning by Wall:")
        print(f"Max simulation time: {max_time:.6f} s")
        print(f"Time bin size: {self.time_bin_size:.6f} s")
        print(f"Number of bins: {n_bins}")

        processed = 0
        for collision in tqdm(wall_collisions, desc="Binning wall collisions by time and wall"):
            bin_idx = int(collision.time / self.time_bin_size)
            if bin_idx >= n_bins:
                continue

            wall_impulse_bins[collision.wall_id][bin_idx] += collision.impulse
            processed += 1

        print(f"Processed {processed} wall collisions into time-wall bins")
        return time_centers, wall_impulse_bins

    def calculate_pressure_per_wall(self, wall_impulse_bins: Dict[int, np.ndarray]) -> Dict[int, np.ndarray]:
        """Calculate pressure per wall: P = Impulse / (Δt × wall_length)"""

        wall_pressures = {}
        for wall_id, impulse_bins in wall_impulse_bins.items():
            wall_length = self.wall_lengths[wall_id]
            # Pressure = Force/Length = (Impulse/Δt)/Length
            wall_pressures[wall_id] = impulse_bins / (self.time_bin_size * wall_length)

        return wall_pressures  # Units: N/m = kg⋅m/s² / m = kg/(s²⋅m)

    def calculate_chamber_pressures(self, wall_pressures: Dict[int, np.ndarray]) -> Tuple[np.ndarray, np.ndarray]:
        """Calculate average chamber pressures from individual wall pressures"""

        n_bins = len(wall_pressures[0])
        left_pressure = np.zeros(n_bins)
        right_pressure = np.zeros(n_bins)

        # Collect walls by chamber (excluding connector walls 2 and 4 to avoid bias)
        left_walls = LEFT_CHAMBER_WALL_IDS  # Pure left chamber walls
        right_walls = RIGHT_CHAMBER_WALL_IDS            # Pure right chamber walls (exclude connectors 2, 4)

        # Calculate average pressure for left chamber
        for bin_idx in range(n_bins):
            left_pressures_in_bin = [wall_pressures[wall_id][bin_idx] for wall_id in left_walls
                                   if wall_pressures[wall_id][bin_idx] > 0]  # Only non-zero pressures
            if left_pressures_in_bin:
                left_pressure[bin_idx] = np.mean(left_pressures_in_bin)

        # Calculate average pressure for right chamber
        for bin_idx in range(n_bins):
            right_pressures_in_bin = [wall_pressures[wall_id][bin_idx] for wall_id in right_walls
                                    if wall_pressures[wall_id][bin_idx] > 0]  # Only non-zero pressures
            if right_pressures_in_bin:
                right_pressure[bin_idx] = np.mean(right_pressures_in_bin)

        return left_pressure, right_pressure

    def calculate_pressure_evolution(self, max_events: Optional[int] = None) -> PressureData:
        """Calculate pressure evolution using impulse-based analysis"""

        # Load wall collisions with velocity data
        wall_collisions = self.load_wall_collisions_with_velocity(max_events)
        if not wall_collisions:
            return PressureData([], [], [])

        # Analyze collision distribution
        self.analyze_collision_distribution(wall_collisions)

        # Bin impulses by time and wall
        time_centers, wall_impulse_bins = self.bin_impulses_by_time_and_wall(wall_collisions)

        if len(time_centers) == 0:
            print("Error: No time bins generated!")
            return PressureData([], [], [])

        # Calculate pressure per wall
        wall_pressures = self.calculate_pressure_per_wall(wall_impulse_bins)

        # Calculate average chamber pressures
        left_pressure, right_pressure = self.calculate_chamber_pressures(wall_pressures)

        # Print binning statistics
        non_zero_left = sum(1 for p in left_pressure if p > 0)
        non_zero_right = sum(1 for p in right_pressure if p > 0)
        n_bins = len(time_centers)

        print(f"\nPressure Calculation Results:")
        print(f"Bins with left chamber activity: {non_zero_left}/{n_bins} ({non_zero_left/n_bins*100:.1f}%)")
        print(f"Bins with right chamber activity: {non_zero_right}/{n_bins} ({non_zero_right/n_bins*100:.1f}%)")
        print(f"Average left pressure: {np.mean(left_pressure):.6e} N/m")
        print(f"Average right pressure: {np.mean(right_pressure):.6e} N/m")

        return PressureData(time_centers, left_pressure.tolist(), right_pressure.tolist())

    def export_simulation_data(self, pressure_data: PressureData, L: float, particle_radius: float,
                              particle_count: int, export_path: str):
        """Export simulation data to JSON for ideal gas law analysis"""

        if not pressure_data.time:
            print("No pressure data to export!")
            return

        # Calculate steady-state statistics (last 20%)
        stationary_start_idx = int(len(pressure_data.time) * 0.8)
        stationary_left = pressure_data.left_pressure[stationary_start_idx:]
        stationary_right = pressure_data.right_pressure[stationary_start_idx:]

        # Filter out zero values and calculate averages
        left_nonzero = [p for p in stationary_left if p > 0]
        right_nonzero = [p for p in stationary_right if p > 0]

        avg_left = np.mean(left_nonzero) if left_nonzero else 0.0
        avg_right = np.mean(right_nonzero) if right_nonzero else 0.0

        # Calculate effective areas (accounting for particle radius)
        effective_width = MAGIC_NUMBER - particle_radius
        left_chamber_area = effective_width ** 2
        right_chamber_area = effective_width * (L - particle_radius)
        total_effective_area = left_chamber_area + right_chamber_area

        # Calculate area-weighted total pressure
        if total_effective_area > 0:
            area_weighted_pressure = (avg_left * left_chamber_area + avg_right * right_chamber_area) / total_effective_area
        else:
            area_weighted_pressure = (avg_left + avg_right) / 2

        # Calculate collision statistics
        total_wall_collisions = 0
        left_collisions = 0
        right_collisions = 0
        total_impulse_left = 0.0
        total_impulse_right = 0.0

        # Count collisions by chamber (approximate from pressure data)
        # This is a simplified calculation - for exact counts would need collision events

        # Export data structure
        export_data = {
            "simulation_metadata": {
                "timestamp": datetime.now().isoformat(),
                "L": L,
                "particle_radius": particle_radius,
                "particle_count": particle_count,
                "total_simulation_time": max(pressure_data.time) if pressure_data.time else 0.0,
                "time_bin_size": self.time_bin_size,
                "magic_number": MAGIC_NUMBER
            },
            "geometry": {
                "left_chamber_area": left_chamber_area,
                "right_chamber_area": right_chamber_area,
                "total_effective_area": total_effective_area,
                "left_chamber_dimensions": f"{effective_width:.6f} x {effective_width:.6f}",
                "right_chamber_dimensions": f"{effective_width:.6f} x {L - particle_radius:.6f}"
            },
            "pressure_analysis": {
                "stationary_period_start": pressure_data.time[stationary_start_idx] if stationary_start_idx < len(pressure_data.time) else 0.0,
                "stationary_period_duration": max(pressure_data.time) - (pressure_data.time[stationary_start_idx] if stationary_start_idx < len(pressure_data.time) else 0.0),
                "left_chamber_pressure": avg_left,
                "right_chamber_pressure": avg_right,
                "area_weighted_total_pressure": area_weighted_pressure,
                "pressure_ratio_left_right": avg_left / avg_right if avg_right > 0 else float('inf'),
                "pressure_units": "N/m"
            },
            "temporal_data": {
                "total_time_bins": len(pressure_data.time),
                "bins_with_left_activity": sum(1 for p in pressure_data.left_pressure if p > 0),
                "bins_with_right_activity": sum(1 for p in pressure_data.right_pressure if p > 0),
                "overall_avg_left_pressure": np.mean(pressure_data.left_pressure),
                "overall_avg_right_pressure": np.mean(pressure_data.right_pressure)
            },
            "ideal_gas_analysis": {
                "PA_product": area_weighted_pressure * total_effective_area,
                "inverse_area": 1.0 / total_effective_area if total_effective_area > 0 else 0.0
            }
        }

        # Save to JSON
        with open(export_path, 'w') as f:
            json.dump(export_data, f, indent=2)

        print(f"\n=== Simulation Data Exported ===")
        print(f"Export path: {export_path}")
        print(f"L = {L:.6f} m")
        print(f"Total effective area: {total_effective_area:.6f} m²")
        print(f"Area-weighted pressure: {area_weighted_pressure:.6e} N/m")
        print(f"P·A product: {area_weighted_pressure * total_effective_area:.6e} N")
        print(f"Steady-state period: {export_data['pressure_analysis']['stationary_period_duration']:.3f} s")


def plot_pressure_evolution(pressure_data: PressureData, save_path: str | None = None):
    """Create pressure evolution plots"""
    
    if not pressure_data.time:
        print("No data to plot!")
        return

    _fig, ((ax1, ax2), (ax3, ax4)) = plt.subplots(2, 2, figsize=(15, 12)) # pyright: ignore[reportUnknownMemberType]

    # Individual chamber pressures
    ax1.plot(pressure_data.time, pressure_data.left_pressure, 'b-', label='Left Chamber', alpha=0.7)
    ax1.set_ylabel('Pressure (N/m)')
    ax1.set_title('Left Chamber Pressure Evolution')
    ax1.grid(True, alpha=0.3)
    ax1.legend()
    
    ax2.plot(pressure_data.time, pressure_data.right_pressure, 'r-', label='Right Chamber', alpha=0.7)
    ax2.set_ylabel('Pressure (N/m)')
    ax2.set_title('Right Chamber Pressure Evolution')
    ax2.grid(True, alpha=0.3)
    ax2.legend()
    
    # Comparison
    ax3.plot(pressure_data.time, pressure_data.left_pressure, 'b-', label='Left Chamber', alpha=0.7)
    ax3.plot(pressure_data.time, pressure_data.right_pressure, 'r-', label='Right Chamber', alpha=0.7)
    ax3.set_xlabel('Time (s)')
    ax3.set_ylabel('Pressure (N/m)')
    ax3.set_title('Pressure Comparison Between Chambers')
    ax3.grid(True, alpha=0.3)
    ax3.legend()

    # Stationary state pressure comparison (last 20% of simulation)
    stationary_start_idx = int(len(pressure_data.time) * 0.8)  # Last 20%
    stationary_left = pressure_data.left_pressure[stationary_start_idx:]
    stationary_right = pressure_data.right_pressure[stationary_start_idx:]

    avg_left_stationary = np.mean(stationary_left) if stationary_left else 0
    avg_right_stationary = np.mean(stationary_right) if stationary_right else 0

    chambers = ['Left Chamber', 'Right Chamber']
    pressures = [avg_left_stationary, avg_right_stationary]
    colors = ['blue', 'red']

    bars = ax4.bar(chambers, pressures, color=colors, alpha=0.7, edgecolor='black', linewidth=1)
    ax4.set_ylabel('Average Pressure (N/m)')
    ax4.set_title('Stationary State Pressure Comparison\\n(Last 20% of Simulation)')
    ax4.grid(True, alpha=0.3, axis='y')

    # Add value labels on bars
    for bar, pressure in zip(bars, pressures):
        height = bar.get_height()
        ax4.text(bar.get_x() + bar.get_width()/2., height + height*0.01,
                f'{pressure:.3e}', ha='center', va='bottom', fontsize=10)

    # Add pressure ratio annotation
    if avg_right_stationary > 0:
        ratio = avg_left_stationary / avg_right_stationary
        ax4.text(0.5, max(pressures) * 0.8, f'Pressure Ratio (L/R): {ratio:.3f}',
                ha='center', va='center', transform=ax4.transData,
                bbox=dict(boxstyle='round,pad=0.3', facecolor='yellow', alpha=0.7))

    plt.tight_layout()
    
    if save_path:
        plt.savefig(save_path, dpi=300, bbox_inches='tight') # pyright: ignore[reportUnknownMemberType]
        print(f"Plot saved to {save_path}")

    plt.show() # pyright: ignore[reportUnknownMemberType]


def main():
    """Main analysis function"""

    # Check for command line arguments
    export_json = False
    json_filename = None

    if len(sys.argv) > 1:
        if "--export-json" in sys.argv:
            export_json = True
            json_idx = sys.argv.index("--export-json")
            if json_idx + 1 < len(sys.argv):
                json_filename = sys.argv[json_idx + 1]
            else:
                json_filename = "simulation_data.json"

    print("=== Impulse-Based Pressure Evolution Analysis ===")
    print("Using actual momentum transfer from wall collisions")

    analyzer = ImpulsePressureAnalyzer(time_bin_size=TIME_PER_BIN)

    try:
        # Calculate pressure evolution
        pressure_data = analyzer.calculate_pressure_evolution(max_events=MAX_FILES)

        if not pressure_data.time:
            print("No pressure data generated. Check simulation output files.")
            return

        # Read simulation parameters from setup.txt
        L = None
        particle_count = None
        try:
            with open(resources.path("setup.txt")) as f:
                first_line = f.readline().strip().split()
                particle_count = int(first_line[0])
                L = float(first_line[1].replace(',', '.'))
        except Exception as e:
            print(f"Warning: Could not read simulation parameters from setup.txt: {e}")
            L = RIGHT_CHAMBER_HEIGHT  # Fallback to default
            particle_count = 300

        # Export JSON if requested
        if export_json:
            particle_radius = 0.0015  # From Main.java
            export_path = resources.path(json_filename)
            analyzer.export_simulation_data(pressure_data, L, particle_radius, particle_count, export_path)

        # Create plots (always)
        plot_pressure_evolution(pressure_data, save_path=resources.path("pressure_evolution_v3.png"))

        # Print summary statistics
        if pressure_data.left_pressure and pressure_data.right_pressure:
            avg_left = np.mean(pressure_data.left_pressure)
            avg_right = np.mean(pressure_data.right_pressure)
            
            # Calculate stationary state averages (last 20%)
            stationary_start_idx = int(len(pressure_data.time) * 0.8)
            stationary_left = pressure_data.left_pressure[stationary_start_idx:]
            stationary_right = pressure_data.right_pressure[stationary_start_idx:]

            avg_left_stationary = np.mean(stationary_left) if stationary_left else 0
            avg_right_stationary = np.mean(stationary_right) if stationary_right else 0

            print(f"\\n=== Summary Statistics ===")
            print(f"Overall Average Left Chamber Pressure: {avg_left:.6e} N/m")
            print(f"Overall Average Right Chamber Pressure: {avg_right:.6e} N/m")

            if avg_right > 0:
                print(f"Overall Pressure Ratio (Left/Right): {avg_left/avg_right:.3f}")

            print(f"\\n=== Stationary State Statistics (Last 20%) ===")
            print(f"Stationary Left Chamber Pressure: {avg_left_stationary:.6e} N/m")
            print(f"Stationary Right Chamber Pressure: {avg_right_stationary:.6e} N/m")

            if avg_right_stationary > 0:
                print(f"Stationary Pressure Ratio (Left/Right): {avg_left_stationary/avg_right_stationary:.3f}")

            print(f"Total simulation time: {max(pressure_data.time):.6f} s")
            print(f"Stationary analysis period: {pressure_data.time[stationary_start_idx]:.6f} - {max(pressure_data.time):.6f} s")
        
    except FileNotFoundError as e:
        print(f"Error: Required simulation files not found.")
        print(f"Please run the Java simulation first to generate events.txt")
    except Exception as e:
        print(f"Error during analysis: {e}")
        import traceback
        traceback.print_exc()


if __name__ == "__main__":
    main()