package com.example.booking_system.auth;

import com.example.booking_system.User.Role;
import com.example.booking_system.User.User;
import com.example.booking_system.User.UserRepository;
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
        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.CUSTOMER);
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setFirstName(request.firstName());
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
    public record RegisterRequest(String email, String password, String firstName, String lastName) {}
    public record LoginRequest(String email, String password) {}
    public record AuthResponse(String token) {}
}

