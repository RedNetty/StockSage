package com.portfolio.stocksage.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface FileStorageService {

    /**
     * Initialize storage directories
     */
    void init();

    /**
     * Store a file
     * @param file The file to store
     * @param category The category of file (e.g., "products", "users")
     * @return The filename of the stored file
     * @throws IOException If file storage fails
     */
    String store(MultipartFile file, String category) throws IOException;

    /**
     * Load a file as a Path
     * @param filename The filename
     * @param category The category of file
     * @return The Path to the file
     */
    Path load(String filename, String category);

    /**
     * Delete a file
     * @param filename The filename to delete
     * @param category The category of file
     * @return true if deletion was successful
     */
    boolean delete(String filename, String category);

    /**
     * Load all files in a category
     * @param category The category of files
     * @return Stream of Paths
     */
    Stream<Path> loadAll(String category);

    /**
     * Load all files from a subdirectory
     * @param subdirectory The subdirectory name
     * @return Stream of Paths
     */
    Stream<Path> loadAllFromSubdirectory(String subdirectory);

    /**
     * Generate a unique filename for a file
     * @param originalFilename The original filename
     * @return A unique filename
     */
    String generateUniqueFilename(String originalFilename);

    /**
     * Get public URL for a file
     * @param filename The filename
     * @param category The category of file
     * @return The public URL for accessing the file
     */
    String getFileUrl(String filename, String category);

    /**
     * Get the root directory path as a string
     * @return The root directory path string
     */
    String toString();
}