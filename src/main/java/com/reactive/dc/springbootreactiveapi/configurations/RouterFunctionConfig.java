package com.reactive.dc.springbootreactiveapi.configurations;

import com.reactive.dc.springbootreactiveapi.handler.ProductHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterFunctionConfig {

    public static final String URL_BASE =  "/api/v2/products/";

    @Bean
    public RouterFunction<ServerResponse> routes(ProductHandler handler){
        return route(GET(URL_BASE).or(GET(URL_BASE+"list")), handler::list)
                .andRoute(GET(URL_BASE + "{id}"), handler::getProduct)
                .andRoute(POST(URL_BASE), handler::create)
                .andRoute(PUT(URL_BASE + "{id}"), handler::edit)
                .andRoute(DELETE(URL_BASE + "{id}"), handler::delete)
                .andRoute(POST(URL_BASE+ "upload/{id}"), handler::uploadPhoto)
                .andRoute(POST(URL_BASE+ "create"), handler::createWithPhoto);

    }

}
