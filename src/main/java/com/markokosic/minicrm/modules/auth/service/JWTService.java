package com.markokosic.minicrm.modules.auth.service;


import com.markokosic.minicrm.modules.user.User;
import com.markokosic.minicrm.modules.user.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Function;


@Service
public class JWTService {

    @Value("${jwt.secret}")
    private String secretKey;

    private final UserRepository userRepository;

    public JWTService(UserRepository userRepository ){
        this.userRepository = userRepository;
    }

    public String generateToken(String email, Long tenantId, Long expirationInMinutes) {

        Map<String, Object> claims = new HashMap<>();
            claims.put("tenantId", tenantId);

        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 60 * 1000 * expirationInMinutes))
                .and()
                .signWith(getKey())
                .compact();

    }

    private SecretKey getKey() {
        byte[] keyByes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyByes);
    }

    public String extractEmail(String token) {
        // extract the username from jwt token
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractTenantId(String token) {
        return extractClaim(token, claims -> claims.get("tenantId", Long.class ));
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        //TODO REFACTOR TO SHOW EMAIL NOT USERNAME
        if (!isTokenSigned(token)) {
            return false;
        }

        if (isTokenExpired(token)) {
            return false;
        }

        final String userEmail = extractEmail(token);
        return ( userEmail.equals(userDetails.getUsername())  );
    }

    public boolean validateRefreshToken(String token) {
        if (!isTokenSigned(token)) {
            return false;
        }

        String email = extractEmail(token);
        if (isTokenExpired(token)) {
            return false;
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return false;
        }

        User user = userOptional.get();
        return email.equals(user.getEmail());
    }

    public boolean isTokenSigned(String token) {
        try {
            Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException e){
            return false;
        }
    }


    public boolean isTokenExpired(String token) {
            return extractAllClaims(token).getExpiration().before(new Date());
    }


}
