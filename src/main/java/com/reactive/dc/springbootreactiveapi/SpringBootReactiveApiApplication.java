package com.reactive.dc.springbootreactiveapi;

import com.reactive.dc.springbootreactiveapi.models.documents.Category;
import com.reactive.dc.springbootreactiveapi.models.documents.Product;
import com.reactive.dc.springbootreactiveapi.models.services.CategoryService;
import com.reactive.dc.springbootreactiveapi.models.services.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

@EnableEurekaClient
@SpringBootApplication
public class SpringBootReactiveApiApplication implements CommandLineRunner {

	@Autowired
	ProductService productService;

	@Autowired
	CategoryService categoryService;

	@Autowired
	ReactiveMongoTemplate reactiveMongoTemplate;

	private static final Logger log = LoggerFactory.getLogger(SpringBootReactiveApiApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactiveApiApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		reactiveMongoTemplate.dropCollection("products").subscribe();
		reactiveMongoTemplate.dropCollection("categories").subscribe();

		Category categoryMac = Category.builder().name("MAC").build();
		Category categoryIphone = Category.builder().name("IPHONE").build();
		Category categoryAirpods = Category.builder().name("AIRPODS").build();
		Category categoryWatch = Category.builder().name("WATCH").build();

		Flux.just(categoryMac, categoryIphone, categoryAirpods, categoryWatch)
				.flatMap(categoryService::save)
				.doOnNext(c -> {
					log.info("categpory created id:{} name:{}", c.getId(), c.getName());
				})
				.thenMany(
						Flux.just(Product.builder().name("MacBook pro").price(3000.0).category(categoryMac).build(),
								Product.builder().name("Iphone 11").price(1000.0).category(categoryIphone).build(),
								Product.builder().name("Airpods 2").price(235.8).category(categoryAirpods).build(),
								Product.builder().name("Apple Watch").price(757.3).category(categoryWatch).build())
								.flatMap(product -> {
									product.setCreateAt(LocalDate.now());
									return productService.insert(product);
								})
				)
				.subscribe(product -> log.info("id: {} name:{}", product.getId(), product.getName()));
	}
}
