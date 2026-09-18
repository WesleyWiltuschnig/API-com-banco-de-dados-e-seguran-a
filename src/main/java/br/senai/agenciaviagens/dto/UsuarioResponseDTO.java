package br.senai.agenciaviagens.dto;

import br.senai.agenciaviagens.entity.Perfil;
import br.senai.agenciaviagens.entity.Usuario;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public record UsuarioResponseDTO(
        Long id,
        String nome,
        String username,
        String email,
        Boolean ativo,
        LocalDateTime criadoEm,
        Set<String> perfis
) {

    public static UsuarioResponseDTO fromEntity(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getUsername(),
                usuario.getEmail(),
                usuario.getAtivo(),
                usuario.getCriadoEm(),
                usuario.getPerfis().stream().map(Perfil::getNome).collect(Collectors.toSet())
        );
    }
}
