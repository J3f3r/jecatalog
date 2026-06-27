package com.jeferson.jecatalog.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.jeferson.jecatalog.services.exceptions.EmailException;

@Service
public class EmailService {

	@Value("${spring.mail.username}")
	private String emailFrom;
	
	@Autowired
	private JavaMailSender emailSender;
	
	public void sendEmail(String to, String subject, String body) {
		// em vez DTO, faremos hard code dentro do metodo com variaveis independentes
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(emailFrom);
			message.setTo(to);
			message.setSubject(subject);
			message.setText(body);
			
			emailSender.send(message);
		}
		catch (MailException e) {
			throw new EmailException ("Failed to send email");
		}
	}
}
