package com.cardio_generator.generators;

import java.util.Random;
import com.cardio_generator.outputs.OutputStrategy;

/**
 * Generates simulated blood oxygen saturation data for patients.
 * <p>
 * This generator simulates realistic, slightly fluctuating oxygen saturation values
 * between 90% and 100% for each patient. It maintains a last known saturation value
 * per patient and updates it slightly on each generation cycle.
 */
public class BloodSaturationDataGenerator implements PatientDataGenerator {

    private static final Random RANDOM = new Random();
    private final int[] lastSaturationValues;

    /**
     * Constructs a new {@code BloodSaturationDataGenerator} for the specified number of patients.
     *
     * @param patientCount the total number of patients to simulate
     */
    public BloodSaturationDataGenerator(int patientCount) {
        lastSaturationValues = new int[patientCount + 1];

        // Initialize baseline saturation values between 95 and 100
        for (int i = 1; i <= patientCount; i++) {
            lastSaturationValues[i] = 95 + RANDOM.nextInt(6);
        }
    }

    /**
     * Generates a new blood saturation data point for the given patient and sends it to the output strategy.
     * <p>
     * The value fluctuates slightly around the previous value, and is clamped between 90% and 100%.
     *
     * @param patientId      the ID of the patient to generate data for
     * @param outputStrategy the output strategy used to send the generated data
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            // Simulate small fluctuation: -1, 0, or 1
            int variation = RANDOM.nextInt(3) - 1;
            int newSaturationValue = lastSaturationValues[patientId] + variation;

            // Clamp value to a realistic healthy range [90, 100]
            newSaturationValue = Math.min(Math.max(newSaturationValue, 90), 100);
            lastSaturationValues[patientId] = newSaturationValue;

            outputStrategy.output(
                    patientId,
                    System.currentTimeMillis(),
                    "Saturation",
                    newSaturationValue + "%"
            );
        } catch (Exception e) {
            System.err.println(
                    "An error occurred while generating blood saturation data for patient " + patientId
            );
            e.printStackTrace();
        }
    }
}
