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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock JobRepository jobRepository;
    @Mock UserRepository userRepository;
    @Mock ApplicationRepository applicationRepository;
    @InjectMocks JobService jobService;

    private User recruiter;
    private Job job;
    private final JobRequest request =
            new JobRequest("Java Dev", "Build APIs", "Acme", "Pune", 1200000, JobType.FULL_TIME);

    @BeforeEach
    void setUp() {
        recruiter = new User();
        recruiter.setId(1L);
        recruiter.setName("Rita");
        recruiter.setEmail("rita@acme.com");
        job = new Job();
        job.setId(10L);
        job.setTitle("Old");
        job.setDescription("Old");
        job.setCompany("Acme");
        job.setLocation("Pune");
        job.setJobType(JobType.FULL_TIME);
        job.setRecruiter(recruiter);
    }

    @Test
    void createAssignsRecruiterAndSaves() {
        when(userRepository.findByEmail("rita@acme.com")).thenReturn(Optional.of(recruiter));
        when(jobRepository.save(any(Job.class))).thenAnswer(i -> i.getArgument(0));

        JobResponse res = jobService.create(request, "rita@acme.com");

        assertEquals("Java Dev", res.title());
        assertEquals("Rita", res.recruiterName());
    }

    @Test
    void updateByOwnerChangesFields() {
        when(jobRepository.findById(10L)).thenReturn(Optional.of(job));
        when(jobRepository.save(any(Job.class))).thenAnswer(i -> i.getArgument(0));

        JobResponse res = jobService.update(10L, request, "rita@acme.com");

        assertEquals("Java Dev", res.title());
    }

    @Test
    void updateByAnotherRecruiterIsForbidden() {
        when(jobRepository.findById(10L)).thenReturn(Optional.of(job));
        assertThrows(AccessDeniedException.class, () -> jobService.update(10L, request, "evil@x.com"));
        verify(jobRepository, never()).save(any());
    }

    @Test
    void deleteRemovesApplicationsThenJob() {
        when(jobRepository.findById(10L)).thenReturn(Optional.of(job));
        jobService.delete(10L, "rita@acme.com");
        verify(applicationRepository).deleteByJobId(10L);
        verify(jobRepository).delete(job);
    }

    @Test
    void getMissingJobThrowsNotFound() {
        when(jobRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> jobService.get(99L));
    }

    @Test
    void searchTreatsBlankFiltersAsNull() {
        when(jobRepository.search(isNull(), isNull(), isNull(), any()))
                .thenReturn(new PageImpl<>(List.of(job)));

        PageResponse<JobResponse> res = jobService.search("  ", "", null, 0, 10);

        assertEquals(1, res.totalElements());
        assertEquals(1, res.content().size());
    }
}
