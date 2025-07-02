package com.codestorykh.alpha.oauth2.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class JwtTokenService {

    private static final String SECRET = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET));
    private static final long EXPIRATION_TIME = 3600000; // 1 hour

    public String generateToken(String clientId, List<String> scopes, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("client_id", clientId);
        claims.put("scope", String.join(" ", scopes));
        claims.put("roles", roles);
        claims.put("iss", "alpha-identity-server");
        claims.put("aud", "alpha-clients");

        return Jwts.builder()
                .setSubject(clientId)
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            throw new RuntimeException("Invalid token");
        }
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getClientIdFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.get("client_id", String.class);
    }

    public List<String> getScopesFromToken(String token) {
        Claims claims = validateToken(token);
        String scopeString = claims.get("scope", String.class);
        return List.of(scopeString.split(" "));
    }

    public List<String> getRolesFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.get("roles", List.class);
    }
} 