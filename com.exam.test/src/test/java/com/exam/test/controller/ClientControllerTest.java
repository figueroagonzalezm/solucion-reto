package com.exam.test.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

	private static final ClientDto SAVED_CLIENT =
			new ClientDto(1, "Ann", "Lee", "Ray", "5551234", "ann@example.com");

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
		when(clientService.save(any(ClientDto.class))).thenReturn(SAVED_CLIENT);

		mockMvc.perform(post("/api/client").contentType(MediaType.APPLICATION_JSON).content(VALID_CLIENT))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Client saved with sucesss"));
	}

	@Test
	@DisplayName("POST /api/client passes phone and email to the service")
	void saveClient_mapsEmail() throws Exception {
		when(clientService.save(any(ClientDto.class))).thenReturn(SAVED_CLIENT);

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

	// ---- POST /api/client: server-side validation ----

	private static String clientJson(String firstName, String lastName, String phone, String email) {
		return "{\"firstName\":" + json(firstName) + ",\"lastName\":" + json(lastName)
				+ ",\"phone\":" + json(phone) + ",\"email\":" + json(email) + "}";
	}

	private static String json(String value) {
		return value == null ? "null" : "\"" + value + "\"";
	}

	private org.springframework.test.web.servlet.ResultActions postClient(String body) throws Exception {
		return mockMvc.perform(post("/api/client").contentType(MediaType.APPLICATION_JSON).content(body));
	}

	@Test
	@DisplayName("POST /api/client with an empty object returns 400 listing every required field")
	void saveClient_emptyObject() throws Exception {
		postClient("{}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.message").value(startsWith("Validation failed")))
				.andExpect(jsonPath("$.errors.firstName").value("is required"))
				.andExpect(jsonPath("$.errors.lastName").value("is required"))
				.andExpect(jsonPath("$.errors.phone").value("is required"))
				.andExpect(jsonPath("$.errors.email").value("is required"))
				.andExpect(jsonPath("$.path").value("/api/client"));

		verify(clientService, never()).save(any());
	}

	@Test
	@DisplayName("POST /api/client rejects blank and whitespace-only values")
	void saveClient_blankValues() throws Exception {
		postClient(clientJson("", "   ", "", ""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.firstName").value("is required"))
				.andExpect(jsonPath("$.errors.lastName").value("is required"))
				.andExpect(jsonPath("$.errors.phone").value("is required"))
				.andExpect(jsonPath("$.errors.email").value("is required"));

		verify(clientService, never()).save(any());
	}

	@ParameterizedTest(name = "phone \"{0}\" is rejected")
	@ValueSource(strings = { "12ab", "555-1234", "+5551234", "555 1234", "12.5", "abc" })
	@DisplayName("POST /api/client rejects a non-numeric phone")
	void saveClient_invalidPhone(String phone) throws Exception {
		postClient(clientJson("Ann", "Lee", phone, "ann@example.com"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.phone").value("must contain digits only"))
				.andExpect(jsonPath("$.errors.length()").value(1));

		verify(clientService, never()).save(any());
	}

	@ParameterizedTest(name = "email \"{0}\" is rejected")
	@ValueSource(strings = { "ann", "ann@", "@example.com", "ann@host", "ann @example.com", "ann@example." })
	@DisplayName("POST /api/client rejects a malformed email")
	void saveClient_invalidEmail(String email) throws Exception {
		postClient(clientJson("Ann", "Lee", "5551234", email))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.email").value("must be a valid email (user@domain.com)"))
				.andExpect(jsonPath("$.errors.length()").value(1));

		verify(clientService, never()).save(any());
	}

	@Test
	@DisplayName("POST /api/client reports every invalid field at once")
	void saveClient_multipleErrors() throws Exception {
		postClient(clientJson("Ann", "", "12ab", "nope"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.length()").value(3))
				.andExpect(jsonPath("$.errors.lastName").value("is required"))
				.andExpect(jsonPath("$.errors.phone").value("must contain digits only"))
				.andExpect(jsonPath("$.errors.email").value("must be a valid email (user@domain.com)"))
				.andExpect(jsonPath("$.message").value(
						"Validation failed: email must be a valid email (user@domain.com); "
						+ "lastName is required; phone must contain digits only"));
	}

	@Test
	@DisplayName("POST /api/client rejects values longer than the column size with 400 (before reaching the database)")
	void saveClient_tooLong() throws Exception {
		postClient(clientJson("x".repeat(101), "Lee", "5551234", "ann@example.com"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.firstName").value("must be at most 100 characters"));

		verify(clientService, never()).save(any());
	}

	@Test
	@DisplayName("POST /api/client accepts values exactly at the maximum length")
	void saveClient_atMaxLength() throws Exception {
		when(clientService.save(any(ClientDto.class))).thenReturn(SAVED_CLIENT);

		postClient(clientJson("x".repeat(100), "y".repeat(100), "1".repeat(30),
				"a".repeat(100) + "@example.com"))
				.andExpect(status().isOk());
	}

	@Test
	@DisplayName("POST /api/client accepts a client without the optional surname")
	void saveClient_surnameOptional() throws Exception {
		when(clientService.save(any(ClientDto.class))).thenReturn(SAVED_CLIENT);

		postClient(clientJson("Ann", "Lee", "5551234", "ann@example.com"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));
	}

	@Test
	@DisplayName("Validation errors do not use the 'errors' field on other error types")
	void otherErrors_haveNoErrorsField() throws Exception {
		doThrow(new ClientNotFoundException(99)).when(clientService).deleteById(99);

		mockMvc.perform(delete("/api/clients/99"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.errors").doesNotExist());
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
