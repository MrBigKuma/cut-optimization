import java.util.ArrayList;
import java.util.List;

public class BruteForceSolver {
    /**
     * This recursive generate all possible
     * This is O(2^n) function so it's only feasible for small amount of order
     * OrderSets must be DESC sorted
     *
     * @param orderSets   all orders
     * @param stockLength length of the bar in stock
     * @return a pair of minimum number of  required bar & patterns for each
     */
    public static Pair<Integer, List<List<BarSet>>> solve(List<BarSet> orderSets, double stockLength) {
        // Stop if there is no more bar
        if (isEmpty(orderSets)) {
            ArrayList<List<BarSet>> bs = new ArrayList<>();
            bs.add(new ArrayList<>()); // Create array to let father func put order to
            return new Pair<>(0, bs);
        }

        // Generate all possible patterns
        final List<List<BarSet>> possiblePatterns = calPossibleCutsFor1Stock(0, orderSets, stockLength);

        // With each pattern, recursive solve the problem with remain orders
        Pair<Integer, List<List<BarSet>>> minPattern = null;
        for (List<BarSet> pattern : possiblePatterns) {
            final List<BarSet> remainOrderSets = new ArrayList<>();
            for (int iBar = 0; iBar < orderSets.size(); iBar++) {
                int nUsedBar = 0;
                if (pattern.size() > iBar) {
                    nUsedBar = pattern.get(iBar).num;
                }
                remainOrderSets.add(new BarSet(orderSets.get(iBar).len, orderSets.get(iBar).num - nUsedBar));
            }

            // Recursive solve
            final Pair<Integer, List<List<BarSet>>> optimizedPattern = solve(remainOrderSets, stockLength);

            // Check if it new one better than current minimum
            if (minPattern == null || optimizedPattern.fst + 1 < minPattern.fst) {
                optimizedPattern.snd.add(pattern);
                minPattern = new Pair<>(optimizedPattern.fst + 1, optimizedPattern.snd);
            }
        }

        return minPattern;
    }

    /**
     * This func generate all possible pattern to be fit in one stock len (or remain stock len)
     * from the @param{curOrderIndex} to end of orderSets
     */
    private static List<List<BarSet>> calPossibleCutsFor1Stock(int curOrderIndex, List<BarSet> orderSets, double stockLen) {
        final List<List<BarSet>> possiblePatterns = new ArrayList<>();

        final boolean canCut = orderSets.stream().anyMatch(barSet -> barSet.num > 0 && barSet.len <= stockLen);
        if (!canCut) {
            possiblePatterns.add(new ArrayList<>()); // Create array to let father func put bar in
            return possiblePatterns;
        }

        if (curOrderIndex == orderSets.size()) {
            return possiblePatterns;
        }

        final BarSet curOrderSet = orderSets.get(curOrderIndex);
        int maxBarNum = minInt((int) (stockLen / curOrderSet.len), curOrderSet.num);
        for (int nBar = 0; nBar <= maxBarNum; nBar++) {
            // Clone current order except the one at current index
            final List<BarSet> remainOrderSets = new ArrayList<>();
            remainOrderSets.addAll(orderSets);
            remainOrderSets.set(curOrderIndex, new BarSet(curOrderSet.len, curOrderSet.num - nBar));

            final List<List<BarSet>> subPatterns = calPossibleCutsFor1Stock(
                    curOrderIndex + 1, remainOrderSets, stockLen - curOrderSet.len * nBar);

            final int barNum = nBar;
            subPatterns.forEach(c -> c.add(0, new BarSet(curOrderSet.len, barNum)));
            possiblePatterns.addAll(subPatterns);
        }

        return possiblePatterns;
    }

    private static int minInt(int a, int b) {
        return a > b ? b : a;
    }

    private static boolean isEmpty(List<BarSet> barSets) {
        return barSets.stream().noneMatch(s -> s.num > 0);
    }
}
