package com.codestorykh.alpha.utils.encryption;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
public class EncryptionUtils {

    private static final String AES_ALGORITHM = "AES";
    private static final String AES_GCM_ALGORITHM = "AES/GCM/NoPadding";
    private static final String SHA_256_ALGORITHM = "SHA-256";
    private static final String SHA_512_ALGORITHM = "SHA-512";
    private static final String MD5_ALGORITHM = "MD5";
    
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;
    private static final int AES_KEY_SIZE = 256;

    /**
     * Generate a random AES key
     */
    public static SecretKey generateAESKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(AES_ALGORITHM);
            keyGen.init(AES_KEY_SIZE);
            return keyGen.generateKey();
        } catch (Exception e) {
            log.error("Failed to generate AES key", e);
            throw new RuntimeException("Failed to generate AES key", e);
        }
    }

    /**
     * Generate a random AES key and return as Base64 string
     */
    public static String generateAESKeyAsString() {
        SecretKey key = generateAESKey();
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    /**
     * Convert Base64 string to SecretKey
     */
    public static SecretKey stringToSecretKey(String keyString) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(keyString);
            return new SecretKeySpec(keyBytes, AES_ALGORITHM);
        } catch (Exception e) {
            log.error("Failed to convert string to SecretKey", e);
            throw new RuntimeException("Failed to convert string to SecretKey", e);
        }
    }

    /**
     * Encrypt text using AES GCM
     */
    public static String encryptAES(String plaintext, String keyString) {
        try {
            SecretKey key = stringToSecretKey(keyString);
            return encryptAES(plaintext, key);
        } catch (Exception e) {
            log.error("Failed to encrypt text", e);
            throw new RuntimeException("Failed to encrypt text", e);
        }
    }

    /**
     * Encrypt text using AES GCM
     */
    public static String encryptAES(String plaintext, SecretKey key) {
        try {
            byte[] iv = generateIV();
            Cipher cipher = Cipher.getInstance(AES_GCM_ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);
            
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] encrypted = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, encrypted, 0, iv.length);
            System.arraycopy(ciphertext, 0, encrypted, iv.length, ciphertext.length);
            
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("Failed to encrypt text", e);
            throw new RuntimeException("Failed to encrypt text", e);
        }
    }

    /**
     * Decrypt text using AES GCM
     */
    public static String decryptAES(String encryptedText, String keyString) {
        try {
            SecretKey key = stringToSecretKey(keyString);
            return decryptAES(encryptedText, key);
        } catch (Exception e) {
            log.error("Failed to decrypt text", e);
            throw new RuntimeException("Failed to decrypt text", e);
        }
    }

    /**
     * Decrypt text using AES GCM
     */
    public static String decryptAES(String encryptedText, SecretKey key) {
        try {
            byte[] encrypted = Base64.getDecoder().decode(encryptedText);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] ciphertext = new byte[encrypted.length - GCM_IV_LENGTH];
            
            System.arraycopy(encrypted, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(encrypted, GCM_IV_LENGTH, ciphertext, 0, ciphertext.length);
            
            Cipher cipher = Cipher.getInstance(AES_GCM_ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
            
            byte[] decrypted = cipher.doFinal(ciphertext);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Failed to decrypt text", e);
            throw new RuntimeException("Failed to decrypt text", e);
        }
    }

    /**
     * Generate SHA-256 hash
     */
    public static String generateSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA_256_ALGORITHM);
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not available", e);
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Generate SHA-512 hash
     */
    public static String generateSHA512(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA_512_ALGORITHM);
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-512 algorithm not available", e);
            throw new RuntimeException("SHA-512 algorithm not available", e);
        }
    }

    /**
     * Generate MD5 hash
     */
    public static String generateMD5(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance(MD5_ALGORITHM);
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("MD5 algorithm not available", e);
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }

    /**
     * Generate hash with salt
     */
    public static String generateHashWithSalt(String input, String salt) {
        return generateSHA256(input + salt);
    }

    /**
     * Generate random salt
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Verify hash with salt
     */
    public static boolean verifyHashWithSalt(String input, String salt, String hash) {
        String computedHash = generateHashWithSalt(input, salt);
        return computedHash.equals(hash);
    }

    /**
     * Convert bytes to hexadecimal string
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    /**
     * Convert hexadecimal string to bytes
     */
    public static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Generate random IV
     */
    private static byte[] generateIV() {
        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[GCM_IV_LENGTH];
        random.nextBytes(iv);
        return iv;
    }

    /**
     * Encrypt sensitive data for storage
     */
    public static String encryptSensitiveData(String data, String keyString) {
        if (data == null || data.isEmpty()) {
            return data;
        }
        return encryptAES(data, keyString);
    }

    /**
     * Decrypt sensitive data from storage
     */
    public static String decryptSensitiveData(String encryptedData, String keyString) {
        if (encryptedData == null || encryptedData.isEmpty()) {
            return encryptedData;
        }
        return decryptAES(encryptedData, keyString);
    }

    /**
     * Generate secure random bytes
     */
    public static byte[] generateRandomBytes(int length) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return bytes;
    }

    /**
     * Generate secure random string
     */
    public static String generateRandomString(int length) {
        byte[] bytes = generateRandomBytes(length);
        return Base64.getEncoder().encodeToString(bytes).replace("+", "-").replace("/", "_").replace("=", "");
    }

    /**
     * Check if string is encrypted (Base64 format)
     */
    public static boolean isEncrypted(String data) {
        if (data == null || data.isEmpty()) {
            return false;
        }
        try {
            Base64.getDecoder().decode(data);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Generate password hash with salt
     */
    public static String hashPassword(String password) {
        String salt = generateSalt();
        String hash = generateHashWithSalt(password, salt);
        return salt + ":" + hash;
    }

    /**
     * Verify password
     */
    public static boolean verifyPassword(String password, String storedHash) {
        if (password == null || storedHash == null) {
            return false;
        }
        
        String[] parts = storedHash.split(":");
        if (parts.length != 2) {
            return false;
        }
        
        String salt = parts[0];
        String hash = parts[1];
        
        return verifyHashWithSalt(password, salt, hash);
    }
} 