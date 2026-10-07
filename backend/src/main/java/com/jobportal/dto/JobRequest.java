package com.jobportal.dto;

import com.jobportal.entity.JobType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record JobRequest(
        @NotBlank String title,
        @NotBlank @Size(max = 5000) String description,
        @NotBlank String company,
        @NotBlank String location,
        @PositiveOrZero Integer salary,
        @NotNull JobType jobType) {
}
