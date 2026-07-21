package com.ChatApp.Services;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

@Service
public class PasswordUtil {

	  private static final  String ALGORITHM = "AES/CBC/PKCS5Padding";
	  private static final	    String SECRET_KEY = "1234567812345678"; // 16-char = 128-bit key
	  private static final  String INIT_VECTOR = "1234567812345678"; // 16-char IV used in CryptoJS
	   
	  public  String encryptPassword(String plainText) {
	        try {
	            SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
	            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding"); // ECB mode
	            cipher.init(Cipher.ENCRYPT_MODE, key);

	            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
	            return Base64.getEncoder().encodeToString(encrypted);

	        } catch (Exception e) {
	            throw new RuntimeException("Encryption failed: " + e.getMessage());
	        }
	    }

	    public  String decryptPassword(String cipherText) {
	        try {
	            SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
	            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
	            cipher.init(Cipher.DECRYPT_MODE, key);

	            byte[] decoded = Base64.getDecoder().decode(cipherText);
	            byte[] original = cipher.doFinal(decoded);
	            return new String(original, StandardCharsets.UTF_8);

	        } catch (Exception e) {
	            throw new RuntimeException("Decryption failed: " + e.getMessage());
	        }
	    }


}
