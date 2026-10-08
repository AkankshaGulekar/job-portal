package com.jobportal.controller;

import com.jobportal.dto.ApplicationResponse;
import com.jobportal.dto.PageResponse;
import com.jobportal.dto.StatusUpdateRequest;
import com.jobportal.entity.ApplicationStatus;
import com.jobportal.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    @Operation(summary = "Candidate: apply to a job with a resume (pdf/doc/docx, max 5MB)")
    @PreAuthorize("hasRole('CANDIDATE')")
    @PostMapping(value = "/jobs/{jobId}/apply", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse apply(@PathVariable Long jobId,
                                     @RequestPart("resume") MultipartFile resume,
                                     Authentication auth) {
        return applicationService.apply(jobId, auth.getName(), resume);
    }

    @Operation(summary = "Candidate: my applications with status")
    @PreAuthorize("hasRole('CANDIDATE')")
    @GetMapping("/applications/mine")
    public PageResponse<ApplicationResponse> mine(Authentication auth,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        return applicationService.myApplications(auth.getName(), page, size);
    }

    @Operation(summary = "Recruiter: applications for one of my jobs, optionally filtered by status")
    @PreAuthorize("hasRole('RECRUITER')")
    @GetMapping("/jobs/{jobId}/applications")
    public PageResponse<ApplicationResponse> forJob(@PathVariable Long jobId,
                                                    @RequestParam(required = false) ApplicationStatus status,
                                                    Authentication auth,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        return applicationService.jobApplications(jobId, status, auth.getName(), page, size);
    }

    @Operation(summary = "Recruiter: shortlist or reject an application")
    @PreAuthorize("hasRole('RECRUITER')")
    @PatchMapping("/applications/{id}/status")
    public ApplicationResponse updateStatus(@PathVariable Long id,
                                            @Valid @RequestBody StatusUpdateRequest request,
                                            Authentication auth) {
        return applicationService.updateStatus(id, request.status(), auth.getName());
    }

    @Operation(summary = "Download a resume (owner candidate or job's recruiter only)")
    @GetMapping("/applications/{id}/resume")
    public ResponseEntity<Resource> resume(@PathVariable Long id, Authentication auth) {
        Resource resource = applicationService.getResume(id, auth.getName());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
