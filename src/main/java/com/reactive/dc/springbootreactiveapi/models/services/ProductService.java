package com.reactive.dc.springbootreactiveapi.models.services;

import com.reactive.dc.springbootreactiveapi.models.documents.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductService {

    Flux<Product> findAll();

    Flux<Product> findAllWithUpperCaseName();

    Flux<Product> findAllWithUpperCaseNameAndRepeat(long repeat);

    Mono<Product> findById(String id);

    Mono<Product> findByName(String name);

    Mono<Product> findByNameQuery(String name);

    Mono<Product> save(Product product);

    Mono<Void> delete(Product product);

    Mono<Product> insert(Product product);

}
