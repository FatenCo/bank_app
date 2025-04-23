package com.bank.app.lettrage.controller;

import com.bank.app.lettrage.configuration.JwtUtils;
import com.bank.app.lettrage.entity.User;
import com.bank.app.lettrage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        // 1️⃣ unicité
        if (userRepository.findByUsername(user.getUsername()) != null) {
            return ResponseEntity.badRequest().body("Nom d'utilisateur déjà utilisé.");
        }

        // 2️⃣ encode mot de passe
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 3️⃣ récupère le rôle envoyé, ou force USER
        String incomingRole = user.getRole() == null ? "" : user.getRole().toUpperCase();
        if (!"ADMIN".equals(incomingRole) && !"USER".equals(incomingRole)) {
            incomingRole = "USER";
        }
        user.setRole(incomingRole);

        // 4️⃣ initialise les autres champs
        user.setLastPasswordChange(new Date());
        user.setFailedAttempts(0);
        user.setLocked(false);

        // 5️⃣ save
        User saved = userRepository.save(user);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Identifiant ou mot de passe incorrect.");
        }
        if (user.isLocked()) {
            return ResponseEntity.status(HttpStatus.LOCKED)
                    .body("Votre compte est bloqué suite à 3 tentatives échouées.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
        } catch (BadCredentialsException ex) {
            int attempts = user.getFailedAttempts() + 1;
            user.setFailedAttempts(attempts);
            if (attempts >= 3) {
                user.setLocked(true);
            }
            userRepository.save(user);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Identifiant ou mot de passe incorrect.");
        }

        // reset attempts
        user.setFailedAttempts(0);
        userRepository.save(user);

        // génère token + renvoie rôle
        String token = jwtUtils.generateToken(user.getUsername());
        Map<String,Object> data = new HashMap<>();
        data.put("token", token);
        data.put("type", "Bearer");
        data.put("role", user.getRole());
        return ResponseEntity.ok(data);
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String,String> m) {
        String username = m.get("username"), newPassword = m.get("newPassword");
        if (username == null || newPassword == null) {
            return ResponseEntity.badRequest().body("username et newPassword sont requis.");
        }
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur introuvable.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setLastPasswordChange(new Date());
        user.setFailedAttempts(0);
        user.setLocked(false);
        userRepository.save(user);
        return ResponseEntity.ok("Mot de passe mis à jour avec succès.");
    }
}
