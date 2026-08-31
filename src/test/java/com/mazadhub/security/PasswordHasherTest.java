package com.mazadhub.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Unit tests for password hashing and verification
class PasswordHasherTest
{
    // The stored value is a salted hash, not the password itself
    @Test
    void hashIsNotThePlaintext()
    {
        String hash=PasswordHasher.hash("s3cret!");
        assertNotEquals("s3cret!", hash);
        assertTrue(hash.contains(":"));
    }

    // The right password verifies against its own hash
    @Test
    void verifyAcceptsCorrectPassword()
    {
        String hash=PasswordHasher.hash("correct horse battery staple");
        assertTrue(PasswordHasher.verify("correct horse battery staple", hash));
    }

    // A different password does not verify
    @Test
    void verifyRejectsWrongPassword()
    {
        String hash=PasswordHasher.hash("correct horse battery staple");
        assertFalse(PasswordHasher.verify("Tr0ub4dor&3", hash));
    }

    // A fresh random salt makes two hashes of the same password differ
    @Test
    void samePasswordHashesDifferentlyEachTime()
    {
        // Different random salts -> different stored values.
        assertNotEquals(PasswordHasher.hash("same"), PasswordHasher.hash("same"));
    }

    // A damaged or missing stored value is refused instead of crashing
    @Test
    void verifyRejectsMalformedHash()
    {
        assertFalse(PasswordHasher.verify("whatever", "not-a-valid-hash"));
        assertFalse(PasswordHasher.verify("whatever", null));
    }
}
