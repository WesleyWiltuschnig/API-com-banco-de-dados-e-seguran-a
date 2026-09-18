package br.senai.agenciaviagens.dto;

import br.senai.agenciaviagens.entity.Destino;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DestinoResponseDTO(
        Long id,
        String nome,
        String localizacao,
        String pais,
        String descricao,
        BigDecimal precoBase,
        Double avaliacao,
        Integer totalAvaliacoes,
        Boolean ativo,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {

    public static DestinoResponseDTO fromEntity(Destino destino) {
        return new DestinoResponseDTO(
                destino.getId(),
                destino.getNome(),
                destino.getLocalizacao(),
                destino.getPais(),
                destino.getDescricao(),
                destino.getPrecoBase(),
                destino.getAvaliacao(),
                destino.getTotalAvaliacoes(),
                destino.getAtivo(),
                destino.getCriadoEm(),
                destino.getAtualizadoEm()
        );
    }
}
