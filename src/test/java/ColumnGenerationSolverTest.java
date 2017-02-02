import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ColumnGenerationSolverTest {
    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void solve_WithSimpleCase_ExpectOptimal() throws Exception {
        final double stockLength = 6000;
        final List<BarSet> requiredBars = Arrays.asList(
                new BarSet(1656d, 8),
                new BarSet(2158d, 10),
                new BarSet(1458d, 5)
        );
        final Map<List<BarSet>, Integer> rstMap = ColumnGenerationSolver.solve(requiredBars, stockLength);

        final int nBarRst = rstMap.values().stream().mapToInt(Integer::intValue).sum();
        assertThat(nBarRst, is(8));
    }

    @Test
    public void solve_WithBigCase_ExpectOptimal() throws Exception {
        //        double[] prices = new double[] { 6, 1656, 2158, 1458, 546, 734, 646 };
        //        double[] orders = new double[] { 6, 83, 1065, 565, 556, 565, 556 };
        final double stockLength = 100;
        final List<BarSet> requiredBars = Arrays.asList(
                new BarSet(45d, 97),
                new BarSet(36d, 610),
                new BarSet(31d, 395),
                new BarSet(14d, 211)
        );
        final Map<List<BarSet>, Integer> rstMap = ColumnGenerationSolver.solve(requiredBars, stockLength);

        final int nBarRst = rstMap.values().stream().mapToInt(Integer::intValue).sum();
        assertThat(nBarRst, is(453));
    }
}
