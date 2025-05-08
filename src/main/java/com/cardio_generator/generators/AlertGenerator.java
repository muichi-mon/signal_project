package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Generates simulated alert data for patients.
 * <p>
 * Each patient has an independent alert state that can be either triggered or resolved.
 * Alerts are generated probabilistically using a Poisson process approximation.
 * </p>
 */
public class AlertGenerator implements PatientDataGenerator {

    /**
     * Shared random number generator used for alert generation.
     */
    public static final Random randomGenerator = new Random();

    /**
     * Tracks the current alert state for each patient.
     * Index corresponds to patient ID. True = alert active, false = no alert.
     */
    private final boolean[] alertStates;

    /**
     * Constructs an AlertGenerator for the specified number of patients.
     *
     * @param patientCount The number of patients to simulate.
     */
    public AlertGenerator(int patientCount) {
        alertStates = new boolean[patientCount + 1]; // 1-based indexing
    }

    /**
     * Generates an alert for the specified patient based on probabilistic rules.
     * If the patient is already in an alert state, there is a 90% chance to resolve it.
     * If not in an alert state, there's a small chance to trigger a new alert.
     *
     * @param patientId      The ID of the patient for whom data is being generated.
     * @param outputStrategy The strategy to output generated alert data.
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            if (alertStates[patientId]) {
                // Resolve existing alert with 90% probability
                if (randomGenerator.nextDouble() < 0.9) {
                    alertStates[patientId] = false;
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "resolved");
                }
            } else {
                // Calculate probability of triggering a new alert (Poisson approximation)
                double lambda = 0.1; // expected alerts per period
                double p = -Math.expm1(-lambda); // P(at least one event)
                boolean alertTriggered = randomGenerator.nextDouble() < p;

                if (alertTriggered) {
                    alertStates[patientId] = true;
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "triggered");
                }
            }
        } catch (Exception e) {
            System.err.println("An error occurred while generating alert data for patient " + patientId);
            e.printStackTrace();
        }
    }
}
