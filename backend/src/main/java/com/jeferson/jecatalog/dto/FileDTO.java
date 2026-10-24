package com.jeferson.jecatalog.dto;

import org.springframework.web.multipart.MultipartFile;

// DTO para o corpo da requisicao receber o arquivo
public class FileDTO {

	private MultipartFile file;
	
	public FileDTO() {}

	public MultipartFile getFile() {
		return file;
	}

	public void setFile(MultipartFile file) {
		this.file = file;
	}
}
