package org.team100.lib.simulation;

import org.team100.lib.math.RandomVector;
import org.team100.lib.storage.BitemporalBuffer;
import org.team100.lib.system.examples.DoubleIntegratorRotary1D;

import edu.wpi.first.math.numbers.N2;

/**
 * this is modeled after the sparkmax
 * observes position continuously.
 * records a measurement every 500us and remembers it.
 * makes another measurement in another 500us and discards the first one.
 * every 20ms whatever measurement is available is copied to the 'can bus' queue
 * which holds it for another 2ms.
 */
public class PositionSensor {
    private static final long measurementPeriodUs = 500;
    private static final long messagePeriodUs = 20000;

    private final DoubleIntegratorRotary1D system;
    private final BitemporalBuffer<RandomVector<N2>> m_measurements;

    double measurementValue;
    long measurementTimestampUs;
    double messageValue;
    long messageTimestampUs;

    public PositionSensor(DoubleIntegratorRotary1D system, BitemporalBuffer<RandomVector<N2>> measurements) {
        this.system = system;
        m_measurements = measurements;
    }

    public void step(CompleteState state) {
        long timeUs = state.systemTimeMicrosec;
        double currentTime = state.actualTimeSec();

        if (timeUs > measurementTimestampUs + measurementPeriodUs) {
            // take a new measurement
            measurementValue = state.actualPosition;
            measurementTimestampUs = timeUs;
        }

        if (timeUs > messageTimestampUs + messagePeriodUs) {
            // send a measurement over can bus
            messageValue = measurementValue;
            messageTimestampUs = timeUs;
            // let the rio see the one that we sent
            state.observedPosition = messageValue;
            state.positionObservationTimeSec = currentTime;
            m_measurements.put(timeUs, currentTime, system.position(messageValue));
        }
    }

}
