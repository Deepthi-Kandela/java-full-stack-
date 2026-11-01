package com.thinkmart.security;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.thinkmart.model.User;
import com.thinkmart.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Reads "Authorization: Bearer <token>", loads the user and exposes it to controllers as the
 * request attribute "currentUser". Anything under /api/** that is not public needs a valid token.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String CURRENT_USER = "currentUser";

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public AuthInterceptor(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            return true; // CORS pre-flight
        }

        User user = null;
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            Long id = jwtUtil.parseUserId(header.substring(7).trim());
            if (id != null) {
                user = userRepository.findById(id).orElse(null);
            }
        }
        if (user != null) {
            req.setAttribute(CURRENT_USER, user);
        }

        if (user == null && !isPublic(req)) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json");
            res.setCharacterEncoding("UTF-8");
            res.getWriter().write("{\"message\":\"Please log in to continue\"}");
            return false;
        }
        return true;
    }

    private boolean isPublic(HttpServletRequest req) {
        String path = req.getRequestURI();
        String method = req.getMethod();
        if (path.equals("/api/auth/login") || path.equals("/api/auth/register")) {
            return true;
        }
        if (path.equals("/api/contact") && "POST".equalsIgnoreCase(method)) {
            return true;
        }
        // Browsing the catalogue does not need an account
        return "GET".equalsIgnoreCase(method)
                && path.startsWith("/api/products")
                && !path.equals("/api/products/mine");
    }
}
