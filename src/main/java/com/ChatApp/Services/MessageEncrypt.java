package com.ChatApp.Services;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

@Service
public class MessageEncrypt {
	
//	  private static final String AES = "AES";
//	    private static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";
//	    private static final int AES_KEY_SIZE = 256;
//	    private static final int GCM_TAG_LENGTH = 128;
//	    private static final int IV_LENGTH = 12; 
//       public  SecretKey key;
//	    
//	    public  MessageEncrypt () throws Exception {
//	        KeyGenerator keyGen = KeyGenerator.getInstance(AES);
//	        keyGen.init(AES_KEY_SIZE);
//	        this.key=keyGen.generateKey();
//	    }
//
//	    // Encrypt a plain message with AES GCM
//	    public  String encrypt(String message) throws Exception {
//	        byte[] iv = new byte[IV_LENGTH];
//	        new SecureRandom().nextBytes(iv); // generate random IV
//
//	        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
//	        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
//	        cipher.init(Cipher.ENCRYPT_MODE, key, spec);
//
//	        byte[] encrypted = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
//
//	        // Prepend IV to ciphertext
//	        byte[] encryptedWithIv = new byte[IV_LENGTH + encrypted.length];
//	        System.arraycopy(iv, 0, encryptedWithIv, 0, IV_LENGTH);
//	        System.arraycopy(encrypted, 0, encryptedWithIv, IV_LENGTH, encrypted.length);
//
//	        return Base64.getEncoder().encodeToString(encryptedWithIv);
//	    }
//
//	    // Decrypt the encrypted message
//	    public  String decrypt(String base64Ciphertext) throws Exception {
//	        byte[] encryptedWithIv = Base64.getDecoder().decode(base64Ciphertext);
//
//	        byte[] iv = new byte[IV_LENGTH];
//	        byte[] encrypted = new byte[encryptedWithIv.length - IV_LENGTH];
//	        System.arraycopy(encryptedWithIv, 0, iv, 0, IV_LENGTH);
//	        System.arraycopy(encryptedWithIv, IV_LENGTH, encrypted, 0, encrypted.length);
//
//	        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
//	        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
//	        cipher.init(Cipher.DECRYPT_MODE, key, spec);
//
//	        byte[] decrypted = cipher.doFinal(encrypted);
//	        return new String(decrypted, StandardCharsets.UTF_8);
//	    }
//
	
	  private static final  String ALGORITHM = "AES/CBC/PKCS5Padding";
	  private static final	    String SECRET_KEY = "1234567812345678"; // 16-char = 128-bit key
	   
	  public  String encrypt(String plainText) {
	        try {
	            SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
	            Cipher cipher = Cipher.getInstance("AES"); // ECB mode
	            cipher.init(Cipher.ENCRYPT_MODE, key);

	            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
	            return Base64.getEncoder().encodeToString(encrypted);

	        } catch (Exception e) {
	            throw new RuntimeException("Encryption failed: " + e.getMessage());
	        }
	    }

	    public  String decrypt(String cipherText) {
	        try {
	            SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
	            Cipher cipher = Cipher.getInstance("AES");
	            cipher.init(Cipher.DECRYPT_MODE, key);

	            byte[] decoded = Base64.getDecoder().decode(cipherText);
	            byte[] original = cipher.doFinal(decoded);
	            return new String(original, StandardCharsets.UTF_8);

	        } catch (Exception e) {
	            throw new RuntimeException("Decryption failed: " + e.getMessage());
	        }
	    }

}
