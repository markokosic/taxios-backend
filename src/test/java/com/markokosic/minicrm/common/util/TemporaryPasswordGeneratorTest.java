package com.markokosic.minicrm.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TemporaryPasswordGeneratorTest {

    @Test
    void testGenerate_defaultLength() {
        String password = TemporaryPasswordGenerator.generate();
        assertNotNull(password);
        assertEquals(12, password.length());

        // Verify at least one uppercase, lowercase, digit, special character
        assertTrue(password.chars().anyMatch(Character::isUpperCase), "Should contain uppercase character");
        assertTrue(password.chars().anyMatch(Character::isLowerCase), "Should contain lowercase character");
        assertTrue(password.chars().anyMatch(Character::isDigit), "Should contain digit");
        assertTrue(password.chars().anyMatch(ch -> "!@#$%^&*()_-+=".indexOf(ch) >= 0), "Should contain special character");
    }

    @Test
    void testGenerate_customLength() {
        String password = TemporaryPasswordGenerator.generate(16);
        assertNotNull(password);
        assertEquals(16, password.length());
    }

    @Test
    void testGenerate_tooShortThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> TemporaryPasswordGenerator.generate(7));
    }
}
