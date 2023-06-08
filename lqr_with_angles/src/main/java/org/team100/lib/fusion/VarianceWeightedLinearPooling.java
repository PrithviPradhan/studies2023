package org.team100.lib.fusion;

import org.team100.lib.math.RandomVector;

import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.Num;

/**
 * Variance weighted linear pooling is also called "mixing" -- it represents the
 * weighted average of probability functions where the weights are the inverse
 * variances.
 * 
 * This has the advantage of linear mixing -- the variance respects the
 * dispersion in means -- and also the advantage of log-linear mixing -- the
 * mean is precision-weighted.
 * 
 * The weights are as follows:
 * 
 * pa = (1/A)/(1/A + 1/B)
 * pb = (1/B)/(1/A + 1/B)
 */
public class VarianceWeightedLinearPooling<States extends Num> extends LinearPooling<States> {
    private static final double kThreshold = 1e-9;
    public RandomVector<States> fuse(RandomVector<States> a, RandomVector<States> b) {
        Matrix<States, States> aP = a.P;
        Matrix<States, States> bP = b.P;
        if (aP.det() < kThreshold) {
            throw new IllegalArgumentException("aP is singular.\n" + aP.toString());
        }
        if (bP.det() < kThreshold) {
            throw new IllegalArgumentException("bP is singular.\n" + bP.toString());
        }
        Matrix<States, States> aPI = aP.inv();
        Matrix<States, States> bPI = bP.inv();
        Matrix<States, States> PIsum = aPI.plus(bPI);
        if (PIsum.det() < kThreshold) {
            throw new IllegalArgumentException("PIsum is singular.\n" + PIsum.toString());
        }
        Matrix<States, States> pIsumI = PIsum.inv();
        Matrix<States, States> pa = aPI.times(pIsumI);
        Matrix<States, States> pb = bPI.times(pIsumI);
        // and the weights are a square matrix. hm.
        return fuse(a, pa, b, pb);
    }

}
