package com.exam.test.respository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.exam.test.model.Client;

/**
 * Checks the JPA mapping against the real PostgreSQL schema (db/init.sql).
 * Needs the database running. Each test runs in a transaction that is rolled back,
 * so no rows are left behind.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ClientRepositoryTest {

	@Autowired
	private ClientRepository clientRepository;

	private Client newClient() {
		Client c = new Client();
		c.setFirstName("Ann");
		c.setLastName("Lee");
		c.setSurname("Ray");
		c.setPhone("5551234");
		c.setEmail("ann@example.com");
		return c;
	}

	@Test
	@DisplayName("save assigns the identity id and persists every column, including email")
	void save_persistsAllFields() {
		Client saved = clientRepository.saveAndFlush(newClient());

		assertNotNull(saved.getId());
		Client found = clientRepository.findById(saved.getId()).orElseThrow();
		assertEquals("Ann", found.getFirstName());
		assertEquals("Lee", found.getLastName());
		assertEquals("Ray", found.getSurname());
		assertEquals("5551234", found.getPhone());
		assertEquals("ann@example.com", found.getEmail());
	}

	@Test
	@DisplayName("findAll includes a saved client")
	void findAll_includesSaved() {
		Client saved = clientRepository.saveAndFlush(newClient());

		assertTrue(clientRepository.findAll().stream().anyMatch(c -> saved.getId().equals(c.getId())));
	}

	@Test
	@DisplayName("deleteById removes the client and existsById reports it")
	void deleteById_removes() {
		Client saved = clientRepository.saveAndFlush(newClient());
		assertTrue(clientRepository.existsById(saved.getId()));

		clientRepository.deleteById(saved.getId());
		clientRepository.flush();

		assertFalse(clientRepository.existsById(saved.getId()));
	}

	@Test
	@DisplayName("existsById is false for an id that does not exist")
	void existsById_missing() {
		assertFalse(clientRepository.existsById(-1));
	}
}
