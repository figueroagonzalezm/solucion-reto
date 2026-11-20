package com.exam.test.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.exam.test.exception.ClientNotFoundException;
import com.exam.test.model.dtos.ClientDto;
import com.exam.test.service.IClientService;

/**
 * Web-layer tests: controller + GlobalExceptionHandler with a mocked service.
 * They do not need a database.
 */
@WebMvcTest(ClientController.class)
class ClientControllerTest {

	private static final String VALID_CLIENT =
			"{\"firstName\":\"Ann\",\"lastName\":\"Lee\",\"surname\":\"Ray\","
			+ "\"phone\":\"5551234\",\"email\":\"ann@example.com\"}";

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private IClientService clientService;

	// ---- GET /api/clients ----

	@Test
	@DisplayName("GET /api/clients returns 200 with the list of clients")
	void getClients_returnsList() throws Exception {
		when(clientService.findAll()).thenReturn(
				List.of(new ClientDto(1, "Ann", "Lee", "Ray", "5551234", "ann@example.com")));

		mockMvc.perform(get("/api/clients"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[0].firstName").value("Ann"))
				.andExpect(jsonPath("$[0].email").value("ann@example.com"));
	}

	@Test
	@DisplayName("GET /api/clients returns 200 and an empty array when there are no clients")
	void getClients_empty() throws Exception {
		when(clientService.findAll()).thenReturn(List.of());

		mockMvc.perform(get("/api/clients"))
				.andExpect(status().isOk())
				.andExpect(content().json("[]"));
	}

	// ---- POST /api/client ----

	@Test
	@DisplayName("POST /api/client saves a valid client and returns success")
	void saveClient_ok() throws Exception {
		when(clientService.save(any(ClientDto.class))).thenReturn(1);

		mockMvc.perform(post("/api/client").contentType(MediaType.APPLICATION_JSON).content(VALID_CLIENT))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Client saved with sucesss"));
	}

	@Test
	@DisplayName("POST /api/client passes phone and email to the service")
	void saveClient_mapsEmail() throws Exception {
		when(clientService.save(any(ClientDto.class))).thenReturn(1);

		mockMvc.perform(post("/api/client").contentType(MediaType.APPLICATION_JSON).content(VALID_CLIENT))
				.andExpect(status().isOk());

		ArgumentCaptor<ClientDto> captor = ArgumentCaptor.forClass(ClientDto.class);
		verify(clientService).save(captor.capture());
		assertEquals("ann@example.com", captor.getValue().getEmail());
		assertEquals("5551234", captor.getValue().getPhone());
	}

	@Test
	@DisplayName("POST /api/client with malformed JSON returns 400 and does not call the service")
	void saveClient_malformedJson() throws Exception {
		mockMvc.perform(post("/api/client").contentType(MediaType.APPLICATION_JSON).content("{bad json"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.error").value("Bad Request"))
				.andExpect(jsonPath("$.message").value("Malformed or missing JSON request body"))
				.andExpect(jsonPath("$.path").value("/api/client"));

		verify(clientService, never()).save(any());
	}

	@Test
	@DisplayName("POST /api/client without a body returns 400")
	void saveClient_missingBody() throws Exception {
		mockMvc.perform(post("/api/client").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Malformed or missing JSON request body"));
	}

	@Test
	@DisplayName("POST /api/client returns 409 when the database rejects the data")
	void saveClient_constraintViolation() throws Exception {
		when(clientService.save(any(ClientDto.class)))
				.thenThrow(new DataIntegrityViolationException("value too long for type varchar(100)"));

		mockMvc.perform(post("/api/client").contentType(MediaType.APPLICATION_JSON).content(VALID_CLIENT))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.error").value("Conflict"));
	}

	@Test
	@DisplayName("POST /api/client returns 500 on a database error without leaking details")
	void saveClient_databaseError() throws Exception {
		when(clientService.save(any(ClientDto.class)))
				.thenThrow(new DataAccessResourceFailureException("connection refused to secret-host:5432"));

		mockMvc.perform(post("/api/client").contentType(MediaType.APPLICATION_JSON).content(VALID_CLIENT))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.status").value(500))
				.andExpect(jsonPath("$.message").value("Database error"))
				.andExpect(content().string(not(containsString("secret-host"))));
	}

	@Test
	@DisplayName("POST /api/client returns 500 on an unexpected error without leaking details")
	void saveClient_unexpectedError() throws Exception {
		when(clientService.save(any(ClientDto.class))).thenThrow(new IllegalStateException("internal detail"));

		mockMvc.perform(post("/api/client").contentType(MediaType.APPLICATION_JSON).content(VALID_CLIENT))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.message").value("Unexpected error"))
				.andExpect(content().string(not(containsString("internal detail"))));
	}

	// ---- DELETE /api/clients/{id} ----

	@Test
	@DisplayName("DELETE /api/clients/{id} returns 204 when the client is deleted")
	void deleteClient_ok() throws Exception {
		mockMvc.perform(delete("/api/clients/7"))
				.andExpect(status().isNoContent())
				.andExpect(content().string(""));

		verify(clientService).deleteById(7);
	}

	@Test
	@DisplayName("DELETE /api/clients/{id} returns 404 with an error body when the client does not exist")
	void deleteClient_notFound() throws Exception {
		doThrow(new ClientNotFoundException(99)).when(clientService).deleteById(99);

		mockMvc.perform(delete("/api/clients/99"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.error").value("Not Found"))
				.andExpect(jsonPath("$.message").value("Client with id 99 not found"))
				.andExpect(jsonPath("$.path").value("/api/clients/99"))
				.andExpect(jsonPath("$.timestamp").exists());
	}

	@Test
	@DisplayName("DELETE /api/clients/{id} with a non-numeric id returns 400 and does not call the service")
	void deleteClient_invalidId() throws Exception {
		mockMvc.perform(delete("/api/clients/abc"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Invalid value for parameter 'id'"));

		verify(clientService, never()).deleteById(anyInt());
	}
}
