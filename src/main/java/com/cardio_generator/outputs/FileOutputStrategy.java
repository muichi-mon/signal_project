package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A file-based implementation of the {@link OutputStrategy} interface.
 * <p>
 * This class outputs patient data to text files. Each label type (e.g., "ECG", "BP")
 * corresponds to a separate file named {@code label.txt} stored in the specified base directory.
 * Data is appended in a human-readable format.
 * </p>
 */
public class FileOutputStrategy implements OutputStrategy {

    /**
     * The root directory where all output files will be stored.
     */
    private String baseDirectory;

    /**
     * A concurrent map that stores the output file paths for each label.
     * Ensures that each signal label has its own dedicated file.
     */
    public final ConcurrentHashMap<String, String> filemap = new ConcurrentHashMap<>();

    /**
     * Constructs a new {@code FileOutputStrategy} with the specified output directory.
     *
     * @param baseDirectory The root directory where all label files will be written.
     */
    public FileOutputStrategy(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    /**
     * Writes the patient data to a file named after the label.
     * If the file does not exist, it is created. Data is appended if the file exists.
     *
     * @param patientId The ID of the patient.
     * @param timestamp The timestamp of the signal data.
     * @param label     The label for the data type (e.g., "ECG", "BP").
     * @param data      The actual signal data value.
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        try {
            // Ensure the base directory exists
            Files.createDirectories(Paths.get(baseDirectory));
        } catch (IOException e) {
            System.err.println("Error creating base directory: " + e.getMessage());
            return;
        }

        // Get or create the file path for this label
        String filePath = filemap.computeIfAbsent(label,
                k -> Paths.get(baseDirectory, label + ".txt").toString());

        // Write data to the file
        try (PrintWriter out = new PrintWriter(
                Files.newBufferedWriter(Paths.get(filePath),
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND))) {
            out.printf("Patient ID: %d, Timestamp: %d, Label: %s, Data: %s%n",
                    patientId, timestamp, label, data);
        } catch (Exception e) {
            System.err.println("Error writing to file " + filePath + ": " + e.getMessage());
        }
    }
}
