package com.bank.app.lettrage.controller;

import com.bank.app.lettrage.configuration.JwtUtils;
import com.bank.app.lettrage.entity.User;
import com.bank.app.lettrage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    // 📌 Enregistrement utilisateur
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()) != null) {
            return ResponseEntity.badRequest().body("Nom d'utilisateur déjà utilisé.");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");
        user.setLastPasswordChange(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        user.setFailedAttempts(0);
        user.setLocked(false);

        return ResponseEntity.ok(userRepository.save(user));
    }

    // 📌 Connexion utilisateur
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        Optional<User> optionalUser = Optional.ofNullable(userRepository.findByUsername(loginRequest.getUsername()));

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Identifiant ou mot de passe incorrect.");
        }

        User user = optionalUser.get();

        if (Boolean.TRUE.equals(user.isLocked())) {
            return ResponseEntity.status(HttpStatus.LOCKED).body("Votre compte est bloqué suite à 3 tentatives échouées.");
        }

        try {
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                // 🔄 Incrémenter les tentatives échouées
                int attempts = user.getFailedAttempts() + 1;
                user.setFailedAttempts(attempts);

                if (attempts >= 3) {
                    user.setLocked(true);
                    userRepository.save(user);
                    return ResponseEntity.status(HttpStatus.LOCKED)
                            .body("Votre compte a été bloqué après 3 tentatives échouées.");
                }

                userRepository.save(user);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Identifiant ou mot de passe incorrect.");
            }

            // 🔒 Authentification réussie
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            if (authentication.isAuthenticated()) {
                // ✅ Réinitialiser les tentatives échouées
                user.setFailedAttempts(0);
                userRepository.save(user);

                // 📅 Vérification expiration mot de passe
                long daysSinceLastChange = ChronoUnit.DAYS.between(
                        user.getLastPasswordChange().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                        LocalDateTime.now()
                );
                boolean passwordExpired = daysSinceLastChange >= 30;

                // 🎫 Génération token JWT
                Map<String, Object> authData = new HashMap<>();
                authData.put("token", jwtUtils.generateToken(user.getUsername()));
                authData.put("type", "Bearer");
                authData.put("passwordExpired", passwordExpired);

                return ResponseEntity.ok(authData);
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Échec d'authentification.");

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Identifiant ou mot de passe incorrect.");
        }
    }

    // 📌 Réinitialisation du mot de passe
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String newPassword = request.get("newPassword");

        if (username == null || newPassword == null) {
            return ResponseEntity.badRequest().body("Nom d'utilisateur et nouveau mot de passe sont requis.");
        }

        User user = userRepository.findByUsername(username);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur introuvable.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setLastPasswordChange(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));

        // 🔓 Débloquer le compte après changement de mot de passe
        user.setFailedAttempts(0);
        user.setLocked(false);

        userRepository.save(user);

        return ResponseEntity.ok("Mot de passe mis à jour avec succès.");
    }
}
