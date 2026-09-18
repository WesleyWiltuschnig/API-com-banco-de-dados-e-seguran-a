package br.senai.agenciaviagens.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UsuarioRequestDTO(

        @NotBlank(message = "O nome e obrigatorio")
        @Size(max = 120)
        String nome,

        @NotBlank(message = "O username e obrigatorio")
        @Size(min = 3, max = 60, message = "O username deve ter entre 3 e 60 caracteres")
        String username,

        @NotBlank(message = "O e-mail e obrigatorio")
        @Email(message = "E-mail invalido")
        String email,

        @NotBlank(message = "A senha e obrigatoria")
        @Size(min = 6, max = 60, message = "A senha deve ter no minimo 6 caracteres")
        String senha,

        @NotEmpty(message = "Informe ao menos um perfil: ADMIN ou USER")
        Set<String> perfis
) {
}
