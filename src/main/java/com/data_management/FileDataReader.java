package com.data_management;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Reads patient data from files in a specified directory and loads it into DataStorage.
 */
public class FileDataReader implements DataReader {
    private String directoryPath;

    public FileDataReader(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    @Override
    public void readData(DataStorage storage) throws IOException {
        File dir = new File(directoryPath);
        if (!dir.isDirectory()) throw new IOException("Invalid directory: " + directoryPath);

        File[] files = dir.listFiles((d, name) -> name.endsWith(".txt") || name.endsWith(".csv"));
        if (files == null) return;

        for (File file : files) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    try {
                        parseAndAdd(line, storage);
                    } catch (Exception e) {
                        System.err.println("Skipping invalid line in file " + file.getName() + ": " + line);
                    }
                }
            }
        }
    }

    private void parseAndAdd(String line, DataStorage storage) {
        // Example format: patientId,measurementValue,recordType,timestamp
        String[] parts = line.split(",");
        if (parts.length != 4) throw new IllegalArgumentException("Invalid data format");

        int patientId = Integer.parseInt(parts[0].trim());
        double value = Double.parseDouble(parts[1].trim());
        String type = parts[2].trim();
        long timestamp = Long.parseLong(parts[3].trim());

        storage.addPatientData(patientId, value, type, timestamp);
    }
}

