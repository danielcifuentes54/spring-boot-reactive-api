package com.reactive.dc.springbootreactiveapi.models.repositories;

import com.reactive.dc.springbootreactiveapi.models.documents.Product;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ProductRepository extends ReactiveMongoRepository<Product, String> {

    Mono<Product> findProductByName(String name);

    @Query("{ 'nombre': ?0 }")
    Mono<Product> getProductByNameQuery(String name);
}
