package com.example.Multi_Vendor_Store.services;


import com.example.Multi_Vendor_Store.dao.RoleRepository;
import com.example.Multi_Vendor_Store.dao.UserRepository;
import com.example.Multi_Vendor_Store.dto.request.LoginRequest;
import com.example.Multi_Vendor_Store.dto.request.SignupRequest;
import com.example.Multi_Vendor_Store.dto.response.JwtResponse;
import com.example.Multi_Vendor_Store.entity.ERole;
import com.example.Multi_Vendor_Store.entity.Role;
import com.example.Multi_Vendor_Store.entity.User;
import com.example.Multi_Vendor_Store.security.jwt.JwtUtils;
import com.example.Multi_Vendor_Store.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    JwtUtils jwtUtils;

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        // FIX: Cast to UserDetailsImpl, NOT UserDetailsServiceImpl
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        assert userDetails != null;
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return new JwtResponse(jwt, userDetails.getId(), userDetails.getEmail(), roles);
    }

    public String registerUser(SignupRequest signUpRequest) {
        if (signUpRequest.getEmail() == null || signUpRequest.getEmail().isBlank()) {
            return "Error: Email cannot be blank!";
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return "Error: Email is already in use!";
        }

        // Determine username: if user provided one (non-blank) use it, otherwise fall back to email
        String usernameToUse = (signUpRequest.getUsername() != null && !signUpRequest.getUsername().isBlank())
                ? signUpRequest.getUsername().trim()
                : signUpRequest.getEmail();

        if (userRepository.existsByUsername(usernameToUse)) {
            return "Error: Username is already taken!";
        }

        if (signUpRequest.getPassword() == null || signUpRequest.getPassword().isBlank()) {
            return "Error: Password cannot be blank!";
        }

        User user = new User();
        user.setEmail(signUpRequest.getEmail());
        user.setUsername(usernameToUse);
        user.setPassword(encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRoles();
        System.out.println("DEBUG: Received roles: " + strRoles);

        Set<Role> roles = new HashSet<>();

        // Helper to resolve a role by ERole; if missing, create and save it (resilient to startup order)
        java.util.function.Function<ERole, Role> resolveRole = (er) -> {
            return roleRepository.findByName(er).orElseGet(() -> roleRepository.save(new Role(er)));
        };

        if (strRoles == null || strRoles.isEmpty()) {
            System.out.println("DEBUG: No roles provided, defaulting to CUSTOMER");
            roles.add(resolveRole.apply(ERole.ROLE_CUSTOMER));
        } else {
            for (String role : strRoles) {
                String normalizedRole = role.trim().toUpperCase();

                switch (normalizedRole) {
                    case "ROLE_ADMIN":
                        roles.add(resolveRole.apply(ERole.ROLE_ADMIN));
                        break;

                    case "ROLE_VENDOR":
                        roles.add(resolveRole.apply(ERole.ROLE_VENDOR));
                        break;

                    case "ROLE_CUSTOMER":
                        roles.add(resolveRole.apply(ERole.ROLE_CUSTOMER));
                        break;

                    default:
                        // Safety fallback
                        roles.add(resolveRole.apply(ERole.ROLE_CUSTOMER));
                }
            }
        }

        System.out.println("DEBUG: Final roles for user: " + roles.stream().map(Role::getName).collect(Collectors.toSet()));

        user.setRoles(roles);
        userRepository.save(user);
        System.out.println("DEBUG: User saved with email: " + signUpRequest.getEmail() + " and roles: " + user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
        return "User registered successfully!";
    }
}
