package com.jeferson.jecatalog.dto;

import com.jeferson.jecatalog.services.validation.UserInsertValid;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@UserInsertValid
public class UserInsertDTO extends UserDTO{
	private static final long serialVersionUID = 1L;

	@NotBlank(message = "campo obrigatório")
	@Size(min = 8, message = "deve ter no mínimo 8 caracteres")
	@Pattern(
		    regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>]).*$",
		    message = "A senha deve conter pelo menos uma letra maiúscula, um número e um caractere especial"
		)
	private String password;
	
	UserInsertDTO(){
		super();
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
