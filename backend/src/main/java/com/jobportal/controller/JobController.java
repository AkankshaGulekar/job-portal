package com.jobportal.controller;

import com.jobportal.dto.JobRequest;
import com.jobportal.dto.JobResponse;
import com.jobportal.dto.PageResponse;
import com.jobportal.entity.JobType;
import com.jobportal.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Jobs")
public class JobController {

    private final JobService jobService;

    @Operation(summary = "Search jobs (public) with pagination and filters")
    @GetMapping("/jobs")
    public PageResponse<JobResponse> search(@RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) String location,
                                            @RequestParam(required = false) JobType jobType,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return jobService.search(keyword, location, jobType, page, size);
    }

    @Operation(summary = "Get a job by id (public)")
    @GetMapping("/jobs/{id}")
    public JobResponse get(@PathVariable Long id) {
        return jobService.get(id);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Recruiter: list my posted jobs")
    @PreAuthorize("hasRole('RECRUITER')")
    @GetMapping("/recruiter/jobs")
    public PageResponse<JobResponse> myJobs(Authentication auth,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return jobService.myJobs(auth.getName(), page, size);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Recruiter: post a job")
    @PreAuthorize("hasRole('RECRUITER')")
    @PostMapping("/jobs")
    @ResponseStatus(HttpStatus.CREATED)
    public JobResponse create(@Valid @RequestBody JobRequest request, Authentication auth) {
        return jobService.create(request, auth.getName());
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Recruiter: update my job")
    @PreAuthorize("hasRole('RECRUITER')")
    @PutMapping("/jobs/{id}")
    public JobResponse update(@PathVariable Long id, @Valid @RequestBody JobRequest request, Authentication auth) {
        return jobService.update(id, request, auth.getName());
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Recruiter: delete my job")
    @PreAuthorize("hasRole('RECRUITER')")
    @DeleteMapping("/jobs/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication auth) {
        jobService.delete(id, auth.getName());
    }
}
