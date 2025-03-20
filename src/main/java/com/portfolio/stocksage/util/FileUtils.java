package com.portfolio.stocksage.util;

import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Utility class for file operations
 */
public class FileUtils {

    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif");
    private static final List<String> ALLOWED_DOCUMENT_EXTENSIONS = Arrays.asList("pdf", "doc", "docx", "xls", "xlsx", "csv");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private FileUtils() {
        // Private constructor to prevent instantiation
        throw new AssertionError("FileUtils is a utility class and should not be instantiated");
    }

    /**
     * Generate a unique filename with the original extension
     *
     * @param originalFilename The original filename
     * @return A unique filename with the original extension
     */
    public static String generateUniqueFilename(String originalFilename) {
        String extension = FilenameUtils.getExtension(originalFilename);
        return UUID.randomUUID().toString() + "." + extension;
    }

    /**
     * Check if the file is a valid image
     *
     * @param file The file to check
     * @return true if the file is a valid image, false otherwise
     */
    public static boolean isValidImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        return extension != null && ALLOWED_IMAGE_EXTENSIONS.contains(extension.toLowerCase());
    }

    /**
     * Check if the file is a valid document
     *
     * @param file The file to check
     * @return true if the file is a valid document, false otherwise
     */
    public static boolean isValidDocument(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        return extension != null && ALLOWED_DOCUMENT_EXTENSIONS.contains(extension.toLowerCase());
    }

    /**
     * Check if the file size is within the allowed limit
     *
     * @param file The file to check
     * @return true if the file size is within the allowed limit, false otherwise
     */
    public static boolean isValidFileSize(MultipartFile file) {
        if (file == null) {
            return false;
        }
        return file.getSize() <= MAX_FILE_SIZE;
    }

    /**
     * Save a file to the specified directory
     *
     * @param file The file to save
     * @param directory The directory to save the file to
     * @param filename The name to save the file as
     * @return The path to the saved file
     * @throws IOException If an error occurs while saving the file
     */
    public static Path saveFile(MultipartFile file, String directory, String filename) throws IOException {
        Path directoryPath = Paths.get(directory);
        if (!Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath);
        }

        Path filePath = directoryPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filePath;
    }

    /**
     * Delete a file from the filesystem
     *
     * @param filePath The path to the file to delete
     * @return true if the file was deleted, false otherwise
     */
    public static boolean deleteFile(String filePath) {
        try {
            File file = new File(filePath);
            return file.delete();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get the content type of a file based on its extension
     *
     * @param filename The filename
     * @return The content type
     */
    public static String getContentType(String filename) {
        String extension = FilenameUtils.getExtension(filename).toLowerCase();
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "pdf":
                return "application/pdf";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls":
                return "application/vnd.ms-excel";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "csv":
                return "text/csv";
            default:
                return "application/octet-stream";
        }
    }
}