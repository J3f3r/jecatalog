package com.jeferson.jecatalog.tests;

import java.time.Instant;

import com.jeferson.jecatalog.dto.ProductDTO;
import com.jeferson.jecatalog.entities.Category;
import com.jeferson.jecatalog.entities.Product;

public class Factory {

	public static Product createProduct() {
		Product product = new Product(1L, "Phone", "Good Phone", 800.0, "https://img.com/img.png", Instant.parse("2025-10-20T03:00:00Z"));
		product.getCategories().add(createCategory());// aproveita o new Category abaixo
		
		return product;
	}
	
	public static ProductDTO createProductDTO() {
		Product product = createProduct();// nao precisa instanciar novamente, aproveita que ja foi acima
		return new ProductDTO(product, product.getCategories());
		//retorna o produto com sua lista de categorias
	}
	
	public static Category createCategory() {
		Category category = new Category(2L, "Electronics");
		
		return category;
	}
}
