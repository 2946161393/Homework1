package com.example.product.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductDTO implements Serializable {
    private Long id;
    private String title;
    private Double price;
    private String description;
    private String category;
    private String image;
    private RatingDTO rating;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RatingDTO implements Serializable {
        private Double rate;
        private Integer count;
    }
}