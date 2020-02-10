package com.reactive.dc.springbootreactiveapi.models.services;

import com.reactive.dc.springbootreactiveapi.models.documents.Category;
import com.reactive.dc.springbootreactiveapi.models.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CategoyServiceImpl implements CategoryService {

    @Autowired
    CategoryRepository categoryRepository;

    @Override
    public Flux<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Override
    public Mono<Category> findById(String id) {
        return categoryRepository.findById(id);
    }

    @Override
    public Mono<Category> save(Category category) {
        return categoryRepository.save(category);
    }
}
