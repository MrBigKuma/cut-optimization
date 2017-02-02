import lpsolve.LpSolve;
import lpsolve.LpSolveException;

import java.util.*;
import java.util.stream.Collectors;

public class ColumnGenerationSolver {
    private final static int MAX_ITER = 100;

    /**
     * Main solver
     *
     * @param orderSets list of object of order length & order's length
     * @param stockLen  raw bar length to cut
     * @return Map of cutting pattern and num of them
     */
    public static Map<List<BarSet>, Integer> solve(final List<BarSet> orderSets, final double stockLen) {
        final int nOrder = orderSets.size();

        // Convert to Lp-solve friendly format: [arrayLen, x1, x2, 3...]
        double[] odLenVec = new double[nOrder + 1]; // order length vector
        double[] odNumVec = new double[nOrder + 1]; // order number vector
        odLenVec[0] = nOrder;
        odNumVec[0] = nOrder;
        for (int i = 0; i < nOrder; i++) {
            final BarSet barSet = orderSets.get(i);
            odLenVec[i + 1] = barSet.len;
            odNumVec[i + 1] = barSet.num;
        }

        double[][] patternMat = genPatternMatrix(odLenVec, odNumVec, stockLen);
        double[] minPatternNums = new double[nOrder];

        int iter;
        for (iter = 0; iter < MAX_ITER; iter++) {
            try {
                // Solve Linear Programming problem
                double[][] lpRst = calLP(patternMat, odNumVec);
                final int solFlag = (int) lpRst[0][0];
                if (solFlag != LpSolve.OPTIMAL) {
                    System.out.println("Can't solve anymore");
                    break;
                }
                minPatternNums = lpRst[1];
                double[] dualCostVector = lpRst[2];

                // Solve Knapsack problem
                double[][] ksRst = calKnapsack(dualCostVector, odLenVec, stockLen);
                double reducedCost = ksRst[0][0];
                double[] newPattern = ksRst[1];

                // TODO: use native optimized variable instead
                if (reducedCost <= 1.000000000001) { // epsilon threshold due to double value error
                    System.out.println("Optimized");
                    break;
                }

                // Cal leaving column
                int lcRst[] = calLeavingColumn(patternMat, newPattern, minPatternNums);
                int lcSolFlag = lcRst[0];
                int leavingColIndex = lcRst[1];
                // System.out.println("Leaving column index: " + leavingColIndex);

                if (lcSolFlag != LpSolve.OPTIMAL) {
                    System.out.println("Can't solve anymore");
                    break;
                }

                // Save new pattern
                for (int r = 0; r < patternMat.length; r++) {
                    patternMat[r][leavingColIndex] = newPattern[r];
                }

            } catch (LpSolveException e) {
                e.printStackTrace();
            }
        }

        if (iter == MAX_ITER) {
            System.out.println("Maximum of iter reached! Solution is not quite optimized");
        }

        //        System.out.println("Optimized pattern: ");
        //        for (int r = 0; r < patternMat.length; r++) {
        //            // TODO: this output is weird
        //            System.out.print(Arrays.toString(patternMat[r]));
        //            System.out.println(": " + minPatternNums[r + 1]);
        //        }

        // ------------ Round up to keep integer part of result ----------------
        final double[] remainOdNumVec = new double[odNumVec.length];
        System.arraycopy(odNumVec, 0, remainOdNumVec, 0, odNumVec.length);
        for (int r = 0; r < patternMat.length; r++) {
            double[] row = patternMat[r];
            for (int c = 1; c < row.length; c++) {
                remainOdNumVec[r + 1] -= row[c] * Math.floor(minPatternNums[c]);
            }
        }
        //        System.out.println("Leftovers: " + Arrays.toString(remainOdNumVec));

        // Optimized for the rest of pattern by BruteForce
        final List<BarSet> remainOrderSets = new ArrayList<>();
        for (int r = 1; r < remainOdNumVec.length; r++) {
            if (remainOdNumVec[r] > 0) {
                remainOrderSets.add(new BarSet(odLenVec[r], (int) remainOdNumVec[r]));
            }
        }

        Pair<Integer, List<List<BarSet>>> remainRst = new Pair<>(0, new ArrayList<>());
        if (remainOrderSets.size() > 0) {
            // Sort DESC
            remainOrderSets.sort((a, b) -> b.len.compareTo(a.len));

            // Solve
            remainRst = BruteForceSolver.solve(remainOrderSets, stockLen);

            // Remove BarSets in patterns that have num = 0
            for (int i = 0; i < remainRst.snd.size(); i++) {
                final List<BarSet> ptrn = remainRst.snd.get(i);
                final List<BarSet> trimPtrn = ptrn.stream().filter(b -> b.num > 0).collect(Collectors.toList());
                remainRst.snd.set(i, trimPtrn);
            }
        }
        //        System.out.println("Leftover patterns:");
        //        for (List<BarSet> bSets : remainRst.snd) {
        //            System.out.println(Arrays.toString(bSets.toArray()));
        //        }

        // Prepare result
        final Map<List<BarSet>, Integer> rstMap = new HashMap<>();
        for (int c = 1; c < nOrder + 1; c++) { // column
            if ((int) minPatternNums[c] == 0) { // if number of this pattern is zero, skip
                continue;
            }

            List<BarSet> pattern = new ArrayList<>();
            for (int r = 0; r < nOrder; r++) { // row
                if (patternMat[r][c] > 0d) {
                    pattern.add(new BarSet(odLenVec[r + 1], (int) patternMat[r][c]));
                }
            }
            rstMap.put(pattern, (int) minPatternNums[c]);
        }
        // Add leftover to major result map
        remainRst.snd.stream().filter(p -> p.size() > 0).forEach(pattern -> rstMap.put(pattern, 1));

        return rstMap;
    }

