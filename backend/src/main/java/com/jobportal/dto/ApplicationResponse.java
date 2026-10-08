package com.jobportal.dto;

import com.jobportal.entity.Application;
import com.jobportal.entity.ApplicationStatus;

import java.time.LocalDateTime;

public record ApplicationResponse(Long id, Long jobId, String jobTitle, String company, String candidateName,
                                  String candidateEmail, ApplicationStatus status, LocalDateTime appliedAt,
                                  String resumeUrl) {

    public static ApplicationResponse from(Application a) {
        return new ApplicationResponse(a.getId(), a.getJob().getId(), a.getJob().getTitle(),
                a.getJob().getCompany(), a.getCandidate().getName(), a.getCandidate().getEmail(),
                a.getStatus(), a.getAppliedAt(), "/api/applications/" + a.getId() + "/resume");
    }
}
