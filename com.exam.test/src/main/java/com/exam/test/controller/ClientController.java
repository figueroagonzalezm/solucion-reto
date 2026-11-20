package com.exam.test.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exam.test.exception.ErrorResponse;
import com.exam.test.model.dtos.ClientDto;
import com.exam.test.model.dtos.ServiceResponse;
import com.exam.test.service.IClientService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@Tag(name = "Clients", description = "Operations to manage clients")
public class ClientController {
	@Autowired
	private IClientService clientService;

	@GetMapping("/clients")
	@Operation(summary = "List clients", description = "Returns all registered clients.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "List of clients (empty if none)",
				content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClientDto.class))))
	})
	public ResponseEntity<List<ClientDto>> getClients() {
		return ResponseEntity.ok(clientService.findAll());
	}

	@PostMapping("/client")
	@Operation(summary = "Create client", description = "Saves a new client.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Result of the save operation",
				content = @Content(schema = @Schema(implementation = ServiceResponse.class))),
		@ApiResponse(responseCode = "400", description = "Malformed JSON body",
				content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
		@ApiResponse(responseCode = "409", description = "Data violates a database constraint",
				content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	public ResponseEntity<ServiceResponse> saveClient(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Client data to save")
			@RequestBody ClientDto client) {
		ServiceResponse serviceReponse = new ServiceResponse();
		clientService.save(client);
		serviceReponse.setSuccess(true);
		serviceReponse.setMessage("Client saved with sucesss");

		return new ResponseEntity<>(serviceReponse, HttpStatus.OK);
	}

	@DeleteMapping("/clients/{id}")
	@Operation(summary = "Delete client", description = "Deletes the client with the given id.")
	@ApiResponses({
		@ApiResponse(responseCode = "204", description = "Client deleted", content = @Content),
		@ApiResponse(responseCode = "400", description = "Invalid id",
				content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
		@ApiResponse(responseCode = "404", description = "Client not found",
				content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	public ResponseEntity<Void> deleteClient(
			@Parameter(description = "Identifier of the client to delete", example = "1")
			@PathVariable int id) {
		clientService.deleteById(id);
		return ResponseEntity.noContent().build();
	}

}
