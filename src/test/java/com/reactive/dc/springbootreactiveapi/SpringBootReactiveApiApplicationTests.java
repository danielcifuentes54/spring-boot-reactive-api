package com.reactive.dc.springbootreactiveapi;

import com.reactive.dc.springbootreactiveapi.configurations.RouterFunctionConfig;
import com.reactive.dc.springbootreactiveapi.models.documents.Category;
import com.reactive.dc.springbootreactiveapi.models.documents.Product;
import com.reactive.dc.springbootreactiveapi.models.services.CategoryService;
import com.reactive.dc.springbootreactiveapi.models.services.ProductService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringBootReactiveApiApplicationTests {

	@Autowired
	private WebTestClient client;

	@Autowired
	private ProductService productService;

	@Autowired
	private CategoryService categoryService;

	@Test
	public void listTest() {
		client.get()
				.uri(RouterFunctionConfig.URL_BASE)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBodyList(Product.class)
				.consumeWith(respose -> {
					List<Product> productList = respose.getResponseBody();
					productList.forEach(product -> {
						System.out.println(product.getName());
					});
					Assertions.assertThat(productList.size() > 0).isTrue();
				});
	}

	@Test
	public void getProductTest() {
		Product product = productService.findByName("MacBook pro").block();
		client.get()
				.uri(RouterFunctionConfig.URL_BASE + "{id}", Collections.singletonMap("id", product.getId()))
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody(Product.class)
				.consumeWith(response -> {
					Product p = response.getResponseBody();
					Assertions.assertThat(p.getId()).isNotEmpty();
					Assertions.assertThat(p.getName()).isEqualTo("MacBook pro");
				});
				/*.expectBody()
				.jsonPath("$.id").isNotEmpty()
				.jsonPath("$.name").isEqualTo("MacBook pro");*/
	}

	@Test
	public void createTest(){
		Category category = categoryService.findByName("MAC").block();
		client.post()
				.uri(RouterFunctionConfig.URL_BASE)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(Mono.just(Product.builder()
						.category(category)
						.name("MAC BOOK PRO")
						.price(2500.0)
						.build()), Product.class)
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.id").isNotEmpty()
				.jsonPath("$.name").isEqualTo("MAC BOOK PRO")
				.jsonPath("$.category.name").isEqualTo("MAC");

        client.post()
                .uri(RouterFunctionConfig.URL_BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(Product.builder()
                        .category(category)
                        .name("MAC BOOK PRO")
                        .build()), Product.class)
                .exchange()
                .expectStatus().isBadRequest();


	}

	@Test
	public void editTest(){
		Product product = productService.findByName("MacBook pro").block();
		Category category = categoryService.findByName("MAC").block();

		client.put()
				.uri(RouterFunctionConfig.URL_BASE + "{id}", Collections.singletonMap("id", product.getId()))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(Mono.just(Product.builder()
						.category(category)
						.name("MAC BOOK PRO")
						.price(2500.0)
						.build()), Product.class)
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody(Product.class)
				.consumeWith(response -> {
					Product product1 = response.getResponseBody();
					System.out.println(product1);
					Assertions.assertThat(product1.getId()).isNotEmpty();
					Assertions.assertThat(product1.getName().equals("MAC BOOK PRO")).isTrue();
					Assertions.assertThat(product1.getPrice() == 2500.0).isTrue();
				});
	}

	@Test
	public void deleteTest(){
        Product product = productService.findByName("Iphone 11").block();

        client.delete()
                .uri(RouterFunctionConfig.URL_BASE + "{id}", Collections.singletonMap("id", product.getId()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        client.delete()
                .uri(RouterFunctionConfig.URL_BASE + "{id}", Collections.singletonMap("id", product.getId()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody().isEmpty();

        client.get()
                .uri(RouterFunctionConfig.URL_BASE + "{id}", Collections.singletonMap("id", product.getId()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody().isEmpty();




    }

}
