# cut-optimization
Solving alu bar cut as Cutting Stock Problem (CSP) with **Lpsolve**

## Problem
Cut aluminum bars in stock (_stock_) with fixed length `L` to `N` type of bar with length `l{i}` with `m{i}` each type.
Assume that the saw width is `W`.
Luckily, `N` is usually <= 9.

## Solution Steps
### Normalization
This is similar to Cutting Stock Problem (CSP) except the fact that we have to consider the saw width `W`.
Whenever we cut a bar, _stock_ loses `l{i} + W` unit of length.
E.g. If we cut 2 times, _stock_ loses `l1 + W + l2 + W + lo` with `lo` is leftover.
If `lo` could be used to cut `l3` if `lo >= l3`.

However, we can normalize this problem to CSP.
By letting `l'{i} = l{i} + W` and `L' = L + W`, this becomes CSP for `L',N, l'{i}, m'{i}`
To make it simple. I call `L` as `L'`, and `l'{i}` as `l{i}` from now on.

### Solve CSP
CSP is NP hard problem so the optimal algorithm is O(2^n). However, we could use **Column Generation** (CG) as approximate algorithm to solve the problem. So the solution is not guaranteed to be optimal but we usually get optimal solution.
CG will be used to calculate the optimal pattern, in real number.

**E.g.** solution we get from CG is:
```
L = 9.0 cm, (l*m){i}= (3.5*18) + (2.5*22) + (2.0*33)
Pattern P1 (3.5 3.5 2.0) x9.3 bars (Cut 3.5 cm twice, 2.0 cm once)
Pattern P2 (2.5 2.5 2.0 2.0) x11.1 bars (Cut 2.5 cm twice, 2.0 cm twice)
```
Then, we just use the integer value of solution (e.g. P1x9 and P2x11). And solve the leftover, the small number of orders, by recusive solution.
It's kind of brute force strategy that check all possibilities that we can cut _stock_ to leftover bars.

### Column Generation (solve main pattern)
In CG, there are 2 sub-problems: **Linear Programming** problem and **Knapsack** problem.

#### Step 1: Solve Linear Programming problem
**Input:** a specific pattern set P{i}.

**Output:** minimum of each pattern p{i,j} is required fo pattern set P{i}; _dual cost vector_; whether our solution is optimal.

We use **LpSolve**. In iteration 0, we keep P{0} simple as `N` patterns with each pattern p{0, j} has maximum number of l{i}

**E.g.** for above problem
```
l{i} = [3.5, 2.5, 2.0] 
p{0, 1} | p{0, 2} | p{0,3}
--- | --- | ---
2 | 0 | 0
0 | 3 | 0
0 | 0 | 4
```
Pattern p{0, 1} can cut maximum 2 bar-3.5
Pattern p{0, 2} can cut maximum 3 bar-2.5
Pattern p{0, 3} can cut maximum 3 bar-2.0

#### Step 2: Solve Knapsack problem
**Input:** _dual cost vector_.

**Ouput:** new pattern p{i+1, 0}.

We use **LpSolve**.

#### Step 3: Generate new pattern an repeat
After having new pattern, we calculate by **Lpsolve** to see which pattern in old pattern set should be replaced with new pattern.
Repeat until Linear Programming problem gets optimal solution OR _maximum number of iteration_ reach.

Solution is the last output of Linear Programming problem.

## Setup
- **Java JDK 1.8**
- **Install lpsolve** (for Mac)
  - Go to [Download page](https://sourceforge.net/p/lpsolve/activity/?page=0&limit=100#57e6d7482718461131b353f9)
  - Download `lp_solve_5.5.2.5_src`, compile script is `lp_solve_5.5/lpsolve55/ccc`. Compile and get the dynamic library.
  - Download java wrapper `lp_solve_5.5_java`, compile script is `lp_solve_5.5_java/lib/mac/build-osx`. Copy compiled file in previous step here. Change `LPSOLVE_DIR` & `JDK_DIR` in script. Run script and get `lpsolve55j.jnilib`
  - Copy `lpsolve55j.jnilib` to `/Library/Java/Extensions/`
  - Use `lpsolve55j.jar` in `lp_solve_5.5_java` as library in our program
- Install with mvn command
