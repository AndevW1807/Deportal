package com.deportal.infrastructure.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Loads database settings from a local {@code .env} file before Spring starts.
 * Values already present in the process environment are kept, and nothing read
 * here is written to the console.
 */
public final class EnvFileLoader {

	private static final String URL_KEY = "SPRING_DATASOURCE_URL";

	private static final Set<String> KEYS = Set.of(
			"DB_HOST",
			"DB_PORT",
			"DB_NAME",
			URL_KEY,
			"SPRING_DATASOURCE_USERNAME",
			"SPRING_DATASOURCE_PASSWORD",
			"JWT_SECRET");

	private static final String INCOMPLETE_CONFIGURATION =
			"Database configuration is incomplete. Set DB_NAME and SPRING_DATASOURCE_USERNAME.";

	private static final String INCOMPLETE_JWT =
			"JWT_SECRET is missing or shorter than 32 bytes.";

	private EnvFileLoader() {
	}

	public static void load() {
		Map<String, String> fromFile = readEnvFile();
		for (String key : KEYS) {
			if (isPresent(System.getenv(key))) {
				continue;
			}
			String value = fromFile.get(key);
			if (value != null) {
				System.setProperty(key, value);
			}
		}
		applyDatabaseUrl(fromFile);
		if (!isPresent(configuredValue(URL_KEY))
				|| !isPresent(configuredValue("SPRING_DATASOURCE_USERNAME"))) {
			throw new IllegalStateException(INCOMPLETE_CONFIGURATION);
		}
		requireJwtSecret();
	}

	private static void requireJwtSecret() {
		String secret = configuredValue("JWT_SECRET");
		if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
			throw new IllegalStateException(INCOMPLETE_JWT);
		}
	}

	private static void applyDatabaseUrl(Map<String, String> fromFile) {
		if (isPresent(System.getenv(URL_KEY))) {
			return;
		}
		String host = resolve("DB_HOST", fromFile);
		String port = resolve("DB_PORT", fromFile);
		String name = resolve("DB_NAME", fromFile);
		if (!isSafeHost(host) || !isSafePort(port) || !isSafeName(name)) {
			throw new IllegalStateException(INCOMPLETE_CONFIGURATION);
		}
		System.setProperty(URL_KEY, "jdbc:mysql://" + host + ":" + port + "/" + name);
	}

	private static String resolve(String key, Map<String, String> fromFile) {
		String fromEnvironment = System.getenv(key);
		if (isPresent(fromEnvironment)) {
			return fromEnvironment;
		}
		String fromProperty = System.getProperty(key);
		if (isPresent(fromProperty)) {
			return fromProperty;
		}
		return fromFile.get(key);
	}

	private static boolean isSafeHost(String host) {
		return host != null && host.matches("[A-Za-z0-9.-]+");
	}

	private static boolean isSafePort(String port) {
		if (port == null || !port.matches("\\d{1,5}")) {
			return false;
		}
		int value = Integer.parseInt(port);
		return value >= 1 && value <= 65535;
	}

	private static boolean isSafeName(String name) {
		return name != null && name.matches("[A-Za-z0-9_]+");
	}

	private static Map<String, String> readEnvFile() {
		Path envFile = Path.of(System.getProperty("user.dir"), ".env");
		if (!Files.isRegularFile(envFile)) {
			return Map.of();
		}
		try {
			Map<String, String> values = new HashMap<>();
			for (String line : Files.readAllLines(envFile)) {
				parseLine(line, values);
			}
			return values;
		}
		catch (IOException exception) {
			throw new IllegalStateException("Database configuration file could not be read.", exception);
		}
	}

	private static void parseLine(String line, Map<String, String> values) {
		String trimmed = line.trim();
		if (trimmed.isEmpty() || trimmed.startsWith("#")) {
			return;
		}
		if (trimmed.startsWith("export ")) {
			trimmed = trimmed.substring("export ".length()).trim();
		}
		int separator = trimmed.indexOf('=');
		if (separator <= 0) {
			return;
		}
		String key = trimmed.substring(0, separator).trim();
		if (!KEYS.contains(key)) {
			return;
		}
		values.put(key, unquote(trimmed.substring(separator + 1)));
	}

	private static String unquote(String value) {
		String trimmed = value.trim();
		if (trimmed.length() >= 2
				&& ((trimmed.startsWith("\"") && trimmed.endsWith("\""))
						|| (trimmed.startsWith("'") && trimmed.endsWith("'")))) {
			return trimmed.substring(1, trimmed.length() - 1);
		}
		return trimmed;
	}

	private static String configuredValue(String key) {
		String fromEnvironment = System.getenv(key);
		if (isPresent(fromEnvironment)) {
			return fromEnvironment;
		}
		return System.getProperty(key);
	}

	private static boolean isPresent(String value) {
		return value != null && !value.isBlank();
	}

}
