package com.bank.app.lettrage.controller;

import com.bank.app.lettrage.entity.User;
import com.bank.app.lettrage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;


import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*")
public class AdminController {
    private final UserRepository repo;
    private final PasswordEncoder encoder;

    // 1️⃣ LISTER TOUS LES UTILISATEURS
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> list() {
        return ResponseEntity.ok(repo.findAll());
    }

    // ➕ CRÉER UN NOUVEL UTILISATEUR
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> create(@RequestBody User in) {
        // encode le mot de passe
        in.setPassword(encoder.encode(in.getPassword()));
        // initialise les champs de suivi
        in.setLastPasswordChange(new Date());
        in.setFailedAttempts(0);
        in.setLocked(false);
        User saved = repo.save(in);
        // 201 Created
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // 2️⃣ RÉCUPÉRER UN UTILISATEUR PAR ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")

    public ResponseEntity<User> get(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3️⃣ METTRE À JOUR UN UTILISATEUR
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")

    public ResponseEntity<User> update(@PathVariable Long id, @RequestBody User in) {
        return repo.findById(id)
                .map(u -> {
                    u.setUsername(in.getUsername());
                    u.setRole(in.getRole());
                    if (in.getPassword() != null && !in.getPassword().isBlank()) {
                        u.setPassword(encoder.encode(in.getPassword()));
                        u.setLastPasswordChange(new Date());
                    }
                    return ResponseEntity.ok(repo.save(u));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 4️⃣ SUPPRIMER UN UTILISATEUR
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")

    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
