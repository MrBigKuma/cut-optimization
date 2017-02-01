import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CutOptimization {
    /**
     * Main calculation
     *
     * @param stockLengthInput raw bar length in stock
     * @param sawWidthInput width of saw
     * @param requiredBarSetInputs required bar set
     * @return num of require bar
     */
    public static int calRequiredBar(
            final float stockLengthInput,
            final float sawWidthInput,
            List<BarSet> requiredBarSetInputs) {
        System.out.printf("-----Bar len: %s-----\n", stockLengthInput);

        // Normalize problem by remove saw width to Cutting Stock Problem (CSP)
        final float stockLength = stockLengthInput + sawWidthInput;
        final List<BarSet> orderSets = requiredBarSetInputs.stream()
                .map(barSetIn -> new BarSet(barSetIn.len + sawWidthInput, barSetIn.num))
                .collect(Collectors.toList());

        // Solve the Cutting Stock Problem
        final Map<List<BarSet>, Integer> rstMap = ColumnGenerationSolver.solve(orderSets, stockLength);
        // Print result
        rstMap.keySet().forEach(pattern -> {
            final String pStr = pattern.stream().map(BarSet::toString).collect(Collectors.joining(", "));
            System.out.println(pStr + " ---> x" + rstMap.get(pattern));
        });

        return rstMap.values().stream().mapToInt(Integer::intValue).sum();
    }

    private static void case4() {
        // Input
        final float rawBarHeightInput = 1000f;
        final float sawWidthInput = 0f;
        List<BarSet> requiredBarSetInputs = new ArrayList<BarSet>() {{
            add(new BarSet(600f, 4));
            add(new BarSet(200f, 5));
        }};

        int nBar = calRequiredBar(rawBarHeightInput, sawWidthInput, requiredBarSetInputs);
        System.out.printf("Need %d bar(s) [optimal: 4]\n", nBar);
    }
}
