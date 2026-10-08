package com.jobportal.service;

import com.jobportal.dto.ApplicationResponse;
import com.jobportal.entity.Application;
import com.jobportal.entity.ApplicationStatus;
import com.jobportal.entity.Job;
import com.jobportal.entity.User;
import com.jobportal.exception.BadRequestException;
import com.jobportal.exception.ConflictException;
import com.jobportal.repository.ApplicationRepository;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock ApplicationRepository applicationRepository;
    @Mock JobRepository jobRepository;
    @Mock UserRepository userRepository;
    @Mock FileStorageService fileStorageService;
    @Mock MultipartFile resume;
    @InjectMocks ApplicationService applicationService;

    private User recruiter;
    private User candidate;
    private Job job;
    private Application application;

    @BeforeEach
    void setUp() {
        recruiter = new User();
        recruiter.setId(1L);
        recruiter.setName("Rita");
        recruiter.setEmail("rita@acme.com");

        candidate = new User();
        candidate.setId(2L);
        candidate.setName("Carl");
        candidate.setEmail("carl@x.com");

        job = new Job();
        job.setId(10L);
        job.setTitle("Java Dev");
        job.setCompany("Acme");
        job.setRecruiter(recruiter);

        application = new Application();
        application.setId(5L);
        application.setJob(job);
        application.setCandidate(candidate);
        application.setResumePath("r.pdf");
    }

    @Test
    void applySavesApplicationWithAppliedStatus() {
        when(userRepository.findByEmail("carl@x.com")).thenReturn(Optional.of(candidate));
        when(jobRepository.findById(10L)).thenReturn(Optional.of(job));
        when(applicationRepository.existsByJobIdAndCandidateId(10L, 2L)).thenReturn(false);
        when(fileStorageService.store(resume)).thenReturn("r.pdf");
        when(applicationRepository.save(any(Application.class))).thenAnswer(i -> i.getArgument(0));

        ApplicationResponse res = applicationService.apply(10L, "carl@x.com", resume);

        assertEquals(ApplicationStatus.APPLIED, res.status());
        assertEquals("Java Dev", res.jobTitle());
    }

    @Test
    void applyTwiceThrowsConflictAndStoresNothing() {
        when(userRepository.findByEmail("carl@x.com")).thenReturn(Optional.of(candidate));
        when(jobRepository.findById(10L)).thenReturn(Optional.of(job));
        when(applicationRepository.existsByJobIdAndCandidateId(10L, 2L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> applicationService.apply(10L, "carl@x.com", resume));
        verify(fileStorageService, never()).store(any());
    }

    @Test
    void ownerRecruiterCanShortlist() {
        when(applicationRepository.findById(5L)).thenReturn(Optional.of(application));

        ApplicationResponse res = applicationService.updateStatus(5L, ApplicationStatus.SHORTLISTED, "rita@acme.com");

        assertEquals(ApplicationStatus.SHORTLISTED, res.status());
    }

    @Test
    void otherRecruiterCannotChangeStatus() {
        when(applicationRepository.findById(5L)).thenReturn(Optional.of(application));

        assertThrows(AccessDeniedException.class,
                () -> applicationService.updateStatus(5L, ApplicationStatus.REJECTED, "other@x.com"));
        assertEquals(ApplicationStatus.APPLIED, application.getStatus());
    }

    @Test
    void cannotMoveBackToApplied() {
        assertThrows(BadRequestException.class,
                () -> applicationService.updateStatus(5L, ApplicationStatus.APPLIED, "rita@acme.com"));
    }

    @Test
    void strangerCannotDownloadResume() {
        when(applicationRepository.findById(5L)).thenReturn(Optional.of(application));
        assertThrows(AccessDeniedException.class, () -> applicationService.getResume(5L, "stranger@x.com"));
    }
}
