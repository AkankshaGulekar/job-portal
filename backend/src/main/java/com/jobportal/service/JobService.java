package com.jobportal.service;

import com.jobportal.dto.JobRequest;
import com.jobportal.dto.JobResponse;
import com.jobportal.dto.PageResponse;
import com.jobportal.entity.Job;
import com.jobportal.entity.JobType;
import com.jobportal.entity.User;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.repository.ApplicationRepository;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class JobService {

    private static final int MAX_PAGE_SIZE = 50;

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;

    @Transactional(readOnly = true)
    public PageResponse<JobResponse> search(String keyword, String location, JobType jobType, int page, int size) {
        return PageResponse.from(
                jobRepository.search(blankToNull(keyword), blankToNull(location), jobType, pageable(page, size))
                        .map(JobResponse::from));
    }

    @Transactional(readOnly = true)
    public JobResponse get(Long id) {
        return JobResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<JobResponse> myJobs(String recruiterEmail, int page, int size) {
        User recruiter = findUser(recruiterEmail);
        return PageResponse.from(
                jobRepository.findByRecruiterId(recruiter.getId(), pageable(page, size)).map(JobResponse::from));
    }

    public JobResponse create(JobRequest req, String recruiterEmail) {
        Job job = new Job();
        job.setRecruiter(findUser(recruiterEmail));
        apply(job, req);
        return JobResponse.from(jobRepository.save(job));
    }

    public JobResponse update(Long id, JobRequest req, String recruiterEmail) {
        Job job = findOwned(id, recruiterEmail);
        apply(job, req);
        return JobResponse.from(jobRepository.save(job));
    }

    public void delete(Long id, String recruiterEmail) {
        Job job = findOwned(id, recruiterEmail);
        applicationRepository.deleteByJobId(job.getId());
        jobRepository.delete(job);
    }

    // ---- helpers ----
    private Job findOwned(Long id, String email) {
        Job job = find(id);
        if (!job.getRecruiter().getEmail().equals(email)) {
            throw new AccessDeniedException("Not the owner of this job");
        }
        return job;
    }

    private Job find(Long id) {
        return jobRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Job not found"));
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void apply(Job job, JobRequest req) {
        job.setTitle(req.title().trim());
        job.setDescription(req.description().trim());
        job.setCompany(req.company().trim());
        job.setLocation(req.location().trim());
        job.setSalary(req.salary());
        job.setJobType(req.jobType());
    }

    private Pageable pageable(int page, int size) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
