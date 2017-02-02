import java.util.ArrayList;
import java.util.List;

public class BruteForceSolver {
    /**
     * This recursive generate all possible
     * This is O(2^n) function so it's only feasible for small amount of order
     * OrderSets must be DESC sorted
     * @param orderSets all orders
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

        // With each pattern, recursive solve the problem with leftover orders
        Pair<Integer, List<List<BarSet>>> minCut = null;
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
            Pair<Integer, List<List<BarSet>>> optimizedCut = solve(remainOrderSets, stockLength);

            // Check if it new one better then current minimum
            if (minCut == null || optimizedCut.fst < minCut.fst) {
                optimizedCut.snd.add(pattern);
                minCut = new Pair<>(optimizedCut.fst + 1, optimizedCut.snd);
            }
        }

        return minCut;
    }

    /**
     * This func generate all pattern possible to be fit in one bar
     */
    private static List<List<BarSet>> calPossibleCutsFor1Stock(int curBarIndex, List<BarSet> barSets, double stockLen) {
        final List<List<BarSet>> possibleBarCuts = new ArrayList<>();

        boolean canCut = barSets.stream()
                .anyMatch(barSet -> barSet.num > 0 && barSet.len <= stockLen);

        if (!canCut) {
            possibleBarCuts.add(new ArrayList<>());
            return possibleBarCuts;
        }

        if (curBarIndex == barSets.size()) {
            return possibleBarCuts;
        }

        BarSet curBarSet = barSets.get(curBarIndex);
        int maxBarNum = minInt((int) (stockLen / curBarSet.len), curBarSet.num);
        for (int i = 0; i <= maxBarNum; i++) {

            final List<BarSet> newBarSets = new ArrayList<>();
            newBarSets.addAll(barSets);
            newBarSets.set(curBarIndex, new BarSet(curBarSet.len, curBarSet.num - i));

            final List<List<BarSet>> subBarCuts = calPossibleCutsFor1Stock(
                    curBarIndex + 1, newBarSets, stockLen - curBarSet.len * i);

            final int barNum = i;
            subBarCuts.forEach(c -> c.add(0, new BarSet(curBarSet.len, barNum)));
            possibleBarCuts.addAll(subBarCuts);
        }

        return possibleBarCuts;
    }

    private static int minInt(int a, int b) {
        return a > b ? b : a;
    }

    private static boolean isEmpty(List<BarSet> barSets) {
        return barSets.stream().noneMatch(s -> s.num > 0);
    }
}
