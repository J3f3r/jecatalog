package com.jeferson.jecatalog.services;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.jeferson.jecatalog.dto.ProductDTO;
import com.jeferson.jecatalog.entities.Category;
import com.jeferson.jecatalog.entities.Product;
import com.jeferson.jecatalog.repositories.CategoryRepository;
import com.jeferson.jecatalog.repositories.ProductRepository;
import com.jeferson.jecatalog.services.exceptions.DatabaseException;
import com.jeferson.jecatalog.services.exceptions.ResourceNotFoundException;
import com.jeferson.jecatalog.tests.Factory;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(SpringExtension.class)
public class ProductServiceTests {

	@InjectMocks
	private ProductService service;
	
	@Mock
	private ProductRepository repository;
	
	@Mock
	private CategoryRepository categoryRepository;
	
	private Long existingId;
	private Long nonExistingId;
	private Long dependentId;
	
	private PageImpl<Product> page;
	private Product product;
	
	private ProductDTO productDTO;
	private Category category;
	
	@BeforeEach
	void setUp() throws Exception{
		org.mockito.MockitoAnnotations.openMocks(this);
		existingId = 1L;// valores fakes (mockados) que nada tem haver com os valores do seed do banco de dados
		nonExistingId = 2L;
		dependentId = 4L;
		
		product = Factory.createProduct();
		page = new PageImpl<>(List.of(product));
		
		productDTO = Factory.createProductDTO();
		category = Factory.createCategory();
		
		// Mock do comportamento de deleção (Ação solicitada). metodos void
	    Mockito.doNothing().when(repository).deleteById(existingId);
	    Mockito.doThrow(DataIntegrityViolationException.class).when(repository).deleteById(dependentId);// comportamento do metodo mockado para chegar ao catch depois do if e try
		
	    //comportamento do busca paginada e Save, com retorno
	    Mockito.when(repository.findAll((Pageable)ArgumentMatchers.any())).thenReturn(page);
	    Mockito.when(repository.save(ArgumentMatchers.any())).thenReturn(product);
	    
	    //comportamento do findById com id existente e sem existir
	    Mockito.when(repository.findById(existingId)).thenReturn(Optional.of(product));
	    Mockito.when(repository.findById(nonExistingId)).thenReturn(Optional.empty());
	    
	    // comportamento do getReferenceById para sucesso, e para erro (exception do JPA)
	    Mockito.when(repository.getReferenceById(existingId)).thenReturn(product);
	    Mockito.when(repository.getReferenceById(nonExistingId)).thenThrow(EntityNotFoundException.class);
	    
	    // comportamento do CategoryRepository (usando o loop copyDtoToEntity)
	    Mockito.when(categoryRepository.getReferenceById(nonExistingId)).thenReturn(category);
	    
		Mockito.when(repository.existsById(existingId)).thenReturn(true);
		Mockito.when(repository.existsById(nonExistingId)).thenReturn(false);
		Mockito.when(repository.existsById(dependentId)).thenReturn(true);
	}
	
	@Test
	public void insertShouldReturnProductDTO() {
	    // Act
	    ProductDTO result = service.insert(productDTO);

	    // Assert
	    Assertions.assertNotNull(result);
	    Assertions.assertEquals(product.getId(), result.getId()); // Verifica se o DTO de retorno tem o ID que o Mock deu
	    Mockito.verify(repository, Mockito.times(1)).save(ArgumentMatchers.any());
	}
	
	@Test
	public void updateShouldReturnProductDTOWhenIdExistis() {
		//act
		ProductDTO result = service.update(existingId, productDTO);
		
		//assert
		Assertions.assertNotNull(result);
		Assertions.assertEquals(productDTO.getName(), result.getName());
		
		Mockito.verify(repository, Mockito.times(1)).getReferenceById(existingId);
		Mockito.verify(repository, Mockito.times(1)).save(ArgumentMatchers.any());
	}
	
	@Test
	public void updateShouldThrowResourceNotFoundExceptionWhenIdNotExisti() {
		//act + assert
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.update(nonExistingId, productDTO);
		});
		
		Mockito.verify(repository, Mockito.times(1)).getReferenceById(nonExistingId);
	}
	
	@Test
	public void findByIdShoulReturnProductDTOWhenIdExistis() {
		//act
		ProductDTO result = service.findById(existingId);
		
		Assertions.assertNotNull(result);
		Assertions.assertEquals(existingId, result.getId());
		
		Mockito.verify(repository, Mockito.times(1)).findById(existingId);
	}
	
	@Test
	public void findByIdShouldThrowsResourceNotFoundExceptionWhenIdNotExisti() {
		//act + assert
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.findById(nonExistingId);
		});
		
		// verifica se o repositorio foi consultado
		Mockito.verify(repository, Mockito.times(1)).findById(nonExistingId);
	}
	
	@Test
	public void findAllPagedShouldReturnPage() {
		Pageable pageable = PageRequest.of(0, 10);
		
		Page<ProductDTO> result = service.findAllPaged(pageable);
		
		Assertions.assertNotNull(result);
		Mockito.verify(repository, Mockito.times(1)).findAll(pageable);
	}
		
	@Test// teste preparado caso a aplicacao evolua e apareca um id dependent pois atualmente ainda nao ha
	public void deleteShouldThrowsDatabaseExceptionWhenDependentId() {
		Assertions.assertThrows(DatabaseException.class, () -> {
			service.delete(dependentId);
		});
	}
	@Test
	public void deleteShouldThrowsResourceNotFoundExceptionWhenIdDoesNotExistis() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.delete(nonExistingId);
		});
		// apos a verificaca da nao existencia do id, eh lancada a excecao e codigo encerra pois nao entra no delete, por isso nao precisa testar mais nada aqui
	}
	
	@Test
	public void deleteShouldDoNothingWhenIdExistis() {
		//act + assert
		Assertions.assertDoesNotThrow(() ->{
			service.delete(existingId);
		});
		
		Mockito.verify(repository, Mockito.times(1)).deleteById(existingId);
		// verifica se o metodo deleteById foi chamado no teste e quantas vezes com times()
	}
}
