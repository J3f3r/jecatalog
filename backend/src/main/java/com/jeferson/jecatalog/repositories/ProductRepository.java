package com.jeferson.jecatalog.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jeferson.jecatalog.entities.Product;

public interface ProductRepository extends JpaRepository<Product, Long>{

}
