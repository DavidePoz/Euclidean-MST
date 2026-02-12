# $\alpha$-EuclideanMST

This is a modified implementation of a class assignment for my Data Structures and Algorithms course. 
I have refactored the code and added graphical visualization for the exececution of the algorithm.

The assignement required to implement and test an algorithm to compute the Euclidean
Minimum Spanning Tree (EMST) of a set of points in the plane.

## THE PROBLEM

Let $D = \{ p_1, p_2,..., p_n \}$ be a set of points $p_i = (x_i,y_i)\in \mathbb{R}^2$.
(For convenience, we assume the points have integer coordinates in $[0,n-1]$)

A *spanning tree* for $D$ is a tree connecting the $n$ points of $D$, which can be represented as
a set $E$ of $n-1$ edges, with each edge being a pair of points $(p_i,p_j)$ of $D$.
For each edge $e \in E$, we define its weight $w(e)$ as the euclidean distance between its two points
(hence the name euclidean).

The weight $w(E)$ of the spanning tree $E$ is defined as the sum of the wights of all its edges.

We are tasked to design an algorithm to compute a spanning tree for the set $D$ with **minimum weight**, but
with a little caveat: each edge must have a weight $w(e)\leq \alpha$, where $\alpha >1$ is a real constant.

Note that such EMST may not exist for all values of $\alpha$.

## THE SOLUTION

A non-trivial solution is given by [Prim's algorithm](https://en.wikipedia.org/wiki/Prim%27s_algorithm), and 
this repository contains my implementation of said strategy.

Instead of treating the input as a fully connected graph (which would require $O(n^2)$ edges), 
the implementation uses a Lazy Prim's Algorithm (see below) combined with a 
spatial hashing grid to achieve far better performace on sparse datasets.

### THE HASHING GRID

The core challenge of computing the $\alpha$-EMST is *efficiently* finding **valid neighbors**, 
that is, points within distance $\alpha$ from a given point.

To solve this problem, a solution consists of *dividing* the plane into *square cells* of size $\alpha \times \alpha$:
this way, given a point in a cell, we know that all the valid neighbors must reside either in the same
cell, or in one of the 8 adjacent cells.

To implement this sulotion, I used a map containing entries in which the key is the cell in which the point falls:
points residing in the same cell are hashed into the same bucket.

### LAZY PRIM STRATEGY

The EMST construction follows the *lazy* version of Prim's algorithm using Java's 'PriorityQueue':
1. **Initialization** : The algorithm starts at an arbitrary point and adds all the valid edges connecting it to its 
neighbors to the Priority Queue.
2. **Selection** : Extracts the minimum weight edge from the queue. If both endpoints are already in the EMST, the edge is discarded, otherwise it's added to the EMST.
3. **PQ Update** : For each newly added point, all its *valid* neighbors are added to the PQ.
4. **Termination** : The algorithm stops when $n-1$ edges have been added, or when the PQ is empty (in this case, $\alpha$ is too small and the requested EMST does not exist).

### COMPLEXITY

First of all, we notice that the PQ holds $O(n^2)$ entries: this is a worst-case upper bound which manifests when $\alpha$ is "very close" to $n$, thus making most of the edges valid.

The operations performed inside the main loop are:
- 'poll' : this is a 'removeMin' operation on a heap-based PQ and therefore costs $O(\log n^2)\in O(\log n)$.
- **Scan neighbors** : given the assumption of integer coordinates and the dimensions of a grid cell, this operation costs $O(\alpha ^2)$.

Since this loop runs (at most) as many times as there are entries in the queue, we conclude that the worst-case complexity is $O(n^2 \log n)$.

In reality, for small enough values of $\alpha$ (compared to $n$) and an even distribution of the points on the plane, each point $p_i\in D$ adds $k_i=\rho\times \alpha ^2$ neighbors to the PQ, where $\rho$ is the *density* of the points per unit area.
We can thus safely assume that the priority queue stores $O(n)$ entries, granting us $O(n \log n)$ complexity in this *"average"* case.

> [!NOTE]
> A clever implementation exists and is known as *"eager"* Prim's strategy, but requires an indexed priority queue that supports 'decreaseKey' operations.
> This would require a custom implementation, as Java's 'PriorityQueue' does not support these.

## PROGRAM USAGE

Compile with:
```
javac EMST.java
```

Run with:
```
java EMST.java <path/to/file.txt> <alpha_parameter> [-v]
```

> [!IMPORTANT]
> - *path/to/file.txt* must point to a txt file listing points with the same formats as the provided examples, found in the 'input_examples' directory. This directory also contains a script to generate such files: tweak the values to your likings.
> - The $\alpha$ parameter must be an 'int' or 'double'.
> - The '-v' flag enables the visualization window seen in the screenshot below. If not included, the program will simply print the weight of the computed EMST if this exists, or FAIL otherwise.

![Demonstration example](/media/demonstration.png)
# CREDITS AND AKNOWLEDGEMENTS

> [!IMPORTANT]
> To visualize the execution of the algorithm I have used the 'StdDraw' library.
> This, alongside other libraries, can be found at [princeton.edu](https://introcs.cs.princeton.edu/java/stdlib/).
