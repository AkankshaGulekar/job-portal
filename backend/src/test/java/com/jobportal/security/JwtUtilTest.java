package com.jobportal.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private final JwtUtil jwtUtil = new JwtUtil("unit-test-secret-key-unit-test-secret-key-123", 60_000);

    @Test
    void generatedTokenIsValidAndContainsEmail() {
        String token = jwtUtil.generateToken("a@b.com", "CANDIDATE");
        assertTrue(jwtUtil.isValid(token));
        assertEquals("a@b.com", jwtUtil.extractEmail(token));
    }

    @Test
    void garbageTokenIsInvalid() {
        assertFalse(jwtUtil.isValid("not-a-jwt"));
    }

    @Test
    void expiredTokenIsInvalid() {
        JwtUtil expired = new JwtUtil("unit-test-secret-key-unit-test-secret-key-123", -1000);
        assertFalse(expired.isValid(expired.generateToken("a@b.com", "CANDIDATE")));
    }

    @Test
    void tokenSignedWithDifferentKeyIsInvalid() {
        JwtUtil other = new JwtUtil("another-secret-key-another-secret-key-another-1", 60_000);
        assertFalse(jwtUtil.isValid(other.generateToken("a@b.com", "CANDIDATE")));
    }
}
