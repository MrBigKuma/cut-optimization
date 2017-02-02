import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CutOptimizationTest {
    @Test
    public void calRequiredBar_WithTensOrder_ExpectOptimal() {
        // Input
        final double rawBarHeightInput = 1000d;
        final double sawWidthInput = 1.5d;
        final List<BarSet> requiredBarSetInputs = Arrays.asList(
                new BarSet(600d, 50),
                new BarSet(200d, 50),
                new BarSet(100d, 10));

        final int nBar = CutOptimization.calRequiredBar(rawBarHeightInput, sawWidthInput, requiredBarSetInputs);

        assertThat(nBar, is(50));
    }

    @Test
    public void calRequiredBar_WithTinyOrder_ExpectOptimal() {
        // Input
        final double rawBarHeightInput = 1000d;
        final double sawWidthInput = 0d;
        final List<BarSet> requiredBarSetInputs = Arrays.asList(
                new BarSet(600d, 2),
                new BarSet(200d, 4));

        final int nBar = CutOptimization.calRequiredBar(rawBarHeightInput, sawWidthInput, requiredBarSetInputs);

        assertThat(nBar, is(2));
    }

    @Test
    public void calRequiredBar_WithSmallOrder_ExpectOptimal() {
        // Input
        final double rawBarHeightInput = 1100d;
        final double sawWidthInput = 0d;
        final List<BarSet> requiredBarSetInputs = Arrays.asList(
                new BarSet(500d, 4),
                new BarSet(200d, 12));

        final int nBar = CutOptimization.calRequiredBar(rawBarHeightInput, sawWidthInput, requiredBarSetInputs);

        assertThat(nBar, is(4));
    }

    @Test
    public void calRequiredBar_WithSmallOrder2_ExpectOptimal() {
        // Input
        final double rawBarHeightInput = 1000d;
        final double sawWidthInput = 0d;
        final List<BarSet> requiredBarSetInputs = Arrays.asList(
                new BarSet(600d, 4),
                new BarSet(200d, 5));

        final int nBar = CutOptimization.calRequiredBar(rawBarHeightInput, sawWidthInput, requiredBarSetInputs);

        assertThat(nBar, is(4));
    }

    @Test
    public void solve_WithBigComplexCase_ExpectOptimal() throws Exception {
        final double rawBarHeightInput = 6000d;
        final double sawWidthInput = 0d;
        final List<BarSet> requiredBarSetInputs = Arrays.asList(
                new BarSet(2158d, 1065),
                new BarSet(1656d, 83),
                new BarSet(1458d, 565),
                new BarSet(734d, 565),
                new BarSet(646d, 556),
                new BarSet(546d, 556)
        );
        final int nBar = CutOptimization.calRequiredBar(rawBarHeightInput, sawWidthInput, requiredBarSetInputs);

        // Simple Cutting software get 747!!! We are better
        assertThat(nBar, is(738));
    }
}
