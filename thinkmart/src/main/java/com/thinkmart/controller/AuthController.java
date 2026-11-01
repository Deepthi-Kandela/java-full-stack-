package com.thinkmart.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thinkmart.dto.AuthResponse;
import com.thinkmart.dto.LoginRequest;
import com.thinkmart.dto.RegisterRequest;
import com.thinkmart.dto.UserDto;
import com.thinkmart.model.User;
import com.thinkmart.security.AuthInterceptor;
import com.thinkmart.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** multipart/form-data so that a profile picture can be uploaded together with the fields. */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @ModelAttribute RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserDto me(@RequestAttribute(AuthInterceptor.CURRENT_USER) User user) {
        return UserDto.from(user);
    }
}
