package org.example.utilities;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.example.entities.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long ExpirationTime = 3600000;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String id, Role role, boolean isActive, boolean isDeleted){
        return Jwts.builder()
                .claim("id", id)
                .claim("role", role)
                .claim("isActive", isActive)
                .claim("isDeleted", isDeleted)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ExpirationTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

   public Claims getAllClaims(String token){
       return Jwts.parser()
               .verifyWith(key)
               .build()
               .parseSignedClaims(token)
               .getPayload();
   }

    public long getRemainingSeconds(String token) {
        Claims claims = getAllClaims(token);
        Date expiration = claims.getExpiration();
        return (expiration.getTime() - System.currentTimeMillis());
    }
}