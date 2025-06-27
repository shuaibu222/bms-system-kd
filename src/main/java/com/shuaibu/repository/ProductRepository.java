package com.shuaibu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.shuaibu.model.ProductModel;

@Repository
public interface ProductRepository extends JpaRepository<ProductModel, Long> {
    boolean existsByNameAndIdNot(String name, Long id);

    ProductModel findByName(String productName);
}