import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CutOptimization {
    /**
     * Main calculation
     *
     * @param stockLengthInput     raw bar length in stock
     * @param sawWidthInput        width of saw
     * @param requiredBarSetInputs required bar set
     * @return num of require bar
     */
    public static int calRequiredBar(
            final double stockLengthInput,
            final double sawWidthInput,
            List<BarSet> requiredBarSetInputs) {
        System.out.printf("-----Bar len: %s-----\n", stockLengthInput);

        // Normalize problem by remove saw width to Cutting Stock Problem (CSP)
        final double stockLength = stockLengthInput + sawWidthInput;
        final List<BarSet> orderSets = requiredBarSetInputs.stream()
                .map(barSetIn -> new BarSet(barSetIn.len + sawWidthInput, barSetIn.num))
                .collect(Collectors.toList());

        // Solve the Cutting Stock Problem
        final Map<List<BarSet>, Integer> rstMap = ColumnGenerationSolver.solve(orderSets, stockLength);

        // Convert problem back to before normalized
        rstMap.keySet().forEach(ptrn -> ptrn.forEach(b->b.len -= sawWidthInput));

        // Print result
        rstMap.keySet().forEach(pattern -> {
            final String pStr = pattern.stream().map(BarSet::toString).collect(Collectors.joining(", "));
            System.out.printf("(x%d): %s\n", rstMap.get(pattern),pStr);
        });

        return rstMap.values().stream().mapToInt(Integer::intValue).sum();
    }
}
