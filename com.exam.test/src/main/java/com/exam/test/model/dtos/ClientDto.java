package com.exam.test.model.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Client payload. The constraints are enforced on {@code POST /api/client} and mirror the
 * rules of the Angular form (required fields, digits-only phone, user@domain.tld email).
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Client data")
public class ClientDto {
	@Schema(description = "Client identifier (ignored on create, assigned by the database)", example = "1")
	int id;

	@NotBlank(message = "is required")
	@Size(max = 100, message = "must be at most 100 characters")
	@Schema(description = "First name", example = "John", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 100)
	String firstName;

	@NotBlank(message = "is required")
	@Size(max = 100, message = "must be at most 100 characters")
	@Schema(description = "Last name", example = "Doe", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 100)
	String lastName;

	@Size(max = 100, message = "must be at most 100 characters")
	@Schema(description = "Surname (optional)", example = "Smith", maxLength = 100)
	String surname;

	@NotBlank(message = "is required")
	@Pattern(regexp = "\\d*", message = "must contain digits only")
	@Size(max = 30, message = "must be at most 30 characters")
	@Schema(description = "Phone number (digits only)", example = "5551234567",
			requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 30)
	String phone;

	@NotBlank(message = "is required")
	@Pattern(regexp = "([^\\s@]+@[^\\s@]+\\.[^\\s@]+)?", message = "must be a valid email (user@domain.com)")
	@Size(max = 150, message = "must be at most 150 characters")
	@Schema(description = "Email address", example = "john.doe@example.com",
			requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 150)
	String email;


	 public ClientDto(int id, String firstName, String lastName, String surname, String phone, String email){
		super();
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.surname = surname;
		this.phone = phone;
		this.email = email;
	 }
}
