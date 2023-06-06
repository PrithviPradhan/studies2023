package org.team100.lib.fusion;

import org.junit.jupiter.api.Test;
import org.team100.lib.math.RandomVector;

import edu.wpi.first.math.numbers.N1;

public class LinearPoolingTest extends PoolingTest {
    private static final Pooling<N1> p = new LinearPooling<N1>();

    @Test
    public void testUnanimity() {
        RandomVector<N1> aV = v1(0, 1);
        RandomVector<N1> bV = v1(0, 1);
        RandomVector<N1> cV = p.fuse(aV, bV);
        assert1(cV, 0, 1);
    }

    @Test
    public void testDifferentMeans() {
        RandomVector<N1> aV = v1(0, 1);
        RandomVector<N1> bV = v1(1, 1);
        RandomVector<N1> cV = p.fuse(aV, bV);
        // aggregate mean is right in the middle
        // aggregate variance is a bit bigger
        assert1(cV, 0.5, 1.25);
    }

    @Test
    public void testDifferentVariance() {
        RandomVector<N1> aV = v1(0, 1);
        RandomVector<N1> bV = v1(0, 2);
        RandomVector<N1> cV = p.fuse(aV, bV);
        // aggregate mean is the same
        // variance is right in the middle
        assert1(cV, 0.0, 1.5);
    }

    @Test
    public void testDifferent() {
        RandomVector<N1> aV = v1(0, 1);
        RandomVector<N1> bV = v1(1, 2);
        RandomVector<N1> cV = p.fuse(aV, bV);
        // aggregate mean is right in the middle
        // aggregate variance is a bit bigger
        assert1(cV, 0.5, 1.75);
    }
}
