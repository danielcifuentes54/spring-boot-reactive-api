package com.reactive.dc.springbootreactiveapi.models.repositories;

import com.reactive.dc.springbootreactiveapi.models.documents.Product;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ProductRepository extends ReactiveMongoRepository<Product, String> {
}
