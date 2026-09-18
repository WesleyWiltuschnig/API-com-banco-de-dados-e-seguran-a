package br.senai.agenciaviagens.service;

import br.senai.agenciaviagens.dto.AvaliacaoRequestDTO;
import br.senai.agenciaviagens.dto.DestinoRequestDTO;
import br.senai.agenciaviagens.dto.DestinoResponseDTO;
import br.senai.agenciaviagens.entity.Destino;
import br.senai.agenciaviagens.exception.RecursoNaoEncontradoException;
import br.senai.agenciaviagens.exception.RegraDeNegocioException;
import br.senai.agenciaviagens.repository.DestinoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DestinoService {

    private final DestinoRepository destinoRepository;

    public DestinoService(DestinoRepository destinoRepository) {
        this.destinoRepository = destinoRepository;
    }

    @Transactional(readOnly = true)
    public Page<DestinoResponseDTO> listar(Pageable pageable) {
        return destinoRepository.findAll(pageable).map(DestinoResponseDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<DestinoResponseDTO> listarAtivos(Pageable pageable) {
        return destinoRepository.findByAtivoTrue(pageable).map(DestinoResponseDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<DestinoResponseDTO> buscarPorNome(String nome, Pageable pageable) {
        return destinoRepository.findByNomeContainingIgnoreCase(nome, pageable)
                .map(DestinoResponseDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<DestinoResponseDTO> buscarPorPais(String pais) {
        return destinoRepository.findByPaisIgnoreCase(pais).stream()
                .map(DestinoResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DestinoResponseDTO> buscarPorAvaliacaoMinima(Double nota) {
        return destinoRepository.findByAvaliacaoGreaterThanEqualOrderByAvaliacaoDesc(nota).stream()
                .map(DestinoResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public DestinoResponseDTO buscarPorId(Long id) {
        return DestinoResponseDTO.fromEntity(obterEntidade(id));
    }

    @Transactional
    public DestinoResponseDTO cadastrar(DestinoRequestDTO dto) {
        if (destinoRepository.existsByNomeIgnoreCaseAndLocalizacaoIgnoreCase(
                dto.nome(), dto.localizacao())) {
            throw new RegraDeNegocioException(
                    "Ja existe um destino cadastrado com este nome e localizacao");
        }

        Destino destino = new Destino();
        aplicarDados(destino, dto);
        return DestinoResponseDTO.fromEntity(destinoRepository.save(destino));
    }

    @Transactional
    public DestinoResponseDTO atualizar(Long id, DestinoRequestDTO dto) {
        Destino destino = obterEntidade(id);
        aplicarDados(destino, dto);
        return DestinoResponseDTO.fromEntity(destinoRepository.save(destino));
    }

    @Transactional
    public DestinoResponseDTO avaliar(Long id, AvaliacaoRequestDTO dto) {
        Destino destino = obterEntidade(id);
        if (Boolean.FALSE.equals(destino.getAtivo())) {
            throw new RegraDeNegocioException("Nao e possivel avaliar um destino inativo");
        }
        destino.registrarAvaliacao(dto.nota());
        return DestinoResponseDTO.fromEntity(destinoRepository.save(destino));
    }

    @Transactional
    public DestinoResponseDTO alternarStatus(Long id, boolean ativo) {
        Destino destino = obterEntidade(id);
        destino.setAtivo(ativo);
        return DestinoResponseDTO.fromEntity(destinoRepository.save(destino));
    }

    @Transactional
    public void deletar(Long id) {
        Destino destino = obterEntidade(id);
        destinoRepository.delete(destino);
    }

    private Destino obterEntidade(Long id) {
        return destinoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Destino nao encontrado para o id " + id));
    }

    private void aplicarDados(Destino destino, DestinoRequestDTO dto) {
        destino.setNome(dto.nome());
        destino.setLocalizacao(dto.localizacao());
        destino.setPais(dto.pais());
        destino.setDescricao(dto.descricao());
        destino.setPrecoBase(dto.precoBase());
    }
}
