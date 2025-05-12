package com.asmtunis.credix.backend.auth.service;

import com.asmtunis.credix.backend.auth.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Service
public class JwtService {
	private Key getSignInKey() {
		String SECRET_KEY = "my-secret-key-that-is-long-enough";
		byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
		return Keys.hmacShaKeyFor(keyBytes);
	}

	public String generateToken(User user) {
		long expirationMs = 86_400_000;
		return Jwts.builder()
			.setSubject(user.getEmail())
			.claim("role", user.getRole().name())
			.setIssuedAt(new Date())
			.setExpiration(new Date(System.currentTimeMillis() + expirationMs))
			.signWith(getSignInKey())
			.compact();
	}

	public String extractEmail(String token) {
		return Jwts.parserBuilder()
			.setSigningKey(getSignInKey())
			.build()
			.parseClaimsJws(token)
			.getBody()
			.getSubject();
	}

	public String extractRole(String token) {
		return Jwts.parserBuilder()
			.setSigningKey(getSignInKey())
			.build()
			.parseClaimsJws(token)
			.getBody()
			.get("role", String.class);
	}

	public boolean isTokenValid(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(getSignInKey()).build().parseClaimsJws(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
