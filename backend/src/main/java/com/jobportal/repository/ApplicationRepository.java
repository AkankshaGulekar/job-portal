package com.jobportal.repository;

import com.jobportal.entity.Application;
import com.jobportal.entity.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    boolean existsByJobIdAndCandidateId(Long jobId, Long candidateId);

    @EntityGraph(attributePaths = {"job", "candidate"})
    Page<Application> findByCandidateId(Long candidateId, Pageable pageable);

    @EntityGraph(attributePaths = {"job", "candidate"})
    Page<Application> findByJobId(Long jobId, Pageable pageable);

    @EntityGraph(attributePaths = {"job", "candidate"})
    Page<Application> findByJobIdAndStatus(Long jobId, ApplicationStatus status, Pageable pageable);

    void deleteByJobId(Long jobId);
}
