package com.reactive.dc.springbootreactiveapi.models.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {

    private int status;

    private LocalDateTime dateTime;

    private Object result;

}
