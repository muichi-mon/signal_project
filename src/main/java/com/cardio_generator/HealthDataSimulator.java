package com.cardio_generator;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.alerts.AlertGenerator;
import com.cardio_generator.generators.BloodLevelsDataGenerator;
import com.cardio_generator.generators.BloodPressureDataGenerator;
import com.cardio_generator.generators.BloodSaturationDataGenerator;
import com.cardio_generator.generators.ECGDataGenerator;
import com.cardio_generator.outputs.*;
import com.data_management.DataReader;
import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.WebSocketDataReader;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Main class for simulating health-related data for multiple patients.
 * <p>
 * Generates various types of cardiovascular and physiological signals (e.g., ECG, blood pressure),
 * and outputs them through different strategies (console, file, WebSocket, or TCP).
 * </p>
 *
 * <p>Usage:</p>
 * <pre>
 * java HealthDataSimulator --patient-count 100 --output websocket:8080
 * </pre>
 */
public class HealthDataSimulator {

    /**
     * Default number of patients to simulate.
     */
    private static int patientCount = 50;

    /**
     * The scheduled thread pool used to run generation tasks periodically.
     */
    private static ScheduledExecutorService scheduler;

    /**
     * The output strategy for generated data. Default is console output.
     */
    private static OutputStrategy outputStrategy = new ConsoleOutputStrategy();

    /**
     * Random number generator for staggering task start times.
     */
    private static final Random random = new Random();

    /**
     * Entry point of the simulation. Parses arguments, initializes patients, and starts data generation tasks.
     *
     * @param args Command-line arguments for patient count and output type.
     * @throws IOException If directory or file creation fails.
     */
    public static void main(String[] args) {
        // Connect to simulator's WebSocket output
        String websocketUrl = "ws://localhost:8080"; // Same port used by the simulator
        DataReader reader = new WebSocketDataReader(websocketUrl);
        DataStorage storage = new DataStorage(reader); // Load initial data

        // Start analyzing new incoming data as it's received
        AlertGenerator alertGenerator = new AlertGenerator(storage);

        // Periodically evaluate patients (every 10 seconds)
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(10000); // Wait 10 seconds
                    for (Patient patient : storage.getAllPatients()) {
                        alertGenerator.evaluateData(patient);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    /**
     * Parses command-line arguments for patient count and output strategy.
     *
     * @param args The command-line arguments.
     * @throws IOException If directory creation for file output fails.
     */
    private static void parseArguments(String[] args) throws IOException {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-h":
                    printHelp();
                    System.exit(0);
                    break;
                case "--patient-count":
                    if (i + 1 < args.length) {
                        try {
                            patientCount = Integer.parseInt(args[++i]);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid number of patients. Using default: " + patientCount);
                        }
                    }
                    break;
                case "--output":
                    if (i + 1 < args.length) {
                        String outputArg = args[++i];
                        if (outputArg.equals("console")) {
                            outputStrategy = new ConsoleOutputStrategy();
                        } else if (outputArg.startsWith("file:")) {
                            String baseDirectory = outputArg.substring(5);
                            Path outputPath = Paths.get(baseDirectory);
                            if (!Files.exists(outputPath)) {
                                Files.createDirectories(outputPath);
                            }
                            outputStrategy = new FileOutputStrategy(baseDirectory);
                        } else if (outputArg.startsWith("websocket:")) {
                            try {
                                int port = Integer.parseInt(outputArg.substring(10));
                                outputStrategy = new WebSocketOutputStrategy(port);
                                System.out.println("WebSocket output will be on port: " + port);
                            } catch (NumberFormatException e) {
                                System.err.println("Invalid WebSocket port number.");
                            }
                        } else if (outputArg.startsWith("tcp:")) {
                            try {
                                int port = Integer.parseInt(outputArg.substring(4));
                                outputStrategy = new TcpOutputStrategy(port);
                                System.out.println("TCP socket output will be on port: " + port);
                            } catch (NumberFormatException e) {
                                System.err.println("Invalid TCP port number.");
                            }
                        } else {
                            System.err.println("Unknown output type. Using default (console).");
                        }
                    }
                    break;
                default:
                    System.err.println("Unknown option '" + args[i] + "'");
                    printHelp();
                    System.exit(1);
            }
        }
    }

    /**
     * Prints usage information for the simulator.
     */
    private static void printHelp() {
        System.out.println("Usage: java HealthDataSimulator [options]");
        System.out.println("Options:");
        System.out.println("  -h                       Show help and exit.");
        System.out.println("  --patient-count <count>  Number of patients to simulate (default: 50).");
        System.out.println("  --output <type>          Output strategy: console, file:<dir>, websocket:<port>, tcp:<port>");
        System.out.println("Example:");
        System.out.println("  java HealthDataSimulator --patient-count 100 --output websocket:8080");
    }

    /**
     * Initializes a list of unique patient IDs from 1 to the specified count.
     *
     * @param patientCount The number of patients.
     * @return A list of patient IDs.
     */
    private static List<Integer> initializePatientIds(int patientCount) {
        List<Integer> patientIds = new ArrayList<>();
        for (int i = 1; i <= patientCount; i++) {
            patientIds.add(i);
        }
        return patientIds;
    }

    /**
     * Schedules recurring tasks for generating health data for each patient.
     *
     * @param patientIds List of patient IDs to simulate.
     */
    private static void scheduleTasksForPatients(List<Integer> patientIds, DataStorage dataStorage, int patientCount) {
        ECGDataGenerator ecgDataGenerator = new ECGDataGenerator(patientCount);
        BloodSaturationDataGenerator bloodSaturationDataGenerator = new BloodSaturationDataGenerator(patientCount);
        BloodPressureDataGenerator bloodPressureDataGenerator = new BloodPressureDataGenerator(patientCount);
        BloodLevelsDataGenerator bloodLevelsDataGenerator = new BloodLevelsDataGenerator(patientCount);
        AlertGenerator alertGenerator = new AlertGenerator(dataStorage);

        for (int patientId : patientIds) {
            scheduleTask(() -> ecgDataGenerator.generate(patientId, outputStrategy), 1, TimeUnit.SECONDS);
            scheduleTask(() -> bloodSaturationDataGenerator.generate(patientId, outputStrategy), 1, TimeUnit.SECONDS);
            scheduleTask(() -> bloodPressureDataGenerator.generate(patientId, outputStrategy), 1, TimeUnit.MINUTES);
            scheduleTask(() -> bloodLevelsDataGenerator.generate(patientId, outputStrategy), 2, TimeUnit.MINUTES);
            scheduleTask(() -> {
                Patient patient = dataStorage.getPatient(patientId);
                alertGenerator.evaluateData(patient);
            }, 20, TimeUnit.SECONDS);
        }
    }


    /**
     * Schedules a recurring task with an initial random delay to avoid uniform timing.
     *
     * @param task     The task to schedule.
     * @param period   The period between executions.
     * @param timeUnit The time unit for the period.
     */
    private static void scheduleTask(Runnable task, long period, TimeUnit timeUnit) {
        scheduler.scheduleAtFixedRate(task, random.nextInt(5), period, timeUnit);
    }
}
