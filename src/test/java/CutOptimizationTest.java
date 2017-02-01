import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CutOptimizationTest {
    @Test
    public void calRequiredBar_WithTensOrder_ExpectOptimal() {
        // Input
        final float rawBarHeightInput = 1000f;
        final float sawWidthInput = 1.5f;
        List<BarSet> requiredBarSetInputs = new ArrayList<BarSet>() {{
            add(new BarSet(600f, 50));
            add(new BarSet(200f, 50));
            add(new BarSet(100f, 10));
        }};

        int nBar = CutOptimization.calRequiredBar(rawBarHeightInput, sawWidthInput, requiredBarSetInputs);
        assertThat(nBar, is(50));
    }

    @Test
    public void calRequiredBar_WithTinyOrder_ExpectOptimal() {
        // Input
        final float rawBarHeightInput = 1000f;
        final float sawWidthInput = 0f;
        List<BarSet> requiredBarSetInputs = new ArrayList<BarSet>() {{
            add(new BarSet(600f, 2));
            add(new BarSet(200f, 4));
        }};

        int nBar = CutOptimization.calRequiredBar(rawBarHeightInput, sawWidthInput, requiredBarSetInputs);
        assertThat(nBar, is(2));
    }

    @Test
    public void calRequiredBar_WithSmallOrder_ExpectOptimal() {
        // Input
        final float rawBarHeightInput = 1100f;
        final float sawWidthInput = 0f;
        List<BarSet> requiredBarSetInputs = new ArrayList<BarSet>() {{
            add(new BarSet(500f, 4));
            add(new BarSet(200f, 12));
        }};

        int nBar = CutOptimization.calRequiredBar(rawBarHeightInput, sawWidthInput, requiredBarSetInputs);
        assertThat(nBar, is(4));
    }

    @Test
    public void calRequiredBar_WithSmallOrder2_ExpectOptimal() {
        // Input
        final float rawBarHeightInput = 1000f;
        final float sawWidthInput = 0f;
        List<BarSet> requiredBarSetInputs = new ArrayList<BarSet>() {{
            add(new BarSet(600f, 4));
            add(new BarSet(200f, 5));
        }};

        int nBar = CutOptimization.calRequiredBar(rawBarHeightInput, sawWidthInput, requiredBarSetInputs);
        assertThat(nBar, is(4));
    }

}
