package com.exam.test.model.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Client data")
public class ClientDto {
	@Schema(description = "Client identifier", example = "1")
	int id;
	@Schema(description = "First name", example = "John")
	String firstName;
	@Schema(description = "Last name", example = "Doe")
	String lastName;
	@Schema(description = "Surname", example = "Smith")
	String surname;
	@Schema(description = "Phone number (digits only)", example = "5551234567")
	String phone;
	@Schema(description = "Email address", example = "john.doe@example.com")
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
