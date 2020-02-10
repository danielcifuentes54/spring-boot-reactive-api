package com.reactive.dc.springbootreactiveapi.models.documents;

import com.reactive.dc.springbootreactiveapi.models.api.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Document(collection = "products")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    @Id
    private String id;

    @NotEmpty
    private String name;

    @NotNull
    private Double price;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate createAt;

    @Valid
    private Category category;

    private String photo;


}
