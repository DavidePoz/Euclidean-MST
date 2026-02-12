import random
import math

def generate_nice_points(filename, count, max_coord):
    points = set()

    # CONFIGURATION
    num_clusters = 20         
    points_per_cluster = 180 
    num_lines = 1            
    points_per_line = 150    
    noise_points = count - (num_clusters * points_per_cluster) - (num_lines * points_per_line)

    # GENERATE CLUSTERS 
    for _ in range(num_clusters):
        # Pick a center for the cluster
        center_x = random.randint(0, max_coord)
        center_y = random.randint(0, max_coord)
        # Pick cluster spread
        std_dev = random.randint(100, 500) 
        
        for _ in range(points_per_cluster):
            # generates points concentrated around the center
            x = int(random.gauss(center_x, std_dev))
            y = int(random.gauss(center_y, std_dev))
            
            # Keep within bounds
            x = max(0, min(max_coord, x))
            y = max(0, min(max_coord, y))
            points.add((x, y))

    # GENERATES LINES 
    for _ in range(num_lines):
        # Start and End points of the line
        x1, y1 = random.randint(0, max_coord), random.randint(0, max_coord)
        x2, y2 = random.randint(0, max_coord), random.randint(0, max_coord)
        
        for i in range(points_per_line):
            # Interpolate along the line and scatter the points around it
            t = i / points_per_line 
            jitter = random.randint(-50, 50) 
            
            x = int(x1 + (x2 - x1) * t) + jitter
            y = int(y1 + (y2 - y1) * t) + jitter
            
            x = max(0, min(max_coord, x))
            y = max(0, min(max_coord, y))
            points.add((x, y))

    # FILLS REMAINDER WITH NOISE (Uniform)
    while len(points) < count:
        x = random.randint(0, max_coord)
        y = random.randint(0, max_coord)
        points.add((x, y))

    # WRITES TO FILE
    with open(filename, 'w') as f:
        for p in list(points):
            f.write(f"({p[0]},{p[1]})\n")
    
    print(f"Points generated.")

generate_nice_points("input_n5000.txt", 5000, 5000)
