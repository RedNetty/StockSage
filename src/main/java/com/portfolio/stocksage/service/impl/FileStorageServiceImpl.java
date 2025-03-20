package com.portfolio.stocksage.service.impl;

import com.portfolio.stocksage.exception.FileStorageException;
import com.portfolio.stocksage.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.base-url:http://localhost:8080}")
    private String baseUrl;

    private Path rootLocation;

    @PostConstruct
    @Override
    public void init() {
        this.rootLocation = Paths.get(uploadDir);
        try {
            Files.createDirectories(rootLocation);
            log.info("File storage initialized at {}", rootLocation.toAbsolutePath());

            // Create category subdirectories
            createCategoryDirectory("products");
            createCategoryDirectory("users");
            createCategoryDirectory("reports");
            createCategoryDirectory("documents");
            createCategoryDirectory("temp");
        } catch (IOException e) {
            throw new FileStorageException("Could not initialize storage location", e);
        }
    }

    @Override
    public String store(MultipartFile file, String category) throws IOException {
        if (file.isEmpty()) {
            throw new FileStorageException("Failed to store empty file");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        // Check for invalid characters in filename
        if (originalFilename.contains("..")) {
            throw new FileStorageException("Filename contains invalid path sequence: " + originalFilename);
        }

        // Generate a unique filename
        String newFilename = generateUniqueFilename(originalFilename);

        // Get the category directory path
        Path categoryDir = getCategoryPath(category);

        // Copy file to storage location
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, categoryDir.resolve(newFilename), StandardCopyOption.REPLACE_EXISTING);
            log.info("Stored file {} in category {}", newFilename, category);
            return newFilename;
        } catch (IOException e) {
            throw new FileStorageException("Failed to store file " + originalFilename, e);
        }
    }

    @Override
    public Path load(String filename, String category) {
        Path categoryDir = getCategoryPath(category);
        return categoryDir.resolve(filename);
    }

    @Override
    public boolean delete(String filename, String category) {
        try {
            Path file = load(filename, category);
            return Files.deleteIfExists(file);
        } catch (IOException e) {
            log.error("Error deleting file {} from category {}", filename, category, e);
            return false;
        }
    }

    @Override
    public Stream<Path> loadAll(String category) {
        Path categoryDir = getCategoryPath(category);
        try {
            return Files.walk(categoryDir, 1)
                    .filter(path -> !path.equals(categoryDir))
                    .map(categoryDir::relativize);
        } catch (IOException e) {
            throw new FileStorageException("Failed to read stored files in category " + category, e);
        }
    }

    @Override
    public Stream<Path> loadAllFromSubdirectory(String subdirectory) {
        Path subDir = rootLocation.resolve(subdirectory);
        try {
            if (!Files.exists(subDir)) {
                Files.createDirectories(subDir);
            }
            return Files.walk(subDir, 1)
                    .filter(path -> !path.equals(subDir))
                    .map(subDir::relativize);
        } catch (IOException e) {
            throw new FileStorageException("Failed to read stored files in subdirectory " + subdirectory, e);
        }
    }

    @Override
    public String generateUniqueFilename(String originalFilename) {
        String extension = FilenameUtils.getExtension(originalFilename);
        return UUID.randomUUID().toString() + "." + extension;
    }

    @Override
    public String getFileUrl(String filename, String category) {
        return baseUrl + "/" + uploadDir + "/" + category + "/" + filename;
    }

    @Override
    public String toString() {
        return rootLocation.toString();
    }

    /**
     * Create a category subdirectory if it doesn't exist
     */
    private void createCategoryDirectory(String category) throws IOException {
        Path categoryDir = rootLocation.resolve(category);
        if (!Files.exists(categoryDir)) {
            Files.createDirectories(categoryDir);
            log.info("Created category directory: {}", categoryDir.toAbsolutePath());
        }
    }

    /**
     * Get the path for a specific category
     */
    private Path getCategoryPath(String category) {
        Path categoryDir = rootLocation.resolve(category);
        if (!Files.exists(categoryDir)) {
            try {
                Files.createDirectories(categoryDir);
                log.info("Created category directory: {}", categoryDir.toAbsolutePath());
            } catch (IOException e) {
                throw new FileStorageException("Could not create category directory: " + category, e);
            }
        }
        return categoryDir;
    }
}