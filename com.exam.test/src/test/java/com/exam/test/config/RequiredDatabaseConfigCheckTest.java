package com.exam.test.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class RequiredDatabaseConfigCheckTest {

	private final RequiredDatabaseConfigCheck check = new RequiredDatabaseConfigCheck();

	private void run(MockEnvironment env) {
		check.postProcessEnvironment(env, null);
	}

	private MockEnvironment complete() {
		return new MockEnvironment()
				.withProperty("spring.datasource.url", "jdbc:postgresql://db:5432/app")
				.withProperty("spring.datasource.username", "app")
				.withProperty("spring.datasource.password", "s3cret");
	}

	@Test
	@DisplayName("starts when url, username and password are all set")
	void allSet_ok() {
		assertDoesNotThrow(() -> run(complete()));
	}

	@Test
	@DisplayName("fails and names every setting when the placeholders cannot be resolved (env vars not set)")
	void unresolvedPlaceholders_fail() {
		MockEnvironment env = new MockEnvironment()
				.withProperty("spring.datasource.url", "${DB_URL}")
				.withProperty("spring.datasource.username", "${DB_USER}")
				.withProperty("spring.datasource.password", "${DB_PASSWORD}");

		IllegalStateException ex = assertThrows(IllegalStateException.class, () -> run(env));

		assertTrue(ex.getMessage().contains("spring.datasource.url"));
		assertTrue(ex.getMessage().contains("spring.datasource.username"));
		assertTrue(ex.getMessage().contains("spring.datasource.password"));
		assertTrue(ex.getMessage().contains("DB_PASSWORD"));
		assertTrue(ex.getMessage().contains("dev"), "should tell how to use the dev profile");
	}

	@Test
	@DisplayName("fails when only the password is missing")
	void missingPassword_fails() {
		MockEnvironment env = new MockEnvironment()
				.withProperty("spring.datasource.url", "jdbc:postgresql://db:5432/app")
				.withProperty("spring.datasource.username", "app")
				.withProperty("spring.datasource.password", "${DB_PASSWORD}");

		IllegalStateException ex = assertThrows(IllegalStateException.class, () -> run(env));

		assertTrue(ex.getMessage().contains("spring.datasource.password"));
		assertTrue(!ex.getMessage().contains("spring.datasource.url"));
	}

	@Test
	@DisplayName("fails when nothing is configured at all")
	void nothingConfigured_fails() {
		assertThrows(IllegalStateException.class, () -> run(new MockEnvironment()));
	}

	@Test
	@DisplayName("fails when the url or username is blank")
	void blankUrlOrUsername_fails() {
		MockEnvironment env = complete().withProperty("spring.datasource.url", "  ")
				.withProperty("spring.datasource.username", "");

		IllegalStateException ex = assertThrows(IllegalStateException.class, () -> run(env));

		assertTrue(ex.getMessage().contains("spring.datasource.url"));
		assertTrue(ex.getMessage().contains("spring.datasource.username"));
	}

	@Test
	@DisplayName("an empty password is allowed (e.g. an in-memory test database)")
	void emptyPassword_ok() {
		assertDoesNotThrow(() -> run(complete().withProperty("spring.datasource.password", "")));
	}
}
