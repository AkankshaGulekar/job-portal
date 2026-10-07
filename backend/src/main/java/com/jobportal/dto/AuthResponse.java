package com.jobportal.dto;

import com.jobportal.entity.Role;

public record AuthResponse(String token, String name, String email, Role role) {
}
