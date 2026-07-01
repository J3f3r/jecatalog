package com.jeferson.jecatalog.dto;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class NewPasswordDTO implements Serializable{
	private static final long serialVersionUID = 1L;

	@NotBlank(message = "Campo obrigatório")
	private String token;
	
	@NotBlank(message = "Campo obrigatório")
	@Size(min = 8, message = "Deve ter no mínimmo 8 caracteres")
	@Pattern(
		    regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>]).*$",
		    message = "A senha deve conter pelo menos uma letra maiúscula, um número e um caractere especial"
		)
	private String password;
	
	public NewPasswordDTO() {}

	public NewPasswordDTO(String token, String password) {

		this.token = token;
		this.password = password;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
