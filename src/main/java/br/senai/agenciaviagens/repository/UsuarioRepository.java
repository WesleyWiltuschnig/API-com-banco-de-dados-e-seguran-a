package br.senai.agenciaviagens.repository;

import br.senai.agenciaviagens.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Carrega o usuario e seus perfis em uma unica consulta.
     * LEFT JOIN FETCH: usuario sem perfil vinculado continua sendo encontrado
     * (autentica com zero authorities e e barrado na autorizacao, com 403),
     * em vez de desaparecer da consulta e gerar um 401 enganoso.
     */
    @Query("select u from Usuario u left join fetch u.perfis where u.username = :username")
    Optional<Usuario> findByUsernameComPerfis(@Param("username") String username);

    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
