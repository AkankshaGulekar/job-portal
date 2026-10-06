package com.jobportal.repository;

import com.jobportal.entity.Job;
import com.jobportal.entity.JobType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JobRepository extends JpaRepository<Job, Long> {

    @EntityGraph(attributePaths = "recruiter")
    @Query("""
            select j from Job j
            where (:keyword is null
                   or lower(j.title) like lower(concat('%', :keyword, '%'))
                   or lower(j.company) like lower(concat('%', :keyword, '%')))
              and (:location is null or lower(j.location) like lower(concat('%', :location, '%')))
              and (:jobType is null or j.jobType = :jobType)
            """)
    Page<Job> search(@Param("keyword") String keyword,
                     @Param("location") String location,
                     @Param("jobType") JobType jobType,
                     Pageable pageable);

    @EntityGraph(attributePaths = "recruiter")
    Page<Job> findByRecruiterId(Long recruiterId, Pageable pageable);
}
