package com.example.product.controller;

import com.example.product.dto.ProductDTO;
import com.example.product.dto.ProductResponse;
import com.example.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Product API", description = "Third-party FakeStore API Integration")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Get all products", description = "Fetches all products from FakeStore API")
    public ResponseEntity<ProductResponse<List<ProductDTO>>> getAllProducts() {
        log.info("GET /api/v1/products - Fetching all products");

        List<ProductDTO> products = productService.getAllProducts();

        return ResponseEntity.ok(
                ProductResponse.success(products, "FakeStore API", false)
        );
    }


    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Fetches a single product by its ID")
    public ResponseEntity<ProductResponse<ProductDTO>> getProductById(
            @Parameter(description = "Product ID") @PathVariable Long id) {
        log.info("GET /api/v1/products/{} - Fetching product", id);

        ProductDTO product = productService.getProductById(id);

        if (product == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                ProductResponse.success(product, "FakeStore API", false)
        );
    }

    @GetMapping("/categories")
    @Operation(summary = "Get all categories", description = "Fetches all product categories")
    public ResponseEntity<ProductResponse<List<String>>> getAllCategories() {
        log.info("GET /api/v1/products/categories - Fetching all categories");

        List<String> categories = productService.getAllCategories();

        return ResponseEntity.ok(
                ProductResponse.success(categories, "FakeStore API", false)
        );
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get products by category", description = "Fetches products in a specific category")
    public ResponseEntity<ProductResponse<List<ProductDTO>>> getProductsByCategory(
            @Parameter(description = "Category name") @PathVariable String category) {
        log.info("GET /api/v1/products/category/{} - Fetching products", category);

        List<ProductDTO> products = productService.getProductsByCategory(category);

        return ResponseEntity.ok(
                ProductResponse.success(products, "FakeStore API", false)
        );
    }


    @GetMapping("/search/price")
    @Operation(summary = "Search products by price range",
            description = "Filters products within specified price range")
    public ResponseEntity<ProductResponse<List<ProductDTO>>> searchByPrice(
            @Parameter(description = "Minimum price") @RequestParam(defaultValue = "0") Double minPrice,
            @Parameter(description = "Maximum price") @RequestParam(defaultValue = "10000") Double maxPrice) {
        log.info("GET /api/v1/products/search/price?minPrice={}&maxPrice={}", minPrice, maxPrice);

        List<ProductDTO> products = productService.searchProductsByPriceRange(minPrice, maxPrice);

        return ResponseEntity.ok(
                ProductResponse.success(products, "FakeStore API (filtered)", false)
        );
    }


    @GetMapping("/search")
    @Operation(summary = "Search products by keyword",
            description = "Searches products by title or description")
    public ResponseEntity<ProductResponse<List<ProductDTO>>> searchByKeyword(
            @Parameter(description = "Search keyword") @RequestParam String keyword) {
        log.info("GET /api/v1/products/search?keyword={}", keyword);

        List<ProductDTO> products = productService.searchProductsByKeyword(keyword);

        return ResponseEntity.ok(
                ProductResponse.success(products, "FakeStore API (filtered)", false)
        );
    }
}