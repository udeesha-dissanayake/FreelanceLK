// ================================================================
// FILE UPLOAD CONTROLLER
// FreelanceLK.com - File Upload Endpoint
// ================================================================
// Handles identity verification documents, listing images,
// avatars, and freelancer work deliverables (.zip, etc.).
// Files are stored on the local filesystem under app.upload.dir.
// Swap the storage backend to S3/GCS by replacing the save logic
// in FileStorageService without touching this controller.
// Version: 1.1.0
// Author: FreelanceLK Development Team
// Last Modified: 2026-05-16
// ================================================================

package lk.freelance.backend.controller;

import lk.freelance.backend.dto.ApiResponse;
import lk.freelance.backend.exception.ResourceNotFoundException;
import lk.freelance.backend.security.CurrentUser;
import lk.freelance.backend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for file uploads
 * Base URL: /api/v1/files
 *
 * Supported upload types (controlled via the "type" query param):
 *   - "verification" → identity documents (NIC, passport scans)
 *   - "listing"      → listing/gig images
 *   - "avatar"       → profile pictures
 *   - "deliverable"  → freelancer work output (zip, pdf, etc.)
 *
 * Stored under:  {app.upload.dir}/{type}/{userId}_{uuid}.{ext}
 * Served at:     GET /api/v1/files/{type}/{filename}
 *
 * NOTE: Deliverable files must NOT be served from the public
 *       file endpoint. The client must use:
 *       GET /api/v1/orders/{orderId}/deliverables/{deliverableId}/file
 *       which enforces order-participant access control.
 *
 * @security All upload endpoints require authentication.
 *           Download is public for listing/avatar/verification.
 *           Deliverable downloads are protected via OrderController.
 */
