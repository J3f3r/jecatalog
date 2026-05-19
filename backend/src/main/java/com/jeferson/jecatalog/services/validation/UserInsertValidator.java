package com.jeferson.jecatalog.services.validation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.jeferson.jecatalog.dto.UserInsertDTO;
import com.jeferson.jecatalog.entities.User;
import com.jeferson.jecatalog.repositories.UserRepository;
import com.jeferson.jecatalog.resources.exceptions.FieldMessage;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UserInsertValidator implements ConstraintValidator<UserInsertValid, UserInsertDTO> {
	
	@Autowired
	private UserRepository repository;
	
	@Override
	public void initialize(UserInsertValid ann) {
	}

	@Override
	public boolean isValid(UserInsertDTO dto, ConstraintValidatorContext context) {
		
		List<FieldMessage> list = new ArrayList<>();
		
		// Coloque aqui seus testes de validação, acrescentando objetos FieldMessage à lista
		User user = repository.findByEmail(dto.getEmail());
		if (user != null) {
			list.add(new FieldMessage("email", "e-mail já existe"));
		}
		
		
		for (FieldMessage e : list) {// este, percorre FieldMessage, coletas os erros que encontrar e os insere na lista de erros do Bean Validation de mesmo tipo
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(e.getMessage()).addPropertyNode(e.getFieldName())
					.addConstraintViolation();
		}
		return list.isEmpty();//se lista vazia - true - nao ha erros, caso contrario dispara a retorna mensagem customizada
	}
}
