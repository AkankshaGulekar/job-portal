package com.jobportal.dto;

import com.jobportal.entity.Job;
import com.jobportal.entity.JobType;

import java.time.LocalDateTime;

public record JobResponse(Long id, String title, String description, String company, String location,
                          Integer salary, JobType jobType, String recruiterName, LocalDateTime createdAt) {

    public static JobResponse from(Job j) {
        return new JobResponse(j.getId(), j.getTitle(), j.getDescription(), j.getCompany(), j.getLocation(),
                j.getSalary(), j.getJobType(), j.getRecruiter().getName(), j.getCreatedAt());
    }
}
