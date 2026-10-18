package com.deportal.infrastructure.security;

import java.time.Clock;
import java.time.Instant;

import com.deportal.application.auth.AuthenticatedUser;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

@Component
public class TokenIssuer {

	private final JwtEncoder encoder;
	private final String issuer;
	private final long expirationSeconds;
	private final Clock clock;

	public TokenIssuer(JwtEncoder encoder, @Value("${jwt.issuer}") String issuer,
			@Value("${jwt.expiration-seconds}") long expirationSeconds, Clock clock) {
		this.encoder = encoder;
		this.issuer = issuer;
		this.expirationSeconds = expirationSeconds;
		this.clock = clock;
	}

	public IssuedToken issue(AuthenticatedUser user) {
		Instant issuedAt = clock.instant();
		JwtClaimsSet claims = JwtClaimsSet.builder()
			.issuer(issuer)
			.subject(Long.toString(user.id()))
			.claim("role", user.role().name())
			.claim("type", user.type().name())
			.issuedAt(issuedAt)
			.expiresAt(issuedAt.plusSeconds(expirationSeconds))
			.build();
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).keyId("deportal-hs256").build();
		String value = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
		return new IssuedToken(value, expirationSeconds);
	}

}
