package com.exam.test.model.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Generic operation result")
public class ServiceResponse {
	@Schema(description = "Whether the operation succeeded", example = "true")
	Boolean success;
	@Schema(description = "Result message", example = "Client saved with sucesss")
	String message;
}
