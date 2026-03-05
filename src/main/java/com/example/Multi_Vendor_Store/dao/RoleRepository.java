package com.example.Multi_Vendor_Store.dao;

import com.example.Multi_Vendor_Store.entity.ERole;
import com.example.Multi_Vendor_Store.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
}
