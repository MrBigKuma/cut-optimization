import com.sun.tools.javac.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BruteForceSolver {
    public static Pair<Integer, List<List<BarSet>>> optimizeAllBar(List<BarSet> requiredBars, float stockLength) {
        if (isEmpty(requiredBars)) {
            return new Pair<>(0, new ArrayList<List<BarSet>>() {{
                add(new ArrayList<>());
            }});
        }

        List<List<BarSet>> possibleOneBarCuts = calPossibleOneBarCuts(
                0, requiredBars, stockLength);

        //         Print out
        possibleOneBarCuts.forEach(c ->
                System.out.println(
                        c.stream().map(BarSet::toString)
                                .collect(Collectors.joining(", "))
                )
        );
        if (possibleOneBarCuts.size() == 0) {
            System.out.printf("%d, %d\n", requiredBars.size(), possibleOneBarCuts.size());
            System.out.println(requiredBars.stream().map(BarSet::toString).collect(
                    Collectors.joining(", ")
            ));
        }
        Pair<Integer, List<List<BarSet>>> minCut = null;
        for (int iCut = 0; iCut < possibleOneBarCuts.size(); iCut++) {
            List<BarSet> cutBarSets = possibleOneBarCuts.get(iCut);

            List<BarSet> newRequiredBarSets = new ArrayList<>();
            for (int iBar = 0; iBar < requiredBars.size(); iBar++) {
                int nUsedBar = 0;
                if (cutBarSets.size() > iBar) {
                    nUsedBar = cutBarSets.get(iBar).num;
                }
                newRequiredBarSets.add(new BarSet(
                        requiredBars.get(iBar).len,
                        requiredBars.get(iBar).num - nUsedBar
                ));
            }

            Pair<Integer, List<List<BarSet>>> optimizedCut = optimizeAllBar(newRequiredBarSets, stockLength);
            //            System.out.println(optimizedCut.fst);
            if (minCut == null || optimizedCut.fst < minCut.fst) {
                optimizedCut.snd.add(cutBarSets);
                minCut = new Pair<>(optimizedCut.fst + 1, optimizedCut.snd);
            }
        }

        return minCut;
    }

    /**
     * Try different cut until we can't cut any more
     */
    private static List<List<BarSet>> calPossibleOneBarCuts(
            int curBarIndex,
            List<BarSet> barSets,
            float barHeight) {
        //        System.out.printf("calPossibleOneBarCuts(%d, %d, %f) \n",
        //                curBarIndex, barSets.size(), barHeight);
        List<List<BarSet>> possibleBarCuts = new ArrayList<>();

        boolean canCut = barSets.stream()
                .anyMatch(barSet -> barSet.num > 0 && barSet.len <= barHeight);

        if (!canCut) {
            possibleBarCuts.add(new ArrayList<>());
            return possibleBarCuts;
        }

        if (curBarIndex == barSets.size()) {
            return possibleBarCuts;
        }

        BarSet curBarSet = barSets.get(curBarIndex);
        int maxBarNum = minInt((int) (barHeight / curBarSet.len), curBarSet.num);
        for (int i = 0; i <= maxBarNum; i++) {

            List<BarSet> newBarSets = new ArrayList<BarSet>();
            newBarSets.addAll(barSets);
            newBarSets.set(curBarIndex, new BarSet(
                    curBarSet.len,
                    curBarSet.num - i
            ));

            List<List<BarSet>> subBarCuts = calPossibleOneBarCuts(
                    curBarIndex + 1, newBarSets, barHeight - curBarSet.len * i);

            final int barNum = i;
            subBarCuts.forEach(c -> c.add(0, new BarSet(curBarSet.len, barNum)));
            possibleBarCuts.addAll(subBarCuts);
        }

        return possibleBarCuts;
    }

    private static int minInt(int a, int b) {
        if (a > b) {
            return b;
        }
        return a;
    }

    private static boolean isEmpty(List<BarSet> barSets) {
        return barSets.stream().noneMatch(s -> s.num > 0);
    }
}