@RestController
@RequestMapping("/api/v1/files")
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class FileUploadController {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // Max sizes (bytes)
    private static final long MAX_IMAGE_SIZE        =   5 * 1024 * 1024;  //   5 MB
    private static final long MAX_DOCUMENT_SIZE     =  10 * 1024 * 1024;  //  10 MB
    private static final long MAX_DELIVERABLE_SIZE  = 100 * 1024 * 1024;  // 100 MB

    private static final Map<String, Long> TYPE_MAX_SIZE = Map.of(
            "verification", MAX_DOCUMENT_SIZE,
            "listing",      MAX_IMAGE_SIZE,
            "avatar",       MAX_IMAGE_SIZE,
            "deliverable",  MAX_DELIVERABLE_SIZE
    );

    private static final Map<String, String[]> TYPE_ALLOWED_EXTENSIONS = Map.of(
            "verification", new String[]{"jpg", "jpeg", "png", "pdf"},
            "listing",      new String[]{"jpg", "jpeg", "png", "webp"},
            "avatar",       new String[]{"jpg", "jpeg", "png", "webp"},
            "deliverable",  new String[]{"zip", "rar", "pdf", "docx", "doc",
                                         "pptx", "xlsx", "mp4", "mov",
                                         "jpg", "jpeg", "png"}
    );

    // ================================================================
    // POST /api/v1/files/upload?type=verification|listing|avatar|deliverable
    // Upload a file and receive back the stored filename + URL
    // ================================================================

    /**
     * Accepts a single multipart file and stores it under the
     * appropriate subfolder. Returns the generated filename so the
     * caller can persist it (e.g. on IdentityVerification, Listing,
     * or OrderDeliverable).
     *
     * @param file        the uploaded file (form-data key: "file")
     * @param type        one of: verification | listing | avatar | deliverable
     * @param currentUser authenticated user from JWT
     * @return ApiResponse containing the stored filename (UUID-based) and URL
     */
    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "listing") String type,
            @CurrentUser UserPrincipal currentUser
    ) throws IOException {

        // ── Validate type ──────────────────────────────────────────
        if (!TYPE_MAX_SIZE.containsKey(type)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(
                            "Invalid upload type. Must be: verification, listing, avatar, or deliverable"));
        }

        // ── Validate not empty ─────────────────────────────────────
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File must not be empty"));
        }

        // ── Validate size ──────────────────────────────────────────
        long maxSize = TYPE_MAX_SIZE.get(type);
        if (file.getSize() > maxSize) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(
                            "File exceeds maximum size of " + (maxSize / 1024 / 1024) + " MB"));
        }

        // ── Validate extension ─────────────────────────────────────
        String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload");
        String extension = getExtension(originalFilename).toLowerCase();

        boolean extensionAllowed = false;
        for (String allowed : TYPE_ALLOWED_EXTENSIONS.get(type)) {
            if (allowed.equals(extension)) {
                extensionAllowed = true;
                break;
            }
        }
        if (!extensionAllowed) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File type '." + extension + "' is not allowed for '" + type + "' uploads"));
        }

        // ── Generate unique filename ───────────────────────────────
        // Pattern: {userId}_{uuid}.{ext} — ties the file to the uploader for audit purposes
        String storedFilename = currentUser.getUserId() + "_" + UUID.randomUUID() + "." + extension;

        // ── Ensure directory exists & write ───────────────────────
        Path uploadPath = Paths.get(uploadDir).resolve(type).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        Path targetPath = uploadPath.resolve(storedFilename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // ── Build response URL ─────────────────────────────────────
        // Deliverables use the protected order endpoint; other types use the public file endpoint.
        String fileUrl = "deliverable".equals(type)
                ? "/api/v1/files/deliverable/" + storedFilename   // stored path — caller links via order endpoint
                : "/api/v1/files/" + type + "/" + storedFilename;

        return ResponseEntity.ok(ApiResponse.success(
                "File uploaded successfully.",
                Map.of(
                        "filename", storedFilename,
                        "url",      fileUrl,
                        "type",     type
                )
        ));
    }

    // ================================================================
    // GET /api/v1/files/{type}/{filename}
    // Serve a stored file (public for listing/avatar/verification)
    // Deliverables are BLOCKED here — use the order download endpoint
    // ================================================================

    /**
     * Serves stored files inline.
     *
     * Deliverable files are intentionally blocked at this endpoint.
     * The client must use:
     *   GET /api/v1/orders/{orderId}/deliverables/{deliverableId}/file
     * which verifies the caller is the buyer or seller of that order.
     */
    @GetMapping("/{type}/{filename:.+}")
    public ResponseEntity<Resource> serveFile(
            @PathVariable String type,
            @PathVariable String filename
    ) {
        // ── Block direct deliverable access ────────────────────────
        if ("deliverable".equals(type)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // ── Guard against path traversal ──────────────────────────
        String cleanFilename = StringUtils.cleanPath(filename);
        if (cleanFilename.contains("..")) {
            throw new ResourceNotFoundException("Invalid filename");
        }

        try {
            Path filePath = Paths.get(uploadDir).resolve(type).resolve(cleanFilename)
                    .toAbsolutePath().normalize();

            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("File not found: " + filename);
            }

            String contentType = detectContentType(cleanFilename);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + cleanFilename + "\"")
                    .body(resource);

        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("File not found: " + filename);
        }
    }

    // ================================================================
    // HELPERS
    // ================================================================

    String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex >= 0 && dotIndex < filename.length() - 1)
                ? filename.substring(dotIndex + 1)
                : "";
    }

    String getUploadDir() {
        return uploadDir;
    }

    private String detectContentType(String filename) {
        String ext = getExtension(filename).toLowerCase();
        return switch (ext) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png"         -> "image/png";
            case "webp"        -> "image/webp";
            case "pdf"         -> "application/pdf";
            case "zip"         -> "application/zip";
            case "rar"         -> "application/x-rar-compressed";
            case "docx"        -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "doc"         -> "application/msword";
            case "pptx"        -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "xlsx"        -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "mp4"         -> "video/mp4";
            case "mov"         -> "video/quicktime";
            default            -> "application/octet-stream";
        };
    }
}
