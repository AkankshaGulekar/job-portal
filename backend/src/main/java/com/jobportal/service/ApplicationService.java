package com.jobportal.service;

import com.jobportal.dto.ApplicationResponse;
import com.jobportal.dto.PageResponse;
import com.jobportal.entity.Application;
import com.jobportal.entity.ApplicationStatus;
import com.jobportal.entity.Job;
import com.jobportal.entity.User;
import com.jobportal.exception.BadRequestException;
import com.jobportal.exception.ConflictException;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.repository.ApplicationRepository;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public ApplicationResponse apply(Long jobId, String candidateEmail, MultipartFile resume) {
        User candidate = findUser(candidateEmail);
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        if (applicationRepository.existsByJobIdAndCandidateId(jobId, candidate.getId())) {
            throw new ConflictException("You have already applied to this job");
        }
        Application application = new Application();
        application.setJob(job);
        application.setCandidate(candidate);
        application.setStatus(ApplicationStatus.APPLIED);
        application.setResumePath(fileStorageService.store(resume));
        return ApplicationResponse.from(applicationRepository.save(application));
    }

    @Transactional(readOnly = true)
    public PageResponse<ApplicationResponse> myApplications(String candidateEmail, int page, int size) {
        User candidate = findUser(candidateEmail);
        return PageResponse.from(applicationRepository
                .findByCandidateId(candidate.getId(), pageable(page, size)).map(ApplicationResponse::from));
    }

    @Transactional(readOnly = true)
    public PageResponse<ApplicationResponse> jobApplications(Long jobId, ApplicationStatus status,
                                                             String recruiterEmail, int page, int size) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        if (!job.getRecruiter().getEmail().equals(recruiterEmail)) {
            throw new AccessDeniedException("Not the owner of this job");
        }
        Pageable pageable = pageable(page, size);
        var result = (status == null)
                ? applicationRepository.findByJobId(jobId, pageable)
                : applicationRepository.findByJobIdAndStatus(jobId, status, pageable);
        return PageResponse.from(result.map(ApplicationResponse::from));
    }

    public ApplicationResponse updateStatus(Long applicationId, ApplicationStatus newStatus, String recruiterEmail) {
        if (newStatus == ApplicationStatus.APPLIED) {
            throw new BadRequestException("Status can only be changed to SHORTLISTED or REJECTED");
        }
        Application application = find(applicationId);
        if (!application.getJob().getRecruiter().getEmail().equals(recruiterEmail)) {
            throw new AccessDeniedException("Not the owner of this job");
        }
        application.setStatus(newStatus);
        return ApplicationResponse.from(application);
    }

    @Transactional(readOnly = true)
    public Resource getResume(Long applicationId, String requesterEmail) {
        Application application = find(applicationId);
        boolean isCandidate = application.getCandidate().getEmail().equals(requesterEmail);
        boolean isRecruiter = application.getJob().getRecruiter().getEmail().equals(requesterEmail);
        if (!isCandidate && !isRecruiter) {
            throw new AccessDeniedException("Not allowed to view this resume");
        }
        return fileStorageService.load(application.getResumePath());
    }

    private Application find(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Pageable pageable(int page, int size) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50),
                Sort.by(Sort.Direction.DESC, "appliedAt"));
    }
}
