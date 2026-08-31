package com.mazadhub.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

// Hashes and checks passwords with PBKDF2 (HMAC-SHA256) and a random salt
// Uses only the JDK, so the project needs no extra security library
public final class PasswordHasher
{
    private static final String ALGORITHM="PBKDF2WithHmacSHA256";
    private static final int ITERATIONS=120_000;
    private static final int KEY_BITS=256;
    private static final int SALT_BYTES=16;
    private static final SecureRandom RNG=new SecureRandom();

    // Utility class, never instantiated
    private PasswordHasher()
    {
    }

    // Hashes a new password and returns "iterations:salt:hash" for storing in the users table
    public static String hash(String password)
    {
        byte[] salt=new byte[SALT_BYTES];
        RNG.nextBytes(salt);
        byte[] dk=pbkdf2(password.toCharArray(), salt, ITERATIONS, KEY_BITS);
        return ITERATIONS+":"+b64(salt)+":"+b64(dk);
    }

    // Re-hashes the typed password with the stored salt and compares the result
    public static boolean verify(String password, String stored)
    {
        if(stored==null)
        {
            return false;
        }

        String[] parts=stored.split(":");
        if(parts.length!=3)
        {
            return false;
        }

        int iterations=Integer.parseInt(parts[0]);
        byte[] salt=unb64(parts[1]);
        byte[] expected=unb64(parts[2]);
        byte[] actual=pbkdf2(password.toCharArray(), salt, iterations, expected.length*8);
        return constantTimeEquals(actual, expected);
    }

    // Runs the actual key-derivation function
    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyBits)
    {
        try
        {
            PBEKeySpec spec=new PBEKeySpec(password, salt, iterations, keyBits);
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        }
        catch(NoSuchAlgorithmException|InvalidKeySpecException e)
        {
            throw new IllegalStateException("Password hashing failed", e);
        }
    }

    // Compares all bytes every time, so the answer time leaks nothing about the password
    private static boolean constantTimeEquals(byte[] a, byte[] b)
    {
        if(a.length!=b.length)
        {
            return false;
        }

        int diff=0;
        for(int i=0; i<a.length; i++)
        {
            diff|=a[i]^b[i];
        }

        return diff==0;
    }

    // Bytes to Base64 text
    private static String b64(byte[] data)
    {
        return Base64.getEncoder().encodeToString(data);
    }

    // Base64 text back to bytes
    private static byte[] unb64(String s)
    {
        return Base64.getDecoder().decode(s);
    }
}
