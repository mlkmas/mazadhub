package com.mazadhub.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHasherTest {

    @Test
    void hashIsNotThePlaintext() {
        String hash = PasswordHasher.hash("s3cret!");
        assertNotEquals("s3cret!", hash);
        assertTrue(hash.contains(":"));
    }

    @Test
    void verifyAcceptsCorrectPassword() {
        String hash = PasswordHasher.hash("correct horse battery staple");
        assertTrue(PasswordHasher.verify("correct horse battery staple", hash));
    }

    @Test
    void verifyRejectsWrongPassword() {
        String hash = PasswordHasher.hash("correct horse battery staple");
        assertFalse(PasswordHasher.verify("Tr0ub4dor&3", hash));
    }

    @Test
    void samePasswordHashesDifferentlyEachTime() {
        // Different random salts -> different stored values.
        assertNotEquals(PasswordHasher.hash("same"), PasswordHasher.hash("same"));
    }

    @Test
    void verifyRejectsMalformedHash() {
        assertFalse(PasswordHasher.verify("whatever", "not-a-valid-hash"));
        assertFalse(PasswordHasher.verify("whatever", null));
    }
}
