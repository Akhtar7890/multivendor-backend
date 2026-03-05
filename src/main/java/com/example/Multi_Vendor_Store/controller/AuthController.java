package com.example.Multi_Vendor_Store.controller;


import com.example.Multi_Vendor_Store.dto.request.LoginRequest;
import com.example.Multi_Vendor_Store.dto.request.SignupRequest;
import com.example.Multi_Vendor_Store.dto.response.JwtResponse;
import com.example.Multi_Vendor_Store.dto.response.MessageResponse;
import com.example.Multi_Vendor_Store.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "https://multivendor-frontend-two.vercel.app/", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/signing")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // Ensure AuthService.authenticateUser returns a JwtResponse object
        JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        // Ensure AuthService.registerUser returns a String
        String result = authService.registerUser(signUpRequest);

        if (result.startsWith("Error")) {
            return ResponseEntity.badRequest().body(new MessageResponse(result));
        }

        return ResponseEntity.ok(new MessageResponse(result));
    }
}
