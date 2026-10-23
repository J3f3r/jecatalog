package com.jeferson.jecatalog;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.jeferson.jecatalog.services.S3Service;

@SpringBootApplication
public class JecatalogApplication implements CommandLineRunner{

	private final S3Service s3Service;

	JecatalogApplication(S3Service s3Service) {
		this.s3Service = s3Service;
	}
	
	public static void main(String[] args) {
		SpringApplication.run(JecatalogApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		
		s3Service.uploadFile("c:\\sts\\foto.png");
	}

}
