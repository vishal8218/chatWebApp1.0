package com.ChatApp.Services;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Key;

@Service
public class JwtUtil {

    private static final String SECRET_KEY = "i6vND7mwdIj0cvH+slvZFxNFa0S+E43fY4hzPp4zQAk="; // Must be 256 bits for HS256
    private static final long EXPIRATION_TIME = 10800000; // 3 hours
 
    @Autowired
   private static FirebaseConfiguration fbc;
    private static final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    public  String generateToken(String username) {
    	return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public 
    String extractUsername(String token) {
    	 return Jwts.parserBuilder()
    	        .setSigningKey(key)
    	        .build()
    	        .parseClaimsJws(token)
    	        .getBody()
    	        .getSubject();
      
    }

    public static boolean validateToken(String token) throws IllegalArgumentException, InterruptedException, ExecutionException  {
        try {
        	
       
        		Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
                return true;
        	
        	
        } catch (JwtException e) {
            return false;
        }
    }

     
}
