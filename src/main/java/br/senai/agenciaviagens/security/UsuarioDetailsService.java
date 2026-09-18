package br.senai.agenciaviagens.security;

import br.senai.agenciaviagens.entity.Usuario;
import br.senai.agenciaviagens.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ponto unico de carregamento de credenciais do Spring Security.
 * Consulta a tabela "usuarios" no PostgreSQL atraves do UsuarioRepository e
 * devolve um UserDetails com as authorities derivadas da tabela "perfis".
 *
 * O contrato de UserDetailsService e sincrono por definicao e e invocado dentro
 * do DaoAuthenticationProvider, tambem sincrono. Por isso a consulta e feita em
 * uma unica query com LEFT JOIN FETCH, dentro de uma transacao somente leitura,
 * evitando N+1 e LazyInitializationException sem recorrer a assincronia.
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioDetailsService.class);

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null || username.isBlank()) {
            throw new UsernameNotFoundException("Username nao informado");
        }

        Usuario usuario = usuarioRepository.findByUsernameComPerfis(username.trim())
                .orElseThrow(() -> {
                    log.debug("Falha de autenticacao: usuario inexistente [{}]", username);
                    return new UsernameNotFoundException("Usuario nao encontrado: " + username);
                });

        if (usuario.getPerfis().isEmpty()) {
            log.warn("Usuario [{}] autenticado sem nenhum perfil vinculado; "
                    + "todo acesso protegido sera negado com 403", username);
        }

        return new UsuarioAutenticado(usuario);
    }
}
