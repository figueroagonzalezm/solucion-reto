package com.exam.test.exception;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "Error returned by the API")
public class ErrorResponse {

	@Schema(description = "When the error happened (UTC)", example = "2026-11-20T15:30:00Z")
	private Instant timestamp;

	@Schema(description = "HTTP status code", example = "404")
	private int status;

	@Schema(description = "HTTP status reason", example = "Not Found")
	private String error;

	@Schema(description = "Human readable description of the problem", example = "Client with id 99 not found")
	private String message;

	@Schema(description = "Request path", example = "/api/clients/99")
	private String path;
}
