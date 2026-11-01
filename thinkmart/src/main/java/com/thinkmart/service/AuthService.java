package com.thinkmart.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thinkmart.dto.AuthResponse;
import com.thinkmart.dto.LoginRequest;
import com.thinkmart.dto.RegisterRequest;
import com.thinkmart.dto.UserDto;
import com.thinkmart.exception.ApiException;
import com.thinkmart.model.Role;
import com.thinkmart.model.User;
import com.thinkmart.repository.UserRepository;
import com.thinkmart.security.JwtUtil;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final FileStorageService fileStorage;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil, FileStorageService fileStorage) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.fileStorage = fileStorage;
    }

    @Transactional
    public AuthResponse register(RegisterRequest r) {
        Role role;
        try {
            role = Role.valueOf(r.getRole().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Role must be Customer or Vendor");
        }
        if (role == Role.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Admin accounts cannot be created from the sign-up form");
        }
        if (userRepository.existsByEmailIgnoreCase(r.getEmail().trim())) {
            throw new ApiException(HttpStatus.CONFLICT, "An account with this email already exists");
        }

        User u = new User();
        u.setFirstName(r.getFirstName().trim());
        u.setLastName(r.getLastName().trim());
        u.setEmail(r.getEmail().trim().toLowerCase());
        u.setPhone(r.getPhone());
        u.setPasswordHash(passwordEncoder.encode(r.getPassword()));
        u.setDob(r.getDob());
        u.setGender(r.getGender());
        u.setState(r.getState());
        u.setAddress(r.getAddress().trim());
        u.setRole(role);
        u.setProfilePic(fileStorage.saveProfilePic(r.getPic()));
        userRepository.save(u);

        return new AuthResponse(jwtUtil.generate(u), UserDto.from(u));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        User u = userRepository.findByEmailIgnoreCase(req.email().trim()).orElse(null);
        if (u == null || !passwordEncoder.matches(req.password(), u.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        return new AuthResponse(jwtUtil.generate(u), UserDto.from(u));
    }
}
