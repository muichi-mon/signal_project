package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Interface for generating patient-specific health data.
 * <p>
 * Implementations of this interface define how to generate and output
 * specific types of medical data (e.g., ECG, blood pressure) for individual patients.
 * </p>
 */
public interface PatientDataGenerator {

    /**
     * Generates health data for a specific patient and sends it through the provided output strategy.
     *
     * @param patientId      The unique identifier of the patient.
     * @param outputStrategy The strategy used to output the generated data (e.g., to console, file, or network).
     */
    void generate(int patientId, OutputStrategy outputStrategy);
}
