package com.company.directory.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    void generatedPasswordHasExpectedLength() {
        assertEquals(14, PasswordUtil.generateReadablePassword().length());
    }

    @Test
    void generatedPasswordAvoidsAmbiguousCharacters() {
        for (int i = 0; i < 50; i++) {
            String pw = PasswordUtil.generateReadablePassword();
            for (char c : pw.toCharArray()) {
                assertTrue("O0Il1".indexOf(c) < 0, "ambiguous char: " + c);
            }
        }
    }

    @Test
    void generatedPasswordsAreNotAllIdentical() {
        String first = PasswordUtil.generateReadablePassword();
        boolean sawDifferent = false;
        for (int i = 0; i < 20 && !sawDifferent; i++) {
            sawDifferent = !PasswordUtil.generateReadablePassword().equals(first);
        }
        assertTrue(sawDifferent);
    }

    @Test
    void hashIsVerifiable() {
        String hash = PasswordUtil.hash("secret123");
        assertNotEquals("secret123", hash);
        assertTrue(PasswordUtil.matches("secret123", hash));
        assertFalse(PasswordUtil.matches("wrong", hash));
    }
}
