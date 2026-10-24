package com.jeferson.jecatalog.dto;

import java.io.Serializable;

// DTO para o corpo da requisicao o link do file
public class UriDTO implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String uri;
	
	public UriDTO() {}

	public UriDTO(String uri) {
		this.uri = uri;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
}
