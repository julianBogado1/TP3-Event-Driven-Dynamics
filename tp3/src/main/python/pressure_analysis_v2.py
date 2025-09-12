#!/usr/bin/env python3
"""
Simplified Pressure Evolution Analysis using Explicit Collision Data

Now that events.txt contains explicit collision type information, we can directly
process wall collisions without any inference or guessing.

Format of events.txt:
time collision_type particle_id target_id

Example:
0,123456789 WALL 42 3      # Particle 42 hits wall 3 at time 0.123456789
0,234567890 PARTICLE 15 87 # Particle 15 hits particle 87 at time 0.234567890
"""

import numpy as np
import matplotlib.pyplot as plt
from typing import List, Dict
from dataclasses import dataclass
from tqdm import tqdm
import resources

MAX_FILES = 2 ** 31 - 1; 

@dataclass
class CollisionEvent:
    time: float
    collision_type: str  # "PARTICLE" or "WALL"
    particle_id: int
    target_id: int  # wall ID for wall collisions, particle ID for particle collisions


@dataclass 
class PressureData:
    time: List[float]
    left_pressure: List[float]
    right_pressure: List[float]


class SimplifiedPressureAnalyzer:
    """Direct pressure analysis using explicit collision event data"""
    
    def __init__(self, events_per_bin: int = 200):
        self.events_per_bin = events_per_bin
        self.time_bin_size = None  # Will be calculated adaptively
        
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
        
        # Chamber perimeters for pressure normalization
        # Left: 0.09×0.09 square minus 0.07 opening = 2*(0.09+0.09) - 0.07
        # Right: 0.09×0.07 rectangle = 2*(0.09+0.07)
        self.left_perimeter = 2 * (0.09 + 0.09) - 0.07  # 0.29m
        self.right_perimeter = 2 * (0.09 + 0.07)        # 0.32m
    
    def parse_float(self, value_str: str) -> float:
        """Parse float with comma as decimal separator"""
        return float(value_str.replace(',', '.'))
    
    def load_collision_events(self, max_events: int = None) -> List[CollisionEvent]:
        """Load collision events from events.txt"""
        
        print("Loading collision events...")
        events = []
        
        with open(resources.path("events.txt")) as f:
            for line_num, line in enumerate(f, 1):
                parts = line.strip().split()
                
                if len(parts) >= 4:
                    try:
                        time = self.parse_float(parts[0])
                        collision_type = parts[1]
                        particle_id = int(parts[2])
                        target_id = int(parts[3])
                        
                        events.append(CollisionEvent(time, collision_type, particle_id, target_id))
                        
                        if max_events and len(events) >= max_events:
                            break
                            
                    except ValueError as e:
                        print(f"Error parsing line {line_num}: {line.strip()}")
                        print(f"Error: {e}")
                        continue
                else:
                    if line_num <= 5:  # Show first few parsing issues
                        print(f"Skipping malformed line {line_num}: {line.strip()}")
        
        print(f"Loaded {len(events)} collision events")
        return events
    
    def analyze_collision_distribution(self, events: List[CollisionEvent]):
        """Analyze the distribution of collision types and wall hits"""
        
        wall_events = [e for e in events if e.collision_type == "WALL"]
        particle_events = [e for e in events if e.collision_type == "PARTICLE"]
        
        print(f"\\nCollision Distribution:")
        print(f"Wall collisions: {len(wall_events)} ({len(wall_events)/len(events)*100:.1f}%)")
        print(f"Particle collisions: {len(particle_events)} ({len(particle_events)/len(events)*100:.1f}%)")
        
        # Analyze wall collision distribution
        if wall_events:
            wall_counts = {}
            for event in wall_events:
                wall_id = event.target_id
                wall_counts[wall_id] = wall_counts.get(wall_id, 0) + 1
            
            print(f"\\nWall Collision Distribution:")
            left_total = 0
            right_total = 0
            
            for wall_id, count in sorted(wall_counts.items()):
                chamber = self.wall_chambers.get(wall_id, 'unknown')
                print(f"Wall {wall_id} ({chamber}): {count} collisions")
                
                if chamber == 'left':
                    left_total += count
                elif chamber == 'right':
                    right_total += count
            
            print(f"\\nChamber Totals:")
            print(f"Left chamber: {left_total} wall collisions")
            print(f"Right chamber: {right_total} wall collisions")
            print(f"Left/Right ratio: {left_total/right_total:.3f}" if right_total > 0 else "Left/Right ratio: inf")
    
    def calculate_pressure_evolution(self, max_events: int = 2 ** 31 - 1) -> PressureData:
        """Calculate pressure evolution using direct collision event data"""
        
        # Load events
        events = self.load_collision_events(max_events)
        if not events:
            return PressureData([], [], [])
        
        # Analyze collision distribution
        self.analyze_collision_distribution(events)
        
        # Filter wall collisions
        wall_events = [e for e in events if e.collision_type == "WALL"]
        
        if not wall_events:
            print("Error: No wall collisions found!")
            return PressureData([], [], [])
        
        # Calculate adaptive time bin size based on events per bin
        max_time = max(e.time for e in events)
        total_wall_events = len(wall_events)
        
        # Calculate bin size to get approximately events_per_bin wall collisions per bin
        if total_wall_events > 0:
            n_bins = max(1, int(total_wall_events / self.events_per_bin))
            self.time_bin_size = max_time / n_bins
        else:
            n_bins = 1
            self.time_bin_size = max_time
        
        time_centers = [(i + 0.5) * self.time_bin_size for i in range(n_bins)]
        left_impulse_bins = [0.0] * n_bins
        right_impulse_bins = [0.0] * n_bins
        
        print(f"\\nAdaptive Binning:")
        print(f"Total wall events: {total_wall_events}")
        print(f"Target events per bin: {self.events_per_bin}")
        print(f"Number of bins: {n_bins}")
        print(f"Time bin size: {self.time_bin_size:.6f} s")
        print(f"Expected events per bin: {total_wall_events/n_bins:.1f}")
        
        print(f"\\nProcessing {len(wall_events)} wall collisions into {n_bins} time bins...")
        
        # Process wall collisions
        processed = 0
        for event in tqdm(wall_events, desc="Processing wall collisions"):
            time_bin = int(event.time / self.time_bin_size)
            
            if time_bin >= n_bins:
                continue
            
            # Get chamber from wall ID
            wall_id = event.target_id
            chamber = self.wall_chambers.get(wall_id, 'unknown')
            
            if chamber == 'unknown':
                continue
            
            # Use unit impulse per collision (simplified model)
            # In reality, this should be calculated from velocity changes
            impulse = 1.0
            
            # Assign to appropriate chamber
            if chamber == 'left':
                left_impulse_bins[time_bin] += impulse
            elif chamber == 'right':
                right_impulse_bins[time_bin] += impulse
            
            processed += 1
        
        print(f"Processed {processed} wall collisions")
        
        # Convert to pressure: impulse / (time_interval * perimeter)
        # Pressure units: collisions per second per meter of wall
        left_pressure = [impulse / (self.time_bin_size * self.left_perimeter) 
                        for impulse in left_impulse_bins]
        right_pressure = [impulse / (self.time_bin_size * self.right_perimeter)
                         for impulse in right_impulse_bins]
        
        # Print binning statistics
        non_zero_left = sum(1 for p in left_pressure if p > 0)
        non_zero_right = sum(1 for p in right_pressure if p > 0)
        print(f"\\nBinning Results:")
        print(f"Bins with left chamber activity: {non_zero_left}/{n_bins} ({non_zero_left/n_bins*100:.1f}%)")
        print(f"Bins with right chamber activity: {non_zero_right}/{n_bins} ({non_zero_right/n_bins*100:.1f}%)")
        
        return PressureData(time_centers, left_pressure, right_pressure)


