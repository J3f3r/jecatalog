package com.jeferson.jecatalog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable());
		http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
		
		// 3. O PULO DO GATO: Desativa a protecao de frames para o H2 poder desenhar a tela
	    http.headers(headers -> headers.frameOptions(frame -> frame.disable()));
	    
		return http.build();
	}
	// configuracao provisoria para liberar todos endpoints
}

