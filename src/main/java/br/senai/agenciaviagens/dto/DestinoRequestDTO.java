package br.senai.agenciaviagens.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record DestinoRequestDTO(

        @NotBlank(message = "O nome do destino e obrigatorio")
        @Size(max = 120, message = "O nome deve ter no maximo 120 caracteres")
        String nome,

        @NotBlank(message = "A localizacao e obrigatoria")
        @Size(max = 150, message = "A localizacao deve ter no maximo 150 caracteres")
        String localizacao,

        @NotBlank(message = "O pais e obrigatorio")
        @Size(max = 80, message = "O pais deve ter no maximo 80 caracteres")
        String pais,

        @NotBlank(message = "A descricao e obrigatoria")
        @Size(max = 1000, message = "A descricao deve ter no maximo 1000 caracteres")
        String descricao,

        @DecimalMin(value = "0.0", message = "O preco base nao pode ser negativo")
        BigDecimal precoBase
) {
}
