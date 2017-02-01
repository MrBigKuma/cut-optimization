import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {
        case1();
        case2();
        case3();
        case4();
    }

    /**
     * Main calculation
     *
     * @param stockLengthInput raw bar length in stock
     * @param sawWidthInput width of saw
     * @param requiredBarSetInputs required bar set
     * @return num of require bar
     */
    private static int calRequiredBar(
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

    private static void case1() {
        // Input
        final float rawBarHeightInput = 1000f;
        final float sawWidthInput = 1.5f;
        List<BarSet> requiredBarSetInputs = new ArrayList<BarSet>() {{
            add(new BarSet(600f, 50));
            add(new BarSet(200f, 50));
            add(new BarSet(100f, 10));
        }};

        int nBar = calRequiredBar(rawBarHeightInput, sawWidthInput, requiredBarSetInputs);
        System.out.printf("Need %d bar(s) [optimal: 50]", nBar);
    }

    private static void case2() {
        // Input
        final float rawBarHeightInput = 1000f;
        final float sawWidthInput = 0f;
        List<BarSet> requiredBarSetInputs = new ArrayList<BarSet>() {{
            add(new BarSet(600f, 2));
            add(new BarSet(200f, 4));
        }};

        int nBar = calRequiredBar(rawBarHeightInput, sawWidthInput, requiredBarSetInputs);
        System.out.printf("Need %d bar(s) [optimal: 2]", nBar);
    }

    private static void case3() {
        // Input
        final float rawBarHeightInput = 1100f;
        final float sawWidthInput = 0f;
        List<BarSet> requiredBarSetInputs = new ArrayList<BarSet>() {{
            add(new BarSet(500f, 4));
            add(new BarSet(200f, 12));
        }};

        int nBar = calRequiredBar(rawBarHeightInput, sawWidthInput, requiredBarSetInputs);
        System.out.printf("Need %d bar(s) [optimal: 4]\n", nBar);
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
