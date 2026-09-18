package br.senai.agenciaviagens.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ErroResponseDTO(
        LocalDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        String caminho,
        Map<String, String> campos
) {

    public static ErroResponseDTO of(int status, String erro, String mensagem, String caminho) {
        return new ErroResponseDTO(LocalDateTime.now(), status, erro, mensagem, caminho, null);
    }

    public static ErroResponseDTO of(int status, String erro, String mensagem, String caminho,
                                     Map<String, String> campos) {
        return new ErroResponseDTO(LocalDateTime.now(), status, erro, mensagem, caminho, campos);
    }
}
