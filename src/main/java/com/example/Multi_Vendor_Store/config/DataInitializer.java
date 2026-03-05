package com.example.Multi_Vendor_Store.config;

import com.example.Multi_Vendor_Store.entity.ERole;
import com.example.Multi_Vendor_Store.entity.Role;
import com.example.Multi_Vendor_Store.dao.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initRoles(RoleRepository roleRepository) {
        return args -> {
            // Ensure the three roles exist
            if (roleRepository.findByName(ERole.ROLE_CUSTOMER).isEmpty()) {
                roleRepository.save(new Role(ERole.ROLE_CUSTOMER));
            }
            if (roleRepository.findByName(ERole.ROLE_VENDOR).isEmpty()) {
                roleRepository.save(new Role(ERole.ROLE_VENDOR));
            }
            if (roleRepository.findByName(ERole.ROLE_ADMIN).isEmpty()) {
                roleRepository.save(new Role(ERole.ROLE_ADMIN));
            }
        };
    }
}

