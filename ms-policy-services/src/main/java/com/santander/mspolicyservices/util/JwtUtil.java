package com.santander.mspolicyservices.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

@Component
@AllArgsConstructor
public class JwtUtil {

    private final Environment environment;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getKey() {
        String SECRET_KEY = "spring.security.oauth2.resourceserver.jwt.public-key-location";
        return Keys.hmacShaKeyFor(
                environment.getRequiredProperty(SECRET_KEY)
                        .getBytes(StandardCharsets.UTF_8)
        );
    }
}