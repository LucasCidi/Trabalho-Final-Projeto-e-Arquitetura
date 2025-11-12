package com.example.gateway.controller;

import com.example.gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if ("admin".equals(request.username()) && "123".equals(request.password())) {
            String token = jwtUtil.generateToken("admin", List.of("ADMIN", "USER"));
            return ResponseEntity.ok(new LoginResponse(token));
        }
        return ResponseEntity.status(401).body("Credenciais inválidas");
    }
}

record LoginRequest(String username, String password) {}
record LoginResponse(String token) {}
