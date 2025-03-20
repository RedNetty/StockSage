package com.portfolio.stocksage.util;

import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Utility class for file operations
 */
public final class FileUtils {

    // Allowed image file extensions
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = new HashSet<>(Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp"
    ));

    // Allowed document file extensions
    private static final Set<String> ALLOWED_DOCUMENT_EXTENSIONS = new HashSet<>(Arrays.asList(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv"
    ));

    private FileUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Check if file extension is allowed for images
     *
     * @param filename Filename to check
     * @return True if extension is allowed
     */
    public static boolean isAllowedImageExtension(String filename) {
        String extension = FilenameUtils.getExtension(filename).toLowerCase();
        return ALLOWED_IMAGE_EXTENSIONS.contains(extension);
    }

    /**
     * Check if file extension is allowed for documents
     *
     * @param filename Filename to check
     * @return True if extension is allowed
     */
    public static boolean isAllowedDocumentExtension(String filename) {
        String extension = FilenameUtils.getExtension(filename).toLowerCase();
        return ALLOWED_DOCUMENT_EXTENSIONS.contains(extension);
    }

    /**
     * Generate a unique filename
     *
     * @param originalFilename Original filename
     * @return Unique filename
     */
    public static String generateUniqueFilename(String originalFilename) {
        String extension = FilenameUtils.getExtension(originalFilename);
        return UUID.randomUUID().toString() + "." + extension;
    }

    /**
     * Check if file is an image
     *
     * @param file File to check
     * @return True if file is an image
     */
    public static boolean isImage(MultipartFile file) {
        return file != null &&
                !file.isEmpty() &&
                file.getContentType() != null &&
                file.getContentType().startsWith("image/");
    }

    /**
     * Create directory if it doesn't exist
     *
     * @param directoryPath Directory path to create
     * @return True if directory exists or was created successfully
     */
    public static boolean createDirectoryIfNotExists(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            return directory.mkdirs();
        }
        return true;
    }

    /**
     * Delete file if it exists
     *
     * @param filePath Path to file
     * @return True if file was deleted or doesn't exist
     */
    public static boolean deleteFileIfExists(String filePath) {
        try {
            Path path = Paths.get(filePath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Get file size in human-readable format
     *
     * @param size File size in bytes
     * @return Human-readable file size
     */
    public static String getHumanReadableSize(long size) {
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int unitIndex = 0;
        double unitValue = size;

        while (unitValue > 1024 && unitIndex < units.length - 1) {
            unitValue /= 1024;
            unitIndex++;
        }

        return String.format("%.2f %s", unitValue, units[unitIndex]);
    }
}