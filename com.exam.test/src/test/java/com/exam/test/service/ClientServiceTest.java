package com.exam.test.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.exam.test.exception.ClientNotFoundException;
import com.exam.test.model.Client;
import com.exam.test.model.dtos.ClientDto;
import com.exam.test.respository.ClientRepository;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

	@Mock
	private ClientRepository clientRepository;

	@InjectMocks
	private ClientService clientService;

	private Client client(int id, String first) {
		Client c = new Client();
		c.setId(id);
		c.setFirstName(first);
		c.setLastName("Lee");
		c.setSurname("Ray");
		c.setPhone("5551234");
		c.setEmail(first.toLowerCase() + "@example.com");
		return c;
	}

	@Test
	@DisplayName("findAll maps every entity field, including email, to the DTO")
	void findAll_mapsFields() {
		when(clientRepository.findAll()).thenReturn(List.of(client(1, "Ann"), client(2, "Bob")));

		List<ClientDto> result = clientService.findAll();

		assertEquals(2, result.size());
		assertEquals(1, result.get(0).getId());
		assertEquals("Ann", result.get(0).getFirstName());
		assertEquals("Lee", result.get(0).getLastName());
		assertEquals("Ray", result.get(0).getSurname());
		assertEquals("5551234", result.get(0).getPhone());
		assertEquals("ann@example.com", result.get(0).getEmail());
		assertEquals("bob@example.com", result.get(1).getEmail());
	}

	@Test
	@DisplayName("findAll returns an empty list when there are no clients")
	void findAll_empty() {
		when(clientRepository.findAll()).thenReturn(List.of());

		assertTrue(clientService.findAll().isEmpty());
	}

	@Test
	@DisplayName("save maps the DTO to the entity (including email) and returns the saved client with its id")
	void save_mapsFieldsAndReturnsSavedClient() {
		// Simulate the database assigning the identity
		when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> {
			Client c = invocation.getArgument(0);
			c.setId(5);
			return c;
		});

		ClientDto result = clientService.save(new ClientDto(0, "Ann", "Lee", "Ray", "5551234", "ann@example.com"));

		ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
		verify(clientRepository).save(captor.capture());
		Client saved = captor.getValue();
		assertEquals("Ann", saved.getFirstName());
		assertEquals("Lee", saved.getLastName());
		assertEquals("Ray", saved.getSurname());
		assertEquals("5551234", saved.getPhone());
		assertEquals("ann@example.com", saved.getEmail());

		assertEquals(5, result.getId());
		assertEquals("ann@example.com", result.getEmail());
	}

	@Test
	@DisplayName("deleteById deletes the client when it exists")
	void deleteById_ok() {
		when(clientRepository.existsById(7)).thenReturn(true);

		clientService.deleteById(7);

		verify(clientRepository).deleteById(7);
	}

	@Test
	@DisplayName("deleteById throws ClientNotFoundException and deletes nothing when the client does not exist")
	void deleteById_notFound() {
		when(clientRepository.existsById(99)).thenReturn(false);

		ClientNotFoundException ex = assertThrows(ClientNotFoundException.class, () -> clientService.deleteById(99));

		assertEquals("Client with id 99 not found", ex.getMessage());
		verify(clientRepository, never()).deleteById(anyInt());
	}
}
