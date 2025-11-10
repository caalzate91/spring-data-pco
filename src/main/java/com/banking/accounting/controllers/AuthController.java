package com.banking.accounting.controllers;

import com.banking.accounting.entities.Usuario;
import com.banking.accounting.repositories.UsuarioRepository;
import com.banking.accounting.services.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user authentication")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UsuarioRepository usuarioRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<Usuario> register(@RequestBody Usuario usuario) {
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return ResponseEntity.ok(usuarioRepository.save(usuario));
    }

    @PostMapping("/login")
    @Operation(summary = "Login a user and generate a JWT token")
    public ResponseEntity<?> login(@RequestParam String correo, @RequestParam String password) {
        Usuario usuario = usuarioRepository.findByCorreo(correo);

        if (usuario != null && passwordEncoder.matches(password, usuario.getPassword())) {
            String token = jwtUtil.generateToken(correo);
            return ResponseEntity.ok(token);
        }
        return ResponseEntity.status(401).body("Credenciales inválidas");
    }
}
