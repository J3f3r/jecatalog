package com.jeferson.jecatalog.services.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerMapping;

import com.jeferson.jecatalog.dto.UserUpdateDTO;
import com.jeferson.jecatalog.entities.User;
import com.jeferson.jecatalog.repositories.UserRepository;
import com.jeferson.jecatalog.resources.exceptions.FieldMessage;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UserUpdateValidator implements ConstraintValidator<UserUpdateValid, UserUpdateDTO> {
	
	@Autowired
	private HttpServletRequest request;
	
	@Autowired
	private UserRepository repository;
	
	@Override
	public void initialize(UserUpdateValid ann) {
	}

	@Override
	public boolean isValid(UserUpdateDTO dto, ConstraintValidatorContext context) {
		
		@SuppressWarnings("unchecked")
		var uriVars = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
		
		long userId = Long.parseLong(uriVars.get("id"));
		
		List<FieldMessage> list = new ArrayList<>();
		
		// Coloque aqui seus testes de validação, acrescentando objetos FieldMessage à lista
		User user = repository.findByEmail(dto.getEmail());
		//if (user != null && userId != user.getId()) {// verifica se id a ser atualizado, se seu email tambem ja existe
		if (user != null && !user.getId().equals(userId)) {// mais robusto
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