def plot_pressure_evolution(pressure_data: PressureData, save_path: str = None):
    """Create pressure evolution plots"""
    
    if not pressure_data.time:
        print("No data to plot!")
        return
    
    fig, (ax1, ax2, ax3) = plt.subplots(3, 1, figsize=(12, 10))
    
    # Individual chamber pressures
    ax1.plot(pressure_data.time, pressure_data.left_pressure, 'b-', label='Left Chamber', alpha=0.7)
    ax1.set_ylabel('Pressure (collisions/s/m)')
    ax1.set_title('Left Chamber Pressure Evolution')
    ax1.grid(True, alpha=0.3)
    ax1.legend()
    
    ax2.plot(pressure_data.time, pressure_data.right_pressure, 'r-', label='Right Chamber', alpha=0.7)
    ax2.set_ylabel('Pressure (collisions/s/m)')
    ax2.set_title('Right Chamber Pressure Evolution')
    ax2.grid(True, alpha=0.3)
    ax2.legend()
    
    # Comparison
    ax3.plot(pressure_data.time, pressure_data.left_pressure, 'b-', label='Left Chamber', alpha=0.7)
    ax3.plot(pressure_data.time, pressure_data.right_pressure, 'r-', label='Right Chamber', alpha=0.7)
    ax3.set_xlabel('Time (s)')
    ax3.set_ylabel('Pressure (collisions/s/m)')
    ax3.set_title('Pressure Comparison Between Chambers')
    ax3.grid(True, alpha=0.3)
    ax3.legend()
    
    plt.tight_layout()
    
    if save_path:
        plt.savefig(save_path, dpi=300, bbox_inches='tight')
        print(f"Plot saved to {save_path}")
    
    plt.show()


def main():
    """Main analysis function"""
    print("=== Simplified Pressure Evolution Analysis ===")
    print("Using explicit collision event data from events.txt")
    
    analyzer = SimplifiedPressureAnalyzer(events_per_bin=250)  # Adaptive binning
    
    try:
        # Calculate pressure evolution
        pressure_data = analyzer.calculate_pressure_evolution(max_events=MAX_FILES)
        
        if not pressure_data.time:
            print("No pressure data generated. Check simulation output files.")
            return
        
        # Create plots
        plot_pressure_evolution(pressure_data, save_path=resources.path("pressure_evolution_v2.png"))
        
        # Print summary statistics
        if pressure_data.left_pressure and pressure_data.right_pressure:
            avg_left = np.mean(pressure_data.left_pressure)
            avg_right = np.mean(pressure_data.right_pressure)
            
            print(f"\\n=== Summary Statistics ===")
            print(f"Average Left Chamber Pressure: {avg_left:.6e} collisions/s/m")
            print(f"Average Right Chamber Pressure: {avg_right:.6e} collisions/s/m")
            
            if avg_right > 0:
                print(f"Pressure Ratio (Left/Right): {avg_left/avg_right:.3f}")
            
            print(f"Total simulation time: {max(pressure_data.time):.6f} s")
        
    except FileNotFoundError as e:
        print(f"Error: Required simulation files not found.")
        print(f"Please run the Java simulation first to generate events.txt")
    except Exception as e:
        print(f"Error during analysis: {e}")
        import traceback
        traceback.print_exc()


if __name__ == "__main__":
    main()