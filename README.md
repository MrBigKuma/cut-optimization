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
Input: a specific pattern set P{i}.

Output: minimum of each pattern p{i,j} is required fo pattern set P{i}; _dual cost vector_; whether our solution is optimal.

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
Input: _dual cost vector_.

Output: new pattern p{i+1, 0}.


#### Step 3: Generate new pattern an repeat
After having new pattern, we calculate by **Lpsolve** to see which pattern in old pattern set should be replaced with new pattern.
Repeat until Linear Programming problem gets optimal solution OR _maximum number of iteration_ reach.

Solution is the last output of Linear Programming problem.

## Setup
Requirement: 
- Java JDK 1.8**
- Maven

### Install lpsolve with Java wrapper (for Mac)

Download and expand lp_solve_5.5_source.tar.gz into a directory named 'lp_solve_5.5'.
Download and expand lp_solve_5.5_java.zip into a directory named 'lp_solve_5.5_java'.
[Download page](https://sourceforge.net/p/lpsolve/activity/?page=0&limit=100#57e6d7482718461131b353f9)

**1) Build the lp_solve library.**
```bash
$ cd lp_solve_5.5/lpsolve55
$ sh ccc.osx
```
This creates two new files in the lpsolve55 subdirectory:
```bash
$ ls liblp*
liblpsolve55.a     liblpsolve55.dylib
```

**2) Install the lp_solve library.**

If not already in directory lpsolve55, cd to it.  Copy liblpsolve55.a
and
liblpsolve55.dylib to /usr/local/lib (you may need to create
/usr/local/lib first: sudo mkdir -p /usr/local/lib):
```bash
$ sudo cp liblpsolve55.a liblpsolve55.dylib /usr/local/lib
```

**3) Test the build and installation (optional).**
it may take several seconds to build 'demo':
The example problems in the demo should issue no errors.

```bash
$ cd lp_solve_5.5/demo
$ sh ccc
$ ./demo
```

**4) Build the lp_solve JNI extension.**
```bash
$ cd lp_solve_5.5_java/lib/mac
```

The script "build-osx" in the mac subdirectory is a DOS text file:

Edit build-osx1 to set `LPSOLVE_DIR` to the location of directory lp_solve_5.5 on your computer.
`JDK_DIR` to your jdk (_e.g. /Library/Java/JavaVirtualMachines/jdk1.8.0_101.jdk/Contents/Home_)
Add to `INCL="-I $JDK_DIR/include -I $JDK_DIR/include/darwin ..."`
Change `-llpsolve55` in last line to `liblpsolve55.dylib`
```bash
$ sh build-osx1
```

This creates the extension library liblpsolve55j.jnilib.

**5) Install  the lp_solve JNI extension.**

The file liblpsolve55j.jnilib should be copied to /Library/Java/
Extensions
if you want it to be available to all users.  We assume this will be a
private installation (create the Java extensions directory if necessary;
e.g., mkdir -p ~/Library/Java/Extensions):
```bash
$ cp liblpsolve55j.jnilib ~/Library/Java/Extensions
```

### Install lpsolve for C#
Check [Calling the lpsolve API from your application](http://web.mit.edu/lpsolve/doc/Build.htm)

## Reference

- [Sample lpsolve usage](http://web.mit.edu/lpsolve/doc/formulate.htm) for languages: Java, C#... 
- [lpsolve doc](http://web.mit.edu/lpsolve/doc/) lists all lpsolve document
