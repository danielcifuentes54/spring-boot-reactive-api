package com.reactive.dc.springbootreactiveapi.models.repositories;

import com.reactive.dc.springbootreactiveapi.models.documents.Category;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface CategoryRepository extends ReactiveMongoRepository<Category, String> {
}
