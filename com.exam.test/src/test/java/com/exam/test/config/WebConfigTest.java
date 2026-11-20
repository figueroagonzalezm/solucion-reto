package com.exam.test.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.exam.test.controller.ClientController;
import com.exam.test.service.IClientService;

/**
 * CORS origins come from {@code app.cors.allowed-origins}. The value below is deliberately
 * different from the default so the test proves the property is what is being read.
 */
@WebMvcTest(ClientController.class)
@TestPropertySource(properties = "app.cors.allowed-origins=https://app.example.com,http://localhost:4200")
class WebConfigTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private IClientService clientService;

	private MockHttpServletRequestBuilder preflight(String path, String origin, String method) {
		return options(path)
				.header(HttpHeaders.ORIGIN, origin)
				.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, method);
	}

	@Test
	@DisplayName("an origin from the property is allowed")
	void configuredOrigin_isAllowed() throws Exception {
		mockMvc.perform(preflight("/api/clients", "https://app.example.com", "GET"))
				.andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://app.example.com"));
	}

	@Test
	@DisplayName("every origin in the comma-separated list is allowed")
	void secondConfiguredOrigin_isAllowed() throws Exception {
		mockMvc.perform(preflight("/api/clients", "http://localhost:4200", "GET"))
				.andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200"));
	}

	@Test
	@DisplayName("DELETE is allowed from a configured origin")
	void delete_isAllowed() throws Exception {
		mockMvc.perform(preflight("/api/clients/1", "https://app.example.com", "DELETE"))
				.andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://app.example.com"));
	}

	@Test
	@DisplayName("localhost:8080 is no longer allowed unless configured")
	void apiOwnOrigin_isNotAllowed() throws Exception {
		mockMvc.perform(preflight("/api/clients", "http://localhost:8080", "GET"))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("an unknown origin is rejected")
	void unknownOrigin_isRejected() throws Exception {
		mockMvc.perform(preflight("/api/clients", "https://evil.example.org", "GET"))
				.andExpect(status().isForbidden());
	}
}
