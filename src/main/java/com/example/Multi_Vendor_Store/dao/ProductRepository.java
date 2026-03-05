package com.example.Multi_Vendor_Store.dao;

import com.example.Multi_Vendor_Store.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Spring generates the query automatically based on the 'vendor' field in Product entity
    List<Product> findByVendorId(Long vendorId);

    // New: Search for products by name or description (case-insensitive)
    List<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);
}