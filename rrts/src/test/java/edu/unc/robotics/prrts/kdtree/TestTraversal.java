package edu.unc.robotics.prrts.kdtree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import  edu.unc.robotics.prrts.tree.Point;

import java.util.List;

import org.junit.jupiter.api.Test;

public class TestTraversal {

    KDModel myModel = new KDModel() {

        @Override
        public int dimensions() {
            return 2;
        }

        @Override
        public double[] getMin() {
            return new double[]{0,0};
        }

        @Override
        public double[] getMax() {
            return new double[]{1,1};
        }

        @Override
        public double dist(double[] start, double[] end) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'dist'");
        }

        @Override
        public void steer(double[] nearConfig, double[] newConfig, double dist) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'steer'");
        }

    };

    static class StringPoint implements Point {
        private final String v;
        private final double[] _config;
        
        public StringPoint(String v, double[] _config) {
            this.v = v;
            this._config = _config;
        }

        @Override
        public double[] getState() {
            return _config;
        }
        public String get_v() {
            return v;

        }
    }

    @Test
    void testInsert() {
        KDNode<StringPoint> n = new KDNode<>(new StringPoint( "n", new double[] { 0, 0 }));
        Util.insert(myModel, n, new StringPoint("one",new double[] { 0.5, 0 }));
        List<StringPoint> s = Util.values(n);
        assertEquals(2, s.size());
        KDNode<StringPoint> a = n.getA();
        KDNode<StringPoint> b = n.getB();
        assertNull(a);
        assertEquals("one", b.getValue().get_v());

    }

}
