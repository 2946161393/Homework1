package com.example.product.service;

import com.example.product.client.FakeStoreClient;
import com.example.product.dto.ProductDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final FakeStoreClient fakeStoreClient;


    @Cacheable(value = "products", key = "'all'", unless = "#result == null or #result.isEmpty()")
    public List<ProductDTO> getAllProducts() {
        log.debug("Fetching all products (cache miss or expired)");
        return fakeStoreClient.getAllProducts();
    }


    @Cacheable(value = "products", key = "#id", unless = "#result == null")
    public ProductDTO getProductById(Long id) {
        log.debug("Fetching product with id: {} (cache miss or expired)", id);
        return fakeStoreClient.getProductById(id);
    }


    @Cacheable(value = "products", key = "'category:' + #category", unless = "#result == null or #result.isEmpty()")
    public List<ProductDTO> getProductsByCategory(String category) {
        log.debug("Fetching products in category: {} (cache miss or expired)", category);
        return fakeStoreClient.getProductsByCategory(category);
    }


    @Cacheable(value = "categories", key = "'all'", unless = "#result == null or #result.isEmpty()")
    public List<String> getAllCategories() {
        log.debug("Fetching all categories (cache miss or expired)");
        return fakeStoreClient.getAllCategories();
    }


    public List<ProductDTO> searchProductsByPriceRange(Double minPrice, Double maxPrice) {
        log.debug("Searching products with price range: {} - {}", minPrice, maxPrice);

        List<ProductDTO> allProducts = getAllProducts();

        return allProducts.stream()
                .filter(product -> product.getPrice() != null)
                .filter(product -> product.getPrice() >= minPrice && product.getPrice() <= maxPrice)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> searchProductsByKeyword(String keyword) {
        log.debug("Searching products with keyword: {}", keyword);

        List<ProductDTO> allProducts = getAllProducts();
        String lowerKeyword = keyword.toLowerCase();

        return allProducts.stream()
                .filter(product ->
                        (product.getTitle() != null && product.getTitle().toLowerCase().contains(lowerKeyword)) ||
                                (product.getDescription() != null && product.getDescription().toLowerCase().contains(lowerKeyword))
                )
                .collect(Collectors.toList());
    }
}