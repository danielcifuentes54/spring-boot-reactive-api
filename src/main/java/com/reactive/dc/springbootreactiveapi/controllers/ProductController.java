package com.reactive.dc.springbootreactiveapi.controllers;

import com.reactive.dc.springbootreactiveapi.models.api.ApiResponse;
import com.reactive.dc.springbootreactiveapi.models.documents.Product;
import com.reactive.dc.springbootreactiveapi.models.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.io.File;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping(ProductController.URL_BASE)
public class ProductController {

    public static final String URL_BASE =  "/api/products/";

    @Value("${config.uploads.path}")
    private String path;

    @Autowired
    private ProductService productService;

    @GetMapping
    public Flux<Product> getAllProducts(){
        return productService.findAll();
    }

    @GetMapping("list")
    public Mono<ResponseEntity<Flux<Product>>> getAllProducts2(){
        return Mono.just(
                ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(productService.findAll())
        );
    }

    @GetMapping("{id}")
    public Mono<ResponseEntity<Product>> getProduct(@PathVariable String id){
        return productService.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(ex -> Mono.just(ResponseEntity.badRequest().build()));
    }

    @PostMapping
    public Mono<ResponseEntity<ApiResponse>> create(@Valid @RequestBody Mono<Product> monoProduct){
        
        return monoProduct.flatMap(product -> {
            product.setCreateAt(LocalDate.now());

            return productService.save(product)
                    .map(p -> ResponseEntity
                            .created(URI.create(URL_BASE.concat(p.getId())))
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(ApiResponse.builder()
                                    .status(HttpStatus.OK.value())
                                    .dateTime(LocalDateTime.now())
                                    .result(p).build()));
        }).onErrorResume(t -> Mono.just(t).cast(WebExchangeBindException.class)
                    .flatMap(e -> Mono.just(e.getFieldErrors())
                    .flatMapMany(Flux::fromIterable)
                    .map(fieldError -> "The field: " + fieldError.getField() + " " + fieldError.getDefaultMessage())
                    .collectList()
                    .flatMap(list -> Mono.just(ResponseEntity.badRequest().body(ApiResponse.builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .dateTime(LocalDateTime.now())
                            .result(list).build())))));
    }

    @PutMapping("{id}")
    public Mono<ResponseEntity<Product>> update(@RequestBody Product product, @PathVariable String id){

        return productService.findById(id)
                .flatMap(p -> {
                    p.setName(product.getName());
                    p.setPrice(product.getPrice());
                    p.setCategory(product.getCategory());
                    return productService.save(p);
                }).map(p -> ResponseEntity
                        .created(URI.create(URL_BASE.concat(p.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(p)
                ).defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable String id){
        return productService.findById(id)
                .flatMap(p -> productService.delete(p)
                        .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT))))
                .defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/upload/{id}")
    public Mono<ResponseEntity> uploadImage(@PathVariable String id, @RequestPart FilePart file){
        return productService.findById(id).flatMap(p -> {
            p.setPhoto(UUID.randomUUID().toString().concat("-").concat(file.filename()
            .replaceAll(" ", "")));

            return file.transferTo(new File(path + p.getPhoto())).then(productService.save(p));
        }).map(ResponseEntity::ok);

    }

    @PostMapping("/v2/")
    public Mono<ResponseEntity<Product>> saveAndUploadPhoto(Product product, @RequestPart FilePart file){

        product.setCreateAt(LocalDate.now());
        product.setPhoto(UUID.randomUUID().toString().concat("-").concat(file.filename().replaceAll(" ", "")));

        return file.transferTo(new File(path + product.getPhoto())).then(productService.save(product))
                .map(p -> ResponseEntity
                        .created(URI.create(URL_BASE.concat(p.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(p)
                );
    }

 }
