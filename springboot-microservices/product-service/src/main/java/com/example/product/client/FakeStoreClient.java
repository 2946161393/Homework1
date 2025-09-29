package com.example.product.client;

public class FakeStoreClient {
}
package com.example.product.client;

import com.example.product.dto.ProductDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * FakeStore API Client
 * 1. RestTemplate.exchange()
 * 2. @CircuitBreaker
 * 3. @Retry
 * 4. ParameterizedTypeReference
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FakeStoreClient {

    private final RestTemplate restTemplate;

    @Value("${fakestore.api.base-url}")
    private String baseUrl;

    /**
     *
     * - RestTemplate.exchange(): HTTP GET
     * - ParameterizedTypeReference: List<ProductDTO>
     *
     * @return
     */
    @CircuitBreaker(name = "fakeStoreApi", fallbackMethod = "getAllProductsFallback")
    @Retry(name = "fakeStoreApi")
    public List<ProductDTO> getAllProducts() {
        log.info("Calling FakeStore API to get all products");

        try {
            String url = baseUrl + "/products";

            // 使用 exchange 方法发送 GET 请求并接收 List<ProductDTO>
            ResponseEntity<List<ProductDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<ProductDTO>>() {}
            );

            log.info("Successfully fetched {} products from FakeStore API",
                    response.getBody() != null ? response.getBody().size() : 0);

            return response.getBody();
        } catch (RestClientException e) {
            log.error("Error calling FakeStore API: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * ID
     *
     *
     * - RestTemplate.getForObject(): GET
     *
     * @param id ID
     * @return product info
     */
    @CircuitBreaker(name = "fakeStoreApi", fallbackMethod = "getProductByIdFallback")
    @Retry(name = "fakeStoreApi")
    public ProductDTO getProductById(Long id) {
        log.info("Calling FakeStore API to get product with id: {}", id);

        try {
            String url = baseUrl + "/products/" + id;

            // 使用 getForObject 方法发送 GET 请求
            ProductDTO product = restTemplate.getForObject(url, ProductDTO.class);

            log.info("Successfully fetched product: {}", product != null ? product.getTitle() : "null");

            return product;
        } catch (RestClientException e) {
            log.error("Error calling FakeStore API for product {}: {}", id, e.getMessage());
            throw e;
        }
    }

    /**
     *
     *
     * @param category
     * @return
     */
    @CircuitBreaker(name = "fakeStoreApi", fallbackMethod = "getProductsByCategoryFallback")
    @Retry(name = "fakeStoreApi")
    public List<ProductDTO> getProductsByCategory(String category) {
        log.info("Calling FakeStore API to get products in category: {}", category);

        try {
            String url = baseUrl + "/products/category/" + category;

            ResponseEntity<List<ProductDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<ProductDTO>>() {}
            );

            log.info("Successfully fetched {} products in category '{}'",
                    response.getBody() != null ? response.getBody().size() : 0, category);

            return response.getBody();
        } catch (RestClientException e) {
            log.error("Error calling FakeStore API for category {}: {}", category, e.getMessage());
            throw e;
        }
    }

    /**
     *
     *
     * @return
     */
    @CircuitBreaker(name = "fakeStoreApi", fallbackMethod = "getAllCategoriesFallback")
    @Retry(name = "fakeStoreApi")
    public List<String> getAllCategories() {
        log.info("Calling FakeStore API to get all categories");

        try {
            String url = baseUrl + "/products/categories";

            ResponseEntity<List<String>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<String>>() {}
            );

            log.info("Successfully fetched {} categories",
                    response.getBody() != null ? response.getBody().size() : 0);

            return response.getBody();
        } catch (RestClientException e) {
            log.error("Error calling FakeStore API for categories: {}", e.getMessage());
            throw e;
        }
    }

    // ==================== Fallback Methods ====================

    /**
     * getAllProducts
     * Circuit Breaker
     */
    private List<ProductDTO> getAllProductsFallback(Exception e) {
        log.warn("FakeStore API unavailable, returning empty product list. Error: {}", e.getMessage());
        return new ArrayList<>();
    }

    /**
     * getProductById
     */
    private ProductDTO getProductByIdFallback(Long id, Exception e) {
        log.warn("FakeStore API unavailable, returning fallback product for id: {}. Error: {}",
                id, e.getMessage());

        // 返回一个默认产品
        return ProductDTO.builder()
                .id(id)
                .title("Product Unavailable")
                .description("The product service is temporarily unavailable")
                .price(0.0)
                .category("unknown")
                .build();
    }

    /**
     * getProductsByCategory
     */
    private List<ProductDTO> getProductsByCategoryFallback(String category, Exception e) {
        log.warn("FakeStore API unavailable, returning empty list for category: {}. Error: {}",
                category, e.getMessage());
        return new ArrayList<>();
    }

    /**
     * getAllCategories
     */
    private List<String> getAllCategoriesFallback(Exception e) {
        log.warn("FakeStore API unavailable, returning default categories. Error: {}", e.getMessage());
        return List.of("electronics", "jewelery", "men's clothing", "women's clothing");
    }
}