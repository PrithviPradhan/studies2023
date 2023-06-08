package org.team100.lib.system.examples;

import org.team100.lib.system.Sensor;

import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.Nat;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N2;

public class NormalDoubleIntegratorCartesian1D extends DoubleIntegratorCartesian1D {
    private final Matrix<N2, N1> stateStdev;
    private final Matrix<N2, N1> measurementStdev;

    public NormalDoubleIntegratorCartesian1D(Matrix<N2, N1> stateStdev, Matrix<N2, N1> measurementStdev) {
        this.stateStdev = stateStdev;
        this.measurementStdev = measurementStdev;
    }

    public class NormalFullSensor extends FullSensor {
        public Matrix<N2, N1> stdev() {
            return measurementStdev;
            // return VecBuilder.fill(0.01, 0.1);
        }
    }

    public class NormalPositionSensor extends PositionSensor {
        public Matrix<N1, N1> stdev() {
            return measurementStdev.block(Nat.N1(),Nat.N1(),0,0);
            // return VecBuilder.fill(0.01);
        }
    }

    public class NormalVelocitySensor extends VelocitySensor {
        public Matrix<N1, N1> stdev() {
            return measurementStdev.block(Nat.N1(),Nat.N1(),1,0);
            // return VecBuilder.fill(0.1);
        }
    }

    public Matrix<N2, N1> stdev() {
        return stateStdev;
        // return VecBuilder.fill(0.015, 0.17);
    }

    public Sensor<N2, N1, N1> newPosition() {
        return new NormalPositionSensor();
    }

    public Sensor<N2, N1, N1> newVelocity() {
        return new NormalVelocitySensor();
    }

    public Sensor<N2, N1, N2> newFull() {
        return new NormalFullSensor();
    }
}
