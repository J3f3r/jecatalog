package com.jeferson.jecatalog.services;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeferson.jecatalog.dto.CategoryDTO;
import com.jeferson.jecatalog.dto.ProductDTO;
import com.jeferson.jecatalog.entities.Category;
import com.jeferson.jecatalog.entities.Product;
import com.jeferson.jecatalog.projection.ProductProjection;
import com.jeferson.jecatalog.repositories.CategoryRepository;
import com.jeferson.jecatalog.repositories.ProductRepository;
import com.jeferson.jecatalog.services.exceptions.DatabaseException;
import com.jeferson.jecatalog.services.exceptions.ResourceNotFoundException;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ProductService {

	@Autowired
	private ProductRepository repository;
	
	@Autowired
	private CategoryRepository categoryRepository;
	
	@Transactional(readOnly = true)
	public Page<ProductDTO> findAllPaged(Pageable pageable){
		Page<Product> list = repository.findAll(pageable);
		return list.map(x -> new ProductDTO(x));
	}

	@Transactional(readOnly = true)
	public ProductDTO findById(Long id) {
		Optional <Product> obj = repository.findById(id);
		Product entity = obj.orElseThrow(() -> new ResourceNotFoundException("Entity not found"));
		return new ProductDTO(entity, entity.getCategories());
	}

	@Transactional
	public ProductDTO insert(ProductDTO dto) {
		Product entity = new Product();
		copyDtoToEntity(dto, entity);
		entity = repository.save(entity);
		return new ProductDTO(entity);
	}

	@Transactional
	public ProductDTO update(Long id, ProductDTO dto) {
		try {
			Product entity = repository.getReferenceById(id);
			copyDtoToEntity(dto, entity);
			entity = repository.save(entity);
			return new ProductDTO(entity);
		}
		catch(EntityNotFoundException e) {
			throw new ResourceNotFoundException("Id noy found " + id);
		}
	}

	public void delete(Long id) {
		if( !repository.existsById(id)) {
			throw new ResourceNotFoundException("Resource not found");
		}
		try {
			repository.deleteById(id);
		}
		catch (DataIntegrityViolationException e) {
			throw new DatabaseException("Integrity violation");
		}
	}
	
	private void copyDtoToEntity(ProductDTO dto, Product entity) {
		// metodo chamado para evitar escrita desnecessaria
		entity.setName(dto.getName());
		entity.setDescription(dto.getDescription());
		entity.setDate(dto.getDate());
		entity.setImgUrl(dto.getImgUrl());
		entity.setPrice(dto.getPrice());
		
		entity.getCategories().clear();
		
		for(CategoryDTO catDto : dto.getCategories()) {
			Category category = categoryRepository.getReferenceById(catDto.getId());// NAO vai tocar no banco de dados
			entity.getCategories().add(category);
		}
	}

	@Transactional(readOnly = true)
	public Page<ProductDTO> findAllPaged(String name, String categoryId, Pageable pageable) {
		
//		String[] vet = categoryId.split(",");// posicao [0] temos a pag 1 e na posicao [1] a pag 3
//		List<String> list = Arrays.asList(vet);// gerei um vetor de string, com esse vetor gerei uma lista para converte-la em Long
//		List<Long> categoryId = list.stream().map(x -> Long.parseLong(x)).toList();// essas 3 linhas serao resumidas na linha abaixo
//		List<Long> categoryIds = Arrays.asList(categoryId.split(",")).stream().map(Long::parseLong).toList();

		List<Long> categoryIds = Arrays.asList();
		if (!"0".equals(categoryId)) {
			categoryIds = Arrays.asList(categoryId.split(",")).stream().map(Long::parseLong).toList();
		}
		Page<ProductProjection> page =repository.searchProducts(categoryIds, name, pageable);		
		List<Long> productIds = page.map(x -> x.getId()).toList();
		
		List<Product> entities = repository.searchProductWithCategories(productIds);
		List<ProductDTO> dtos = entities.stream().map(p -> new ProductDTO(p, p.getCategories())).toList();
		
		return new PageImpl<>(dtos, page.getPageable(), page.getTotalElements());
	}
}
