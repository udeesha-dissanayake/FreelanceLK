package lk.freelance.backend.controller;

import jakarta.validation.Valid;
import lk.freelance.backend.dto.ApiResponse;
import lk.freelance.backend.dto.JobApplicationDTO;
import lk.freelance.backend.dto.JobApplicationRequest;
import lk.freelance.backend.security.CurrentUser;
import lk.freelance.backend.security.UserPrincipal;
import lk.freelance.backend.service.JobApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for job application lifecycle
 * Base URL: /api/v1/jobs
 *
 * Freelancer endpoints:
 *   POST   /api/v1/jobs/{listingId}/apply          - apply to a job
 *   DELETE /api/v1/jobs/applications/{applicationId}/withdraw - withdraw application
 *   GET    /api/v1/jobs/applications/my             - view own applications
 *
 * Client endpoints:
 *   GET    /api/v1/jobs/{listingId}/applicants      - view all applicants
 *   POST   /api/v1/jobs/applications/{applicationId}/accept - accept an applicant
 *   POST   /api/v1/jobs/applications/{applicationId}/reject - reject an applicant
 */
@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    // ================================================================
    // FREELANCER ENDPOINTS
    // ================================================================

    /**
     * Apply to a job listing
     * POST /api/v1/jobs/{listingId}/apply
     */
    @PostMapping("/{listingId}/apply")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<JobApplicationDTO>> apply(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID listingId,
            @Valid @RequestBody(required = false) JobApplicationRequest request
    ) {
        if (request == null) request = new JobApplicationRequest();

        JobApplicationDTO application = jobApplicationService.apply(
                currentUser.getUserId(),
                listingId,
                request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Application submitted successfully!", application));
    }

    /**
     * Withdraw own application
     * DELETE /api/v1/jobs/applications/{applicationId}/withdraw
     */
    @DeleteMapping("/applications/{applicationId}/withdraw")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<JobApplicationDTO>> withdraw(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID applicationId
    ) {
        JobApplicationDTO application = jobApplicationService.withdraw(
                currentUser.getUserId(),
                applicationId
        );

        return ResponseEntity
                .ok(ApiResponse.success("Application withdrawn.", application));
    }

    /**
     * Freelancer views all their own applications
     * GET /api/v1/jobs/applications/my
     */
    @GetMapping("/applications/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<JobApplicationDTO>>> getMyApplications(
            @CurrentUser UserPrincipal currentUser
    ) {
        List<JobApplicationDTO> applications = jobApplicationService.getMyApplications(
                currentUser.getUserId()
        );

        return ResponseEntity
                .ok(ApiResponse.success(applications));
    }

    // ================================================================
    // CLIENT ENDPOINTS
    // ================================================================

    /**
     * Client views all applicants for their job listing
     * GET /api/v1/jobs/{listingId}/applicants
     */
    @GetMapping("/{listingId}/applicants")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<JobApplicationDTO>>> getApplicants(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID listingId
    ) {
        List<JobApplicationDTO> applicants = jobApplicationService.getApplicants(
                currentUser.getUserId(),
                listingId
        );

        return ResponseEntity
                .ok(ApiResponse.success(applicants));
    }

    /**
     * Client accepts a freelancer's application
     * POST /api/v1/jobs/applications/{applicationId}/accept
     */
    @PostMapping("/applications/{applicationId}/accept")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<JobApplicationDTO>> accept(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID applicationId
    ) {
        JobApplicationDTO application = jobApplicationService.accept(
                currentUser.getUserId(),
                applicationId
        );

        return ResponseEntity
                .ok(ApiResponse.success(
                        "Freelancer accepted! All other applicants have been notified.",
                        application
                ));
    }

    /**
     * Client rejects a specific applicant
     * POST /api/v1/jobs/applications/{applicationId}/reject
     */
    @PostMapping("/applications/{applicationId}/reject")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<JobApplicationDTO>> reject(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID applicationId
    ) {
        JobApplicationDTO application = jobApplicationService.reject(
                currentUser.getUserId(),
                applicationId
        );

        return ResponseEntity
                .ok(ApiResponse.success("Application rejected.", application));
    }
}