    /**
     * Generate initial pattern matrix
     */
    private static double[][] genPatternMatrix(double[] odLenVec, double[] odNumVec, double stockLen) {
        final int vecLen = odNumVec.length;
        final double[][] basicMatrix = new double[vecLen - 1][vecLen];
        //        System.out.println("First pattern: ");
        for (int r = 1; r < vecLen; r++) {
            basicMatrix[r - 1] = new double[vecLen];
            basicMatrix[r - 1][r] = Math.floor(stockLen / odLenVec[r]);
            basicMatrix[r - 1][0] = vecLen - 1;

            //            System.out.println(Arrays.toString(basicMatrix[r - 1]));
        }
        return basicMatrix;
    }

    /**
     * @param basicMatrixIn the coefficient matrix of constraints
     * @return Array[2][m+1]
     * First row is just reduced cost
     * Second row is dual cost vector
     */
    private static double[][] calLP(double[][] basicMatrixIn, double[] orderNumVec) throws LpSolveException {
        double[][] basicMatrix = cloneMatrix(basicMatrixIn); // LP solve modifies our input array

        int nVar = basicMatrix.length;
        LpSolve solver = LpSolve.makeLp(0, nVar);
        solver.setVerbose(LpSolve.CRITICAL);

        // add constraints
        for (int r = 0; r < nVar; r++) {
            solver.addConstraint(basicMatrix[r], LpSolve.EQ, orderNumVec[r + 1]);
        }

        // set objective function
        double[] minCoef = new double[nVar + 1]; // coefficients
        Arrays.fill(minCoef, 1);
        minCoef[0] = nVar;
        solver.setObjFn(minCoef);
        solver.setMinim();

        // solve the problem
        final int solFlag = solver.solve();

        // print solution
        //        System.out.println("Value of objective function: " + solver.getObjective());
        double[] rhs = solver.getPtrVariables();
        //        for (int i = 0; i < rhs.length; i++) {
        //            System.out.println("rhs[" + i + "] = " + rhs[i]);
        //        }

        //        System.out.println("Dual cost vector");
        double[] duals = solver.getPtrDualSolution();
        //        for (int i = 1; i <= nVar; i++) {
        //            double d = duals[i];
        //            System.out.println("y[" + i + "] = " + d);
        //        }
        //        solver.printDuals();

        // delete the problem and free memory
        solver.deleteLp();

        // Prepare result
        double[][] rst = new double[3][nVar + 1];
        rst[0][0] = solFlag;
        rst[1][0] = nVar;
        System.arraycopy(rhs, 0, rst[1], 1, nVar);
        rst[2][0] = nVar;
        System.arraycopy(duals, 1, rst[2], 1, nVar);
        return rst;
    }

