package br.senai.agenciaviagens.repository;

import br.senai.agenciaviagens.entity.Destino;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DestinoRepository extends JpaRepository<Destino, Long> {

    boolean existsByNomeIgnoreCaseAndLocalizacaoIgnoreCase(String nome, String localizacao);

    Page<Destino> findByAtivoTrue(Pageable pageable);

    Page<Destino> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    List<Destino> findByPaisIgnoreCase(String pais);

    List<Destino> findByAvaliacaoGreaterThanEqualOrderByAvaliacaoDesc(Double nota);
}
