package com.example.booking_system.auth;

import com.example.booking_system.exception.ValidationException;
import com.example.booking_system.user.Role;
import com.example.booking_system.user.User;
import com.example.booking_system.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ValidationException("Email already registered");
        }

        Role role;
        if (request.role() == null) {
            // If the role is not specified, we give CUSTOMER
            role = Role.CUSTOMER;
        } else if (request.role() == Role.ADMIN) {
            // ADMIN can only register if there are no users in the database yet.
            if (userRepository.count() > 0) {
                throw new ValidationException("Admin already exists. Only one admin allowed.");
            }
            role = Role.ADMIN;
        } else if (request.role() == Role.EMPLOYEE) {
            // EMPLOYEE НЕ МОЖЕТ зарегистрироваться сам — только через админа
            throw new ValidationException("Employees can only be created by an admin.");
        } else {
            role = Role.CUSTOMER;
        }

        // Create a user
        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(role);
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setStatus(true);

        userRepository.save(user);
        
        UserPrincipal userPrincipal = new UserPrincipal(user);

        String token = jwtService.generateToken(userPrincipal);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow();

        // FIX: Wrap the user entity into your UserPrincipal wrapper
        UserPrincipal userPrincipal = new UserPrincipal(user);

        String token = jwtService.generateToken(userPrincipal);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    // DTOs (records — clean for this)
// Match the exact naming convention from your JSON payload
    public record RegisterRequest(String email, String password, String firstName, String lastName, Role role) {}
    public record LoginRequest(String email, String password) {}
    public record AuthResponse(String token) {}
}