    /**
     * Solve Knapsack problem
     *
     * @param objArr      objective array
     * @param orderLenVec order len
     * @param stockLen
     * @return 1st: reduced cost, 2nd new pattern to enter
     */
    private static double[][] calKnapsack(double[] objArr, double[] orderLenVec, double stockLen) throws LpSolveException {
        int nVar = objArr.length - 1;
        LpSolve solver = LpSolve.makeLp(0, nVar);
        solver.setVerbose(LpSolve.CRITICAL);

        // add constraints
        solver.addConstraint(orderLenVec, LpSolve.LE, stockLen);
        // objArr.length is equal to nCol + 1
        for (int c = 1; c <= nVar; c++) {
            solver.setInt(c, true);
        }

        // set objective function
        solver.setObjFn(objArr);
        solver.setMaxim();

        // solve the problem
        final int solFlag = solver.solve();

        // print solution
        double reducedCost = solver.getObjective();
        //        System.out.println("Reduced cost: " + reducedCost);
        double[] var = solver.getPtrVariables();
        //        for (int i = 0; i < var.length; i++) {
        //            System.out.println("Pattern [" + i + "] = " + var[i]);
        //        }

        // delete the problem and free memory
        solver.deleteLp();

        // Prepare result
        double[][] rst = new double[2][nVar];
        rst[0] = new double[] { reducedCost };
        rst[1] = var;
        return rst;
    }

    /**
     * Calculate which column index should be replaced
     *
     * @return index of the column that will leave the pattern matrix
     */
    private static int[] calLeavingColumn(double[][] basicMatrixIn, double[] newPattern, double[] rhs) throws LpSolveException {
        double[][] basicMatrix = cloneMatrix(basicMatrixIn); // LP solve modifies our input array

        int nVar = basicMatrix.length;
        LpSolve solver = LpSolve.makeLp(0, nVar);
        solver.setVerbose(LpSolve.CRITICAL);

        // add constraints
        for (int r = 0; r < nVar; r++) {
            solver.addConstraint(basicMatrix[r], LpSolve.EQ, newPattern[r]);
        }

        // set objective function
        double[] minCoef = new double[nVar + 1];
        Arrays.fill(minCoef, 1);
        minCoef[0] = nVar;
        solver.setObjFn(minCoef);
        solver.setMinim();

        // solve the problem
        final int solFlag = solver.solve();

        // print solution
        double[] var = solver.getPtrVariables();
        //        for (int i = 0; i < var.length; i++) {
        //            System.out.println("Value of var[" + i + "] = " + var[i]);
        //        }

        // leaving
        int minIndex = -1;
        double minVal = Double.MAX_VALUE;
        for (int i = 0; i < var.length; i++) {
            if (var[i] > 0 && (rhs[i + 1] / var[i]) < minVal) {
                minIndex = i;
                minVal = rhs[i + 1] / var[i];
            }
        }

        // delete the problem and free memory
        solver.deleteLp();

        return new int[] { solFlag, minIndex + 1 };
    }

    private static double[][] cloneMatrix(double[][] mat) {
        final double[][] clone = new double[mat.length][mat[0].length];
        for (int r = 0; r < mat.length; r++) {
            System.arraycopy(mat[r], 0, clone[r], 0, mat[r].length);
        }
        return clone;
    }
}
