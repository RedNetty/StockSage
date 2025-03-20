package com.portfolio.stocksage.controller.api;

import com.portfolio.stocksage.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "File API", description = "Endpoints for file management")
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    @Operation(summary = "Upload a file", description = "Uploads a file to the server and returns its details")
    public ResponseEntity<Map<String, String>> uploadFile(
            @Parameter(description = "File to upload", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Category for the file (products, users, etc.)", required = true)
            @RequestParam("category") String category) {

        try {
            String filename = fileStorageService.store(file, category);

            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/files/download/")
                    .path(category + "/")
                    .path(filename)
                    .toUriString();

            Map<String, String> response = new HashMap<>();
            response.put("filename", filename);
            response.put("fileUrl", fileDownloadUri);
            response.put("size", String.valueOf(file.getSize()));
            response.put("contentType", file.getContentType());

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to upload file: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/download/{category}/{filename:.+}")
    @Operation(summary = "Download a file", description = "Downloads a file from the server")
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "Filename to download", required = true)
            @PathVariable String filename,
            @Parameter(description = "Category of the file", required = true)
            @PathVariable String category) {

        try {
            Path filePath = fileStorageService.load(filename, category);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{category}/{filename:.+}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    @Operation(summary = "Delete a file", description = "Deletes a file from the server")
    public ResponseEntity<Map<String, Boolean>> deleteFile(
            @Parameter(description = "Filename to delete", required = true)
            @PathVariable String filename,
            @Parameter(description = "Category of the file", required = true)
            @PathVariable String category) {

        boolean deleted = fileStorageService.delete(filename, category);

        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", deleted);

        if (deleted) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}