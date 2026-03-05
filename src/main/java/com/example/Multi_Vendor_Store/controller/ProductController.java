package com.example.Multi_Vendor_Store.controller;

import com.example.Multi_Vendor_Store.dto.response.MessageResponse;
import com.example.Multi_Vendor_Store.dto.response.ProductResponse;
import com.example.Multi_Vendor_Store.entity.Product;
import com.example.Multi_Vendor_Store.security.services.UserDetailsImpl;
import com.example.Multi_Vendor_Store.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "https://multivendor-frontend-two.vercel.app/")
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    // 1. PUBLIC: Get all products (Now added)
    @GetMapping("/")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        List<ProductResponse> response = products.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // 2. PUBLIC: Health check
    @GetMapping("/health")
    public String healthCheck() {
        return "ProductController is up and running!";
    }

    // 3. VENDOR ONLY: Get only my products
    @GetMapping("/my-products")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<List<ProductResponse>> getMyProducts(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<Product> products = productService.getProductsByVendor(userDetails.getId());
        List<ProductResponse> response = products.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // 4. VENDOR/ADMIN: Add a product
    @PostMapping("/add")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ProductResponse> addProduct(@RequestBody Product product, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Product savedProduct = productService.saveProduct(product, userDetails.getId());
        return ResponseEntity.ok(convertToDto(savedProduct));
    }

    // 5. OWNER/ADMIN: Update a product
    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody Product productRequest, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        try {
            assert userDetails != null;
            Product updatedProduct = productService.updateProductSafely(id, productRequest, userDetails.getId());
            return ResponseEntity.ok(convertToDto(updatedProduct));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponse(e.getMessage()));
        }
    }

    // 6. OWNER/ADMIN: Delete a product
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        try {
            assert userDetails != null;
            productService.deleteProductSafely(id, userDetails.getId());
            return ResponseEntity.ok(new MessageResponse("Product deleted successfully!"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponse(e.getMessage()));
        }
    }

    // Helper to keep logic clean
    private ProductResponse convertToDto(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getImageUrl(),
                product.getStockQuantity(),
                product.getVendor() != null ? product.getVendor().getUsername() : "Unknown Vendor"
        );
    }

    // 7. PUBLIC: Search products by keyword
    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(@RequestParam String keyword) {
        List<Product> products = productService.searchProducts(keyword);

        List<ProductResponse> response = products.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}