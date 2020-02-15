package com.reactive.dc.springbootreactiveapi.handler;

import com.reactive.dc.springbootreactiveapi.configurations.RouterFunctionConfig;
import com.reactive.dc.springbootreactiveapi.models.documents.Category;
import com.reactive.dc.springbootreactiveapi.models.documents.Product;
import com.reactive.dc.springbootreactiveapi.models.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;

@Component
public class ProductHandler {

    @Autowired
    ProductService productService;

    @Value("${config.uploads.path}")
    private String path;

    @Autowired
    @Qualifier("defaultValidator")
    private Validator validator;

    public Mono<ServerResponse> list(ServerRequest request){
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(productService.findAll(), Product.class);
    }

    public Mono<ServerResponse> getProduct(ServerRequest request){
        String id = request.pathVariable("id");
        return productService.findById(id).flatMap(p -> ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(p)))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> create(ServerRequest request){
        Mono<Product> product = request.bodyToMono(Product.class);

        return product.flatMap(p -> {
            Errors errors = new BeanPropertyBindingResult(p, Product.class.getName());
            validator.validate(p, errors);
            if (errors.hasErrors()) {
                return Flux.fromIterable(errors.getFieldErrors())
                        .map(fieldError -> "The field: ".concat(fieldError.getField()).concat(" ").concat(fieldError.getDefaultMessage()))
                        .collectList()
                        .flatMap(list -> ServerResponse.badRequest().body(BodyInserters.fromValue(list)));
            } else {
                p.setCreateAt(LocalDate.now());
                return productService.save(p).flatMap(proDB -> ServerResponse
                        .created(URI.create(RouterFunctionConfig.URL_BASE.concat(proDB.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(proDB)))
                        .onErrorResume(e -> ServerResponse
                                .badRequest()
                                .body(BodyInserters.fromValue("Some Error")));
            }
        });
    }

    public Mono<ServerResponse> edit(ServerRequest request){
        String id = request.pathVariable("id");
        Mono<Product> product = request.bodyToMono(Product.class);

        Mono<Product> productdb = productService.findById(id);

        return productdb.zipWith(product, (pdb, preq)-> {
            pdb.setName(preq.getName());
            pdb.setPrice(preq.getPrice());
            pdb.setCategory(preq.getCategory());
            return pdb;
        }).flatMap(p -> ServerResponse
                .created(URI.create(RouterFunctionConfig.URL_BASE.concat(p.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .body(productService.save(p), Product.class)
        ).switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> delete(ServerRequest request){

        String id = request.pathVariable("id");
        Mono<Product> productdb = productService.findById(id);

        return productdb.flatMap(p -> productService.delete(p)
                        .then(ServerResponse.noContent().build()))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> uploadPhoto(ServerRequest request){

        String id = request.pathVariable("id");

        return request.multipartData().map(multipart -> multipart.toSingleValueMap().get("file"))
                .cast(FilePart.class)
                .flatMap(file -> productService.findById(id)
                        .flatMap(p -> {
                            p.setPhoto(UUID.randomUUID().toString() + "-" + file.filename()
                                    .replaceAll(" ","-"));
                            return file.transferTo(new File(path + p.getPhoto())).then(productService.save(p));
                        }))
                .flatMap(p -> ServerResponse
                        .created(URI.create(RouterFunctionConfig.URL_BASE.concat(p.getId())))
                        .body(BodyInserters.fromValue(p)))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> createWithPhoto(ServerRequest request){

        Mono<Product> product = request.multipartData().map(multipart -> {
            Category category = Category.builder()
                    .name(((FormFieldPart) multipart.toSingleValueMap().get("category.name")).value())
                    .id(((FormFieldPart) multipart.toSingleValueMap().get("category.id")).value())
                    .build();
            return Product.builder()
                    .name(((FormFieldPart) multipart.toSingleValueMap().get("name")).value())
                    .price(Double.parseDouble(((FormFieldPart) multipart.toSingleValueMap().get("price")).value()))
                    .category(category).build();
        });

        return request.multipartData().map(multipart -> multipart.toSingleValueMap().get("file"))
                .cast(FilePart.class)
                .flatMap(file -> product
                        .flatMap(p -> {
                            p.setPhoto(UUID.randomUUID().toString() + "-" + file.filename()
                                    .replaceAll(" ","-"));
                            p.setCreateAt(LocalDate.now());
                            return file.transferTo(new File(path + p.getPhoto())).then(productService.save(p));
                        }))
                .flatMap(p -> ServerResponse
                        .created(URI.create(RouterFunctionConfig.URL_BASE.concat(p.getId())))
                        .body(BodyInserters.fromValue(p)));
    }

}