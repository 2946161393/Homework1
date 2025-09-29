package com.example.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse<T> {
    private T data;
    private String source;
    private boolean cached;
    private long timestamp;
    private String message;

    public static <T> ProductResponse<T> success(T data, String source, boolean cached) {
        return ProductResponse.<T>builder()
                .data(data)
                .source(source)
                .cached(cached)
                .timestamp(System.currentTimeMillis())
                .message("Success")
                .build();
    }

    public static <T> ProductResponse<T> error(String message) {
        return ProductResponse.<T>builder()
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}