import com.sun.tools.javac.util.Pair;
import lpsolve.LpSolve;
import lpsolve.LpSolveException;

import java.util.*;
import java.util.stream.Collectors;

public class ColumnGenerationSolver {
    final static int MAX_ITER = 1000;

    /**
     * This func is just for testing
     */
    public static void main(String[] args) {
        // TODO: this problem failed
        //        double[] prices = new double[] { 6, 1656, 2158, 1458, 546, 734, 646 };
        //        double[] orders = new double[] { 6, 83, 1065, 565, 556, 565, 556 };
        final float rawBarHeight = 6000;
        final List<BarSet> requiredBars = new ArrayList<BarSet>() {{
            add(new BarSet(1656, 8));
            add(new BarSet(2158, 10));
            add(new BarSet(1458, 5));
        }};
        final Map<List<BarSet>, Integer> rstMap = solve(requiredBars, rawBarHeight);

        rstMap.keySet().forEach(pattern -> {
            System.out.print(pattern.stream().map(BarSet::toString).collect(Collectors.joining(", ")));
            System.out.println(": x" + rstMap.get(pattern));
        });
    }

    /**
     * Main solver
     * @param orderSets list of object of order length & order's length
     * @param stockLength raw bar length to cut
     * @return Map of cutting pattern and num of them
     */
    public static Map<List<BarSet>, Integer> solve(final List<BarSet> orderSets, final float stockLength) {
        final int nOrderType = orderSets.size();

        double[] prices = new double[nOrderType + 1];
        double[] orders = new double[nOrderType + 1];
        prices[0] = nOrderType;
        orders[0] = nOrderType;
        for (int i = 0; i < nOrderType; i++) {
            final BarSet barSet = orderSets.get(i);
            prices[i + 1] = barSet.len;
            orders[i + 1] = barSet.num;
        }

        double[][] basicMatrix = genBasicMatrix(prices, orders, stockLength);
        double[] curMinSol = new double[nOrderType];

        int iter;
        for (iter = 0; iter < MAX_ITER; iter++) {
            try {
                // Master problem
                double[][] lpRst = calLP(basicMatrix, orders);
                curMinSol = lpRst[0];
                double[] dualCostVector = lpRst[1];

                // Slave problem
                double[][] ksRst = calKnapsack(dualCostVector, prices, stockLength);
                double reducedCost = ksRst[0][0];
                double[] newPattern = ksRst[1];

                // TODO: use native optimized variable
                if (reducedCost <= 1) {
                    System.out.println("Optimized");
                    break;
                }

                // Cal leaving column
                int leavingColIndex = calLeavingColumn(basicMatrix, newPattern, curMinSol);
                System.out.println("Leaving column index: " + leavingColIndex);
                // Save new pattern
                for (int r = 0; r < basicMatrix.length; r++) {
                    basicMatrix[r][leavingColIndex] = newPattern[r];
                }

            } catch (LpSolveException e) {
                e.printStackTrace();
            }
        }

        if (iter == MAX_ITER) {
            System.out.println("Maximum of iter reached! Solution is not quite optimized");
        }

        System.out.println("Optimized pattern: ");
        for (int r = 0; r < basicMatrix.length; r++) {
            // TODO: this output is weird
            System.out.print(Arrays.toString(basicMatrix[r]));
            System.out.println(": " + curMinSol[r + 1]);
        }

        // -----Round up and optimized for the rest of pattern-----
        double[] orderLeftovers = new double[orders.length];
        System.arraycopy(orders, 0, orderLeftovers, 0, orders.length);
        for (int r = 0; r < basicMatrix.length; r++) {
            double[] row = basicMatrix[r];
            for (int c = 1; c < row.length; c++) {
                orderLeftovers[r + 1] -= row[c] * (int) curMinSol[c];
            }
        }
        System.out.println("Leftovers: " + Arrays.toString(orderLeftovers));

        List<BarSet> barSets = new ArrayList<>();
        for (int r = 1; r < orderLeftovers.length; r++) {
            if (orderLeftovers[r] != 0f) {
                BarSet barSet = new BarSet((float) prices[r], (int) orderLeftovers[r]);
                barSets.add(barSet);
            }
        }
        Pair<Integer, List<List<BarSet>>> leftoverRst = BruteForceSolver.optimizeAllBar(barSets, (float) stockLength);
        System.out.println("Leftover patterns:");
        for (List<BarSet> barsets : leftoverRst.snd) {
            System.out.println(Arrays.toString(barsets.toArray()));
        }

        // Prepare result
        final Map<List<BarSet>, Integer> rstMap = new HashMap<>();
        for (int c = 1; c < nOrderType + 1; c++) {
            List<BarSet> pattern = new ArrayList<>();
            for (int r = 0; r < nOrderType; r++) {
                if (basicMatrix[r][c] > 0f) {
                    pattern.add(new BarSet((float) prices[r + 1], (int) basicMatrix[r][c]));
                }
            }
            rstMap.put(pattern, (int) curMinSol[c]);
        }
        // Add leftover to major result map
        leftoverRst.snd.stream().filter(p -> p.size() > 0).forEach(pattern -> rstMap.put(pattern, 1));

        return rstMap;
    }

