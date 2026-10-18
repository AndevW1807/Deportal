package com.deportal.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.time.Clock;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;

import tools.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	Clock clock() {
		return Clock.systemUTC();
	}

	@Bean
	LoginRateLimiter loginRateLimiter() {
		return new LoginRateLimiter();
	}

	@Bean
	LoginRateLimitFilter loginRateLimitFilter(LoginRateLimiter limiter, ObjectMapper objectMapper) {
		return new LoginRateLimitFilter(limiter, objectMapper);
	}

	@Bean
	JwtEncoder jwtEncoder(@Value("${jwt.secret}") String secret) {
		OctetSequenceKey key = new OctetSequenceKey.Builder(secret.getBytes(StandardCharsets.UTF_8))
			.keyID("deportal-hs256")
			.algorithm(JWSAlgorithm.HS256)
			.build();
		return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(key)));
	}

	@Bean
	JwtDecoder jwtDecoder(@Value("${jwt.secret}") String secret, @Value("${jwt.issuer}") String issuer) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey(secret)).build();
		OAuth2TokenValidator<Jwt> validator = token -> {
			OAuth2TokenValidator<Jwt> timestamp = new JwtTimestampValidator();
			var timestampResult = timestamp.validate(token);
			if (timestampResult.hasErrors()) {
				return timestampResult;
			}
			return new JwtIssuerValidator(issuer).validate(token);
		};
		decoder.setJwtValidator(validator);
		return decoder;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, LoginRateLimitFilter rateLimit,
			ObjectMapper objectMapper) throws Exception {
		ProblemAuthenticationEntryPoint entryPoint = new ProblemAuthenticationEntryPoint(objectMapper);
		http
			// Bearer tokens are not cookies, so a CSRF token would not protect this API.
			.csrf(csrf -> csrf.disable())
			.httpBasic(basic -> basic.disable())
			.formLogin(form -> form.disable())
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register").permitAll()
				.anyRequest().authenticated())
			.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()).authenticationEntryPoint(entryPoint))
			.exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(entryPoint))
			.addFilterBefore(rateLimit, BearerTokenAuthenticationFilter.class);
		return http.build();
	}

	private static SecretKey secretKey(String secret) {
		return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
	}

}
