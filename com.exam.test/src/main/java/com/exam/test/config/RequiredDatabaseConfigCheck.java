package com.exam.test.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Fails startup with a clear message when the database settings are missing, instead of a
 * confusing Hibernate/Hikari error. There are no default credentials: they must come from the
 * DB_URL, DB_USER and DB_PASSWORD environment variables or from the "dev" profile.
 */
public class RequiredDatabaseConfigCheck implements EnvironmentPostProcessor, Ordered {

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		List<String> missing = new ArrayList<>();
		check(environment, "spring.datasource.url", true, missing);
		check(environment, "spring.datasource.username", true, missing);
		// An empty password is allowed (some databases have none); an unset one is not
		check(environment, "spring.datasource.password", false, missing);

		if (!missing.isEmpty()) {
			throw new IllegalStateException("Database configuration is missing: " + String.join(", ", missing)
					+ ". Set the DB_URL, DB_USER and DB_PASSWORD environment variables, "
					+ "or activate the 'dev' profile for local development (--spring.profiles.active=dev).");
		}
	}

	private void check(ConfigurableEnvironment environment, String key, boolean notBlank, List<String> missing) {
		try {
			String value = environment.getProperty(key);
			if (value == null || (notBlank && value.isBlank())) {
				missing.add(key);
			}
		} catch (IllegalArgumentException unresolvedPlaceholder) {
			// e.g. ${DB_PASSWORD} with no DB_PASSWORD variable set
			missing.add(key);
		}
	}

	/** Run after the configuration files (and profiles) have been loaded. */
	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}
}