    private static double[][] genBasicMatrix(double[] prices, double[] orders, double stockLength) {
        double[][] basicMatrix = new double[orders.length - 1][orders.length];
        System.out.println("First pattern: ");
        for (int r = 1; r < orders.length; r++) {
            basicMatrix[r - 1] = new double[orders.length];
            basicMatrix[r - 1][r] = Math.floor(stockLength / prices[r]);
            basicMatrix[r - 1][0] = orders.length - 1;

            System.out.println(Arrays.toString(basicMatrix[r - 1]));

        }
        return basicMatrix;
    }

    private static double[][] calLP(double[][] basicMatrix, double[] orders) throws LpSolveException {
        int nVar = basicMatrix.length;
        // Create a problem with 4 variables and 0 constraints
        LpSolve solver = LpSolve.makeLp(0, nVar);
        solver.setVerbose(LpSolve.CRITICAL);

        // add constraints
        for (int r = 0; r < nVar; r++) {
            solver.addConstraint(basicMatrix[r], LpSolve.EQ, orders[r + 1]);
        }

        // set objective function
        double[] minCoef = new double[nVar + 1];
        Arrays.fill(minCoef, 1);
        minCoef[0] = nVar;
        solver.setObjFn(minCoef);
        solver.setMinim();

        // solve the problem
        solver.solve();

        // print solution
        System.out.println("Value of objective function: " + solver.getObjective());
        double[] rhs = solver.getPtrVariables();
        for (int i = 0; i < rhs.length; i++) {
            System.out.println("rhs[" + i + "] = " + rhs[i]);
        }

        System.out.println("Dual cost vector");
        double[] duals = solver.getPtrDualSolution();
        for (int i = 1; i <= nVar; i++) {
            double d = duals[i];
            System.out.println("y[" + i + "] = " + d);
        }
        solver.printDuals();

        // delete the problem and free memory
        solver.deleteLp();

        double[][] rst = new double[2][nVar + 1];
        System.arraycopy(rhs, 0, rst[0], 1, nVar);
        System.arraycopy(duals, 1, rst[1], 1, nVar);
        rst[0][0] = nVar;
        rst[1][0] = nVar;
        return rst;
    }

    private static double[][] calKnapsack(double[] objArr, double[] prices, double stockLength) throws LpSolveException {
        int nVar = objArr.length - 1;

        // Create a problem with 4 variables and 0 constraints
        LpSolve solver = LpSolve.makeLp(0, nVar);
        solver.setVerbose(LpSolve.CRITICAL);

        // add constraints
        solver.addConstraint(prices, LpSolve.LE, stockLength);
        // objArr.length is equal to nCol + 1
        for (int c = 1; c <= nVar; c++) {
            solver.setInt(c, true);
        }

        // set objective function
        solver.setObjFn(objArr);
        solver.setMaxim();

        // solve the problem
        solver.solve();

        // print solution
        double reducedCost = solver.getObjective();
        System.out.println("Reduced cost: " + reducedCost);
        double[] var = solver.getPtrVariables();
        for (int i = 0; i < var.length; i++) {
            System.out.println("Pattern [" + i + "] = " + var[i]);
        }

        // delete the problem and free memory
        solver.deleteLp();

        double[][] rst = new double[2][nVar];
        rst[0] = new double[] { reducedCost };
        rst[1] = var;
        return rst;
    }

    private static int calLeavingColumn(double[][] basicMatrix, double[] newPattern, double[] rhs) throws LpSolveException {
        int nVar = basicMatrix.length;

        // Create a problem with 4 variables and 0 constraints
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
        solver.solve();

        // print solution
        //        System.out.println("Value of objective function: " + solver.getObjective());
        double[] var = solver.getPtrVariables();
        for (int i = 0; i < var.length; i++) {
            System.out.println("Value of var[" + i + "] = " + var[i]);
        }

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
        return minIndex + 1;
    }
}
