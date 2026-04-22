package com.jeferson.jecatalog.resources;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.jeferson.jecatalog.dto.ProductDTO;
import com.jeferson.jecatalog.services.ProductService;
import com.jeferson.jecatalog.services.exceptions.DatabaseException;
import com.jeferson.jecatalog.services.exceptions.ResourceNotFoundException;
import com.jeferson.jecatalog.tests.Factory;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(ProductResource.class)
public class ProductResourceTests {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@MockitoBean
	private ProductService service;
	
	private Long existingId;
	private Long nonExistingId;
	private Long dependentId;
	
	private PageImpl<ProductDTO> page;
	private ProductDTO productDTO;
	
	@BeforeEach
	void setUp()throws Exception{
		existingId = 1L;
		nonExistingId = 2L;
		dependentId = 3L;
		
		productDTO = Factory.createProductDTO();
		
		page = new PageImpl<>(List.of(productDTO));
		
		//simula o comportamento do findAll pageable
		when(service.findAllPaged(ArgumentMatchers.any())).thenReturn(page);
		
		//simula o comportamento findById: se o id existir retorna Produto, se não existir retorna uma exception
		when(service.findById(existingId)).thenReturn(productDTO);
		when(service.findById(nonExistingId)).thenThrow(ResourceNotFoundException.class);
		
		//simula o comportamento do update com os mesmos 2 cenarios acima
		when(service.update(eq(existingId), any())).thenReturn(productDTO);
		when(service.update(eq(nonExistingId), any())).thenThrow(ResourceNotFoundException.class);
		
		//delete com 3 cenarios: se id existir deleta apenas. se nao existir lanca exception. se nao poder ser deletado por integridade referencial lanca exeception
		doNothing().when(service).delete(existingId);
		doThrow(ResourceNotFoundException.class).when(service).delete(nonExistingId);
		doThrow(DatabaseException.class).when(service).delete(dependentId);
		
		// simula o comportamento do cenario de inserir um produto
		when(service.insert(any())).thenReturn(productDTO);
	}
	
	@Test
	public void insertShouldReturnProductDTOCreated() throws Exception{
		// arrenge
		String jsonBody = objectMapper.writeValueAsString(productDTO);
		
		// act
		ResultActions result = mockMvc.perform(post("/products")
				.content(jsonBody)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON));
		
		// assert
		result.andExpect(status().isCreated());
	    result.andExpect(header().exists("Location"));//Valida linha do codigo que usa o ServletUriComponentsBuilder. Ela gera o endereco do novo recurso (ex: http://localhost/products/1)
	    result.andExpect(jsonPath("$.id").exists());
	    result.andExpect(jsonPath("$.name").value("Phone"));
	}
	
	@Test// cenario de sucesso status 204
	public void deleteShouldReturnNoContentWhenIdExists() throws Exception{
		// act
		ResultActions result = mockMvc.perform(delete("/products/{id}", existingId)
				.accept(MediaType.APPLICATION_JSON));
		
		// assert
		result.andExpect(status().isNoContent());
	}
	
	@Test// cenario de erro (id inexistente - 404)
	public void deleteShouldReturnNotFoundWhenIdDoesNotExist() throws Exception{
		// act
		ResultActions result = mockMvc.perform(delete("/products/{id}", nonExistingId)
				.accept(MediaType.APPLICATION_JSON));
		// assert
		result.andExpect(status().isNotFound());
	}
	
	@Test// cenario de erro de Integridade (400 Bad Request)
	public void deleteShouldReturnBadRequestWhenDependentId() throws Exception{
		//act
		ResultActions result = mockMvc.perform(delete("/products/{id}", dependentId)
				.accept(MediaType.APPLICATION_JSON));
		// assert
		result.andExpect(status().isBadRequest());
	}
	
	@Test
	public void updateShouldReturnProductDTOWhenIdExistis() throws Exception{
		// arrenge: conversao de um objeto JAVA para um objeto JSON
		String jsonBody = objectMapper.writeValueAsString(productDTO);
		
		// act:
		ResultActions result = mockMvc.perform(put("/products/{id}", existingId)
				.content(jsonBody)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON));
		
		// assert
		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.id").exists());
		result.andExpect(jsonPath("$.name").exists());
		result.andExpect(jsonPath("$.description").exists());
	}
	
	@Test
	public void updateShouldReturnNotFoundWhenIdDoesNotExist() throws Exception{
		// arrenge
		String jsonBody = objectMapper.writeValueAsString(productDTO);
		
		// act
		ResultActions result = mockMvc.perform(put("/products/{id}", nonExistingId)
				.content(jsonBody)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON));
		
		// assert
		result.andExpect(status().isNotFound());
	}
	
	@Test
	public void findAllShouldReturnPage()throws Exception{
		//mockMvc.perform(get("/products")).andExpect(status().isOk());// ou mais legível
		
		// act: Executar a ação (Requisição HTTP)
		ResultActions result = mockMvc.perform(get("/products")
				.accept(MediaType.APPLICATION_JSON));
		// assert
		result.andExpect(status().isOk());
	}// perform chama a requiscao e a rota, adiciona andExpect dizendo que se espera 200ok
	
	@Test
	public void findByIdShouldReturnProductWhenIdExists() throws Exception{
		//act
		ResultActions result = mockMvc.perform(get("/products/{id}", existingId)
				.accept(MediaType.APPLICATION_JSON));
		
		//assert
		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.id").exists());
		result.andExpect(jsonPath("$.name").exists());
		result.andExpect(jsonPath("$.description").exists());
	}
	
	@Test
	public void finByIdShouldReturnNotFoundWhenIdDoesNotExist() throws Exception{
		//act
		ResultActions result = mockMvc.perform(get("/products/{id}", nonExistingId)
				.accept(MediaType.APPLICATION_JSON));
		//assert
		result.andExpect(status().isNotFound());
	}
}
