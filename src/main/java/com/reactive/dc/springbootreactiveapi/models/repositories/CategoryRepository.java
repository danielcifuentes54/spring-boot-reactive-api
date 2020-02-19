package com.reactive.dc.springbootreactiveapi.models.repositories;

import com.reactive.dc.springbootreactiveapi.models.documents.Category;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface CategoryRepository extends ReactiveMongoRepository<Category, String> {

    Mono<Category> findCategoryByName(String name);
}
