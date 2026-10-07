package com.jobportal.service;

import com.jobportal.dto.AuthResponse;
import com.jobportal.dto.LoginRequest;
import com.jobportal.dto.RegisterRequest;
import com.jobportal.entity.Role;
import com.jobportal.entity.User;
import com.jobportal.exception.ConflictException;
import com.jobportal.repository.UserRepository;
import com.jobportal.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuthenticationManager authenticationManager;
    @Mock JwtUtil jwtUtil;
    @InjectMocks AuthService authService;

    @Test
    void registerHashesPasswordAndReturnsToken() {
        when(userRepository.existsByEmail("john@x.com")).thenReturn(false);
        when(passwordEncoder.encode("secret1")).thenReturn("hashed");
        when(jwtUtil.generateToken("john@x.com", "RECRUITER")).thenReturn("tok");

        AuthResponse res = authService.register(new RegisterRequest("John", "John@X.com", "secret1", Role.RECRUITER));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("hashed", captor.getValue().getPassword());
        assertEquals("john@x.com", captor.getValue().getEmail());
        assertEquals("tok", res.token());
        assertEquals(Role.RECRUITER, res.role());
    }

    @Test
    void registerWithExistingEmailThrowsConflict() {
        when(userRepository.existsByEmail("john@x.com")).thenReturn(true);
        assertThrows(ConflictException.class,
                () -> authService.register(new RegisterRequest("John", "john@x.com", "secret1", Role.CANDIDATE)));
        verify(userRepository, never()).save(any());
    }

    @Test
    void loginReturnsTokenForValidCredentials() {
        User user = new User();
        user.setName("Jane");
        user.setEmail("jane@x.com");
        user.setRole(Role.CANDIDATE);
        when(userRepository.findByEmail("jane@x.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("jane@x.com", "CANDIDATE")).thenReturn("tok");

        AuthResponse res = authService.login(new LoginRequest("jane@x.com", "pw"));

        assertEquals("tok", res.token());
        verify(authenticationManager).authenticate(any());
    }
}
