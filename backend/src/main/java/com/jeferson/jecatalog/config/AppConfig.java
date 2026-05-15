package com.jeferson.jecatalog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class AppConfig {

    @Bean
    BCryptPasswordEncoder passordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
