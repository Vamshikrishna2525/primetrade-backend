package com.primetrade.controller;

import com.primetrade.model.User;
import com.primetrade.repository.UserRepository;
import com.primetrade.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                          UserDetailsService userDetailsService,
                          JwtUtil jwtUtil,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String email = body.get("email");
        String password = body.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body("username and password required");
        }
        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().body("username already exists");
        }

        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(password));
        u.setRole(com.primetrade.model.Role.ROLE_USER);
        userRepository.save(u);
        return ResponseEntity.ok(u);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> creds) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(creds.get("username"), creds.get("password"))
            );
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(401).body(Map.of("error", "invalid credentials"));
        }
        UserDetails ud = userDetailsService.loadUserByUsername(creds.get("username"));
        String token = jwtUtil.generateToken(ud);
        return ResponseEntity.ok(Map.of("token", token));
    }
}
