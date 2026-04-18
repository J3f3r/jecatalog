package com.jeferson.jecatalog.repositories;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.EmptyResultDataAccessException;

import com.jeferson.jecatalog.entities.Product;
import com.jeferson.jecatalog.tests.Factory;

@DataJpaTest
public class ProductRepositoryTests {

	@Autowired
	private ProductRepository repository;
	
	private Long existingId;
	private Long nonExistingId;
	private Long countTotalProducts;
	
	@BeforeEach
	void setUp() throws Exception{
		existingId = 1L;
		nonExistingId = 1000L;
		countTotalProducts = 25L;
	}
	
	@Test// nao precisa criar nada, soh consulta o banco de dados
	public void findByIdShouldReturnNonemptyOptionalProductIDExists() {
		//act
		Optional <Product> result = repository.findById(existingId);
		//assert
		Assertions.assertTrue(result.isPresent());
	}
	
	@Test
	public void findByIdShouldReturnEmptyOptionalProductWhenIDDoesNotExists() {
		//act
		Optional<Product> result = repository.findById(nonExistingId);
		//assert
		Assertions.assertTrue(result.isEmpty()); // Verifica se o Optional está vazio
	}
	
	@Test
	public void saveShouldPersistWhitAutoincrementWhenIdIsNull() {
		//arrenge
		Product product = Factory.createProduct();
		product.setId(null);
		
		//act
		product = repository.save(product);
		
		//assert
		Assertions.assertNotNull(product.getId());
		Assertions.assertEquals(countTotalProducts + 1, product.getId());
	}
	
	@Test
	public void deleteShouldDeleteObjectWhenIdExists() {
		//arrenge
		//long existingId = 1L;// usado no BeforeEach
		
		//act
		repository.deleteById(existingId);
		
		//assert
		Optional <Product> result = repository.findById(existingId);
		Assertions.assertFalse(result.isPresent());
	}
	
//	@Test// esse teste eh para o spring 4
//	public void deleteShouldThrowEmptyResultAccessExceptionIdDoesNotExists() {
//		//Long nonExistingId = 1000L;
//		
//		Assertions.assertThrows(EmptyResultDataAccessException.class, () -> {
//			repository.deleteById(nonExistingId);
//		});
//	}
}
