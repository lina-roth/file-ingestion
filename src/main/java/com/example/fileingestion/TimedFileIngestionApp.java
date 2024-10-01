package com.example.fileingestion;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.*;

public class TimedFileIngestionApp {


    /**
     * How It Works:
     *
     * Scheduled Directory Check:
     * The ScheduledExecutorService checks the directory every 1 minute (scheduler.scheduleAtFixedRate).
     * It uses a DirectoryStream to iterate over the files in the directory each time it checks. The checkDirectoryForFiles()
     * method looks for .txt files in the monitored folder and places them in the
     * fileQueue for processing.Moves files to ingestion-error folder if not a valid file.
     *
     * Queue Processing:
     * The processFilesFromQueue() method constantly checks the queue for new files. When a file is added to the queue,
     * it reads the file's content and simulates sending it to a processing queue (see below).
     * This is where we would want to hit the Orchestration API. It then moves the files to ingestion-completed
     *
     * Use Java Queue If:
     * - Your application is relatively simple and doesn't require high scalability.
     * - You're processing a manageable number of items and can afford to lose some data if the application crashes.
     * - You are running in a single instance and don't need the distributed capabilities.
     *
     * Use RabbitMQ If:
     * - You're dealing with a high volume of items that may require distributed processing across multiple instances.
     * - You need reliable message delivery and want to ensure that no items are lost.
     * - You want to decouple the processing logic from the production of messages for better maintainability.
     *
     * BlockingQueue:
     * We use a BlockingQueue to store file paths. This is thread-safe, so multiple threads can safely access the queue
     * for both ingestion and processing.
     *
     * @author: Lina Roth
     * @date: 10/1/2024
     */

    // A thread-safe blocking queue to hold files for processing
    private final BlockingQueue<Path> fileQueue = new LinkedBlockingQueue<>();

    // The directory to monitor
    private final Path directoryToWatch;

    // The directory where processed files will be moved
    private final Path ingestionCompletedDir;

    // The directory where processed files will be moved
    private final Path ingestionErrorDir;

    // ScheduledExecutorService for periodic folder checks
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    // Extension to filter for
    private static final String FILE_EXTENSION = ".txt";

    public TimedFileIngestionApp(String directoryPath, String completedDirectoryPath, String errorDirectoryPath) throws IOException {
        this.directoryToWatch = Paths.get(directoryPath);
        this.ingestionCompletedDir = Paths.get(completedDirectoryPath);
        this.ingestionErrorDir = Paths.get(errorDirectoryPath);

        // Create the "ingestion" folder if it doesn't exist
        if (!Files.exists(directoryToWatch)) {
            Files.createDirectories(directoryToWatch);
        }
        // Create the "ingestion-completed" folder if it doesn't exist
        if (!Files.exists(ingestionCompletedDir)) {
            Files.createDirectories(ingestionCompletedDir);
        }
        // Create the "ingestion-error" folder if it doesn't exist
        if (!Files.exists(ingestionErrorDir)) {
            Files.createDirectories(ingestionErrorDir);
        }
    }

    // Start the file ingestion and processing system
    public void start() throws IOException, InterruptedException {
        // Schedule the directory monitoring every 1 minute
        scheduler.scheduleAtFixedRate(this::checkDirectoryForFiles, 0, 1, TimeUnit.MINUTES);

        // Start processing files from the queue
        processFilesFromQueue();
    }

    // Check the directory for new files every 1 minute
    private void checkDirectoryForFiles() {
        System.out.println("1 minute has passed - Checking directory for new files...");
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directoryToWatch)) {
            for (Path filePath : directoryStream) {
                if (Files.isRegularFile(filePath) &&  filePath.toString().endsWith(FILE_EXTENSION)) {
                    System.out.println("New file detected: " + filePath);
                    fileQueue.put(filePath);  // Put the new file on the queue for processing
                } else {
                    System.out.println("Ignoring non-regular file: " + filePath);
                    Path targetPath = ingestionErrorDir.resolve(filePath.getFileName());
                    Files.move(filePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("File moved to ingestion-error: " + targetPath);

                }
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error checking directory: " + e.getMessage());
        }
    }

    // Process files from the queue
    private void processFilesFromQueue() {
        while (true) {
            try {
                // Take the next file from the queue
                Path filePath = fileQueue.take();
                System.out.println("Processing file: " + filePath);

                // Simulate processing the file (you can add your actual processing logic here)
                String content = new String(Files.readAllBytes(filePath));
                System.out.println("File content: " + content);

                // Simulate putting the file content on a processing queue (e.g., a message queue)
                System.out.println("File content placed on processing queue for hitting Orchestration API: " + filePath.getFileName());

                // Move the processed file to the ingestion-completed directory
                Path targetPath = ingestionCompletedDir.resolve(filePath.getFileName());
                Files.move(filePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("File moved to ingestion-completed: " + targetPath);

            } catch (InterruptedException | IOException e) {
                System.err.println("Error processing file: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        // Specify the folder to monitor
        String directoryToMonitor = System.getenv("INGESTION_DIR");
        String completedFolder = System.getenv("COMPLETED_DIR");
        String errorFolder = System.getenv("ERROR_DIR");

        // Check if the environment variables are not null
        if (directoryToMonitor == null || completedFolder == null || errorFolder == null) {
            System.out.println("Environment variables not set correctly!");
            System.exit(1);
        }

        // Your app logic
        System.out.println("Ingestion Directory: " + directoryToMonitor);
        System.out.println("Completed Directory: " + completedFolder);
        System.out.println("Error Folder: " + errorFolder);

        // Start the file ingestion application
        TimedFileIngestionApp app = new TimedFileIngestionApp(directoryToMonitor, completedFolder, errorFolder);
        app.start();
    }
}
