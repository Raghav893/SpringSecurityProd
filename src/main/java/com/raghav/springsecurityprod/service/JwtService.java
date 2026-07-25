package com.raghav.springsecurityprod.service;

import com.raghav.springsecurityprod.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class JwtService {

    @Value("${jwt.access-secret}")
    private String  accessSecret;

    @Value("${jwt.access-expiry-ms}")
    private long accessexpiryMs;

    private SecretKey signingKey;

    @PostConstruct
    void init(){
        this.signingKey= Keys.hmacShaKeyFor(accessSecret.getBytes(StandardCharsets.UTF_8));
    }//Basically converting access key to signing key

    public String generateAccessToken(User user){
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email",user.getEmail())
                .claim("role",user.getRoles().stream().map(Enum::name).toList())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(accessexpiryMs)))
                .signWith(signingKey,Jwts.SIG.HS256)
                .compact();
    }
    public Claims parseAndValidate(String token){
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    public UUID extractUserId(Claims claims){
        return UUID.fromString(claims.getSubject());
    }
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(Claims claims) {
        return claims.get("roles", List.class);
    }


}
