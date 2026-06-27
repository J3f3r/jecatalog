package com.jeferson.jecatalog.dto;

import java.io.Serializable;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class EmailDTO implements Serializable{
	private static final long serialVersionUID = 1L;

	@NotBlank(message = "campo obrigatório")
	@Email(message = "Email inválido")
	private String email;
	
	public EmailDTO() {}

	public EmailDTO( String email) {
		this.email = email;
	}

	public String getEmail() {
		return email;
	}
}
