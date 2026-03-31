package com.banking.banking_api.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private  String secretKey;

    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey getSigningkey(){
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);

    }

    private Claims extractClaims(String token){
        return Jwts.parser()
                .verifyWith(getSigningkey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String generateToken(String email, String role){
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningkey())
                .compact();
    }

    public String extractEmail(String token){
        return extractClaims(token).getSubject();
    }

    public String getRoleFromToken(String token) {
        return extractClaims(token).get("role", String.class);
    }

    public boolean validateToken(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("Token expired");
        } catch (MalformedJwtException e) {
            System.out.println("Token is invalid");
        } catch (UnsupportedJwtException e) {
            System.out.println("Token not supported");
        } catch (IllegalArgumentException e) {
            System.out.println("Token is empty");
        }
        return false;
    }



}
