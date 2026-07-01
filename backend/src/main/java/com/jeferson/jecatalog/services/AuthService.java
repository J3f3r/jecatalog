package com.jeferson.jecatalog.services;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeferson.jecatalog.dto.EmailDTO;
import com.jeferson.jecatalog.dto.NewPasswordDTO;
import com.jeferson.jecatalog.entities.PasswordRecover;
import com.jeferson.jecatalog.entities.User;
import com.jeferson.jecatalog.repositories.PasswordRecoverRepository;
import com.jeferson.jecatalog.repositories.UserRepository;
import com.jeferson.jecatalog.services.exceptions.ResourceNotFoundException;

@Service
public class AuthService {

	@Autowired
	private UserRepository userRepository;
	
	@Value("${email.password-recover.token.minutes}")
	private Long tokenMinutes;
	
	@Autowired
	@Lazy
	PasswordEncoder passwordEncoder;
	
	@Autowired
	private PasswordRecoverRepository passwordRecoverRepository;
	
	@Autowired
	private EmailService emailService;
	
	// vamos usar a variavel de ambiente URI para simular o fornt end
	@Value("${email.password-recover.uri}")
	private String recoverUri;
	
	@Transactional
	public void createRecoverToken(EmailDTO body) {
		// PASSO 1 busca no banco de dados e verifica se o email existe
		User user = userRepository.findByEmail(body.getEmail());
		
		// se nao existir email, o user vai ficar nul
		if (user == null) {
			throw new ResourceNotFoundException("Email não encontrado!");// 404
		}
		
		// PASSO 2 gera o token com certa validade de tempo e salva no bd
		
		String token = UUID.randomUUID().toString();
		
		PasswordRecover entity = new PasswordRecover();
		
		// quem eh o email
		entity.setEmail(body.getEmail());
		
		// vou gerar um token aleatorio de string com UUID
		entity.setToken(token);
		
		// vamos pegar o instante atual + (plus) um tempo adicional em minutos = ao tempo de expiracao
		entity.setExpiration(Instant.now().plusSeconds(tokenMinutes * 60L));
		
		// salvar no bd
		entity = passwordRecoverRepository.save(entity);
		
		// PASSO 3 enviar o email para o usuario com um link para usar o token
		String text = "Acesse o link para definir uma nova senha \n\n"
				+ recoverUri + token + ". Validade de" + tokenMinutes + " minutos";
		emailService.sendEmail(body.getEmail(), "Recuperação de senha", text);
	}

	@Transactional
	public void saveNewPassword(NewPasswordDTO body) {
		
		List<PasswordRecover> result = passwordRecoverRepository.searchValidTokens(body.getToken(), Instant.now());
		
		if(result.size() == 0) {
			throw new ResourceNotFoundException("Email não encontrado!");
		}
		
		User user = userRepository.findByEmail(result.get(0).getEmail());
		
		user.setPassword(passwordEncoder.encode(body.getPassword()));
		
		user = userRepository.save(user);
	}
	
	protected User authenticated() {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			Jwt jwtPrincipal = (Jwt) authentication.getPrincipal();
			String username = jwtPrincipal.getClaim("username");
			return userRepository.findByEmail(username);
		} catch (Exception e) {
			throw new UsernameNotFoundException("Invalid user");
		}
	}

}
