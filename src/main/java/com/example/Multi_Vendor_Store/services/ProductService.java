package com.example.Multi_Vendor_Store.services;

import com.example.Multi_Vendor_Store.dao.ProductRepository;
import com.example.Multi_Vendor_Store.dao.UserRepository;
import com.example.Multi_Vendor_Store.entity.Product;
import com.example.Multi_Vendor_Store.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    // 1. ADDED: Needed for the /all endpoint in the controller
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // 2. Fetch products for a specific vendor
    public List<Product> getProductsByVendor(Long vendorId) {
        return productRepository.findByVendorId(vendorId);
    }

    // 3. Save product with vendor linkage
    public Product saveProduct(Product product, Long vendorId) {
        if (vendorId != null) {
            User vendor = userRepository.findById(vendorId)
                    .orElseThrow(() -> new RuntimeException("Error: Vendor not found with ID: " + vendorId));
            product.setVendor(vendor);
        }
        return productRepository.save(product);
    }

    // 4. Delete product with ownership verification
    public void deleteProductSafely(Long productId, Long vendorId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

        if (!product.getVendor().getId().equals(vendorId)) {
            throw new RuntimeException("Unauthorized: You do not own this product!");
        }

        productRepository.delete(product);
    }

    // 5. Update product with ownership verification
    public Product updateProductSafely(Long productId, Product productRequest, Long vendorId) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

        if (!existingProduct.getVendor().getId().equals(vendorId)) {
            throw new RuntimeException("Unauthorized: You cannot edit a product you do not own!");
        }

        existingProduct.setName(productRequest.getName());
        existingProduct.setDescription(productRequest.getDescription());
        existingProduct.setPrice(productRequest.getPrice());
        existingProduct.setImageUrl(productRequest.getImageUrl());
        existingProduct.setStockQuantity(productRequest.getStockQuantity());

        return productRepository.save(existingProduct);
    }

    // 6. Search functionality of the product
    public List<Product> searchProducts(String keyword) {
        return productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);
    }
}