package br.senai.agenciaviagens.controller;

import br.senai.agenciaviagens.dto.UsuarioResponseDTO;
import br.senai.agenciaviagens.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /** Valida as credenciais enviadas via HTTP Basic e retorna o usuario autenticado. */
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> usuarioLogado(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(usuarioService.buscarPorUsername(userDetails.getUsername()));
    }
}
