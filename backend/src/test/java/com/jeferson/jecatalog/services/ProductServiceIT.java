package com.jeferson.jecatalog.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import com.jeferson.jecatalog.dto.ProductDTO;
import com.jeferson.jecatalog.repositories.ProductRepository;
import com.jeferson.jecatalog.services.exceptions.ResourceNotFoundException;

@SpringBootTest
@Transactional
public class ProductServiceIT {

	@Autowired
	private ProductService service;
	
	@Autowired
	private ProductRepository repository;
	
	private Long existingId;
	private Long nonExistingId;
	private Long countTotalProducts;
	
	@BeforeEach
	void setUp() throws Exception{
		existingId = 1L;
		nonExistingId = 1000L;
		countTotalProducts = 25L;// sabemos que tem essa qtdd no bd
	}
	
	@Test
	public void findAllPagedShoulReturnPageWhenPage0Size10() {
		// arreng: classe que implementa pageRequest e of metodo que controi objetos
		PageRequest pageRequest = PageRequest.of(0, 10);
		
		// act
		Page<ProductDTO> result = service.findAllPaged(pageRequest);
		
		//assert
		Assertions.assertFalse(result.isEmpty());
		Assertions.assertEquals(0, result.getNumber());
		Assertions.assertEquals(10, result.getSize());
		Assertions.assertEquals(countTotalProducts, result.getTotalElements());
	}
	
	@Test
	public void findAllPagedShoulReturnEmptyPageWhenPageDoesNotExist() {
		
		PageRequest pageRequest = PageRequest.of(50, 10);// sabemos nao existir
		
		Page<ProductDTO> result = service.findAllPaged(pageRequest);
		
		Assertions.assertTrue(result.isEmpty());// verdade que pagina esta vazia
	}
	
	@Test
	public void findAllPagedShouldReturnSortedPageWhenSortByName() {
		
		PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("name"));// buscar por nome
		
		Page<ProductDTO> result = service.findAllPaged(pageRequest);
		
		Assertions.assertFalse(result.isEmpty());
		Assertions.assertEquals("Macbook Pro", result.getContent().get(0).getName());
		Assertions.assertEquals("PC Gamer", result.getContent().get(1).getName());
		Assertions.assertEquals("PC Gamer Alfa", result.getContent().get(2).getName());
	}
	
	@Test
	public void deleteShouldDeleteResourceWhenIdExists() {
		
		service.delete(existingId);
		
		Assertions.assertEquals(countTotalProducts - 1, repository.count());
		// compara total - 1 delecao com o que tem no banco apos a delecao
	}
	
	@Test
	public void deleteShouldThrowsResourceNotFoundExceptionWhenIdDoesNotExist() {
		
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			
			service.delete(nonExistingId);
		});
	}
}
