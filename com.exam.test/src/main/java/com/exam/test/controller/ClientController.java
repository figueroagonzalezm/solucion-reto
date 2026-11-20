package com.exam.test.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exam.test.model.dtos.ClientDto;
import com.exam.test.model.dtos.ServiceResponse;
import com.exam.test.service.IClientService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@CrossOrigin(origins = { "http://localhost:8080" })
@RequestMapping("/api")
@Tag(name = "Clients", description = "Operations to manage clients")
public class ClientController {
	@Autowired
	private IClientService clientService;

	@CrossOrigin(origins = "http://localhost:4200")
	@GetMapping("/clients")
	@Operation(summary = "List clients", description = "Returns all registered clients.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "List of clients (empty if none)",
				content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClientDto.class))))
	})
	public ResponseEntity<List<ClientDto>> getClients() {
		return ResponseEntity.ok(clientService.findAll());
	}

	@CrossOrigin(origins = "http://localhost:4200")
	@PostMapping("/client")
	@Operation(summary = "Create client", description = "Saves a new client.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Result of the save operation",
				content = @Content(schema = @Schema(implementation = ServiceResponse.class)))
	})
	public ResponseEntity<ServiceResponse> saveClient(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Client data to save")
			@RequestBody ClientDto client) {
		ServiceResponse serviceReponse = new ServiceResponse();
		int result = clientService.save(client);
		if (result == 1) {
			serviceReponse.setSuccess(true);
			serviceReponse.setMessage("Client saved with sucesss");
		}

		return new ResponseEntity<>(serviceReponse, HttpStatus.OK);
	}

}
