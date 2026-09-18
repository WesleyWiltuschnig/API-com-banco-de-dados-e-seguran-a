package br.senai.agenciaviagens.service;

import br.senai.agenciaviagens.dto.UsuarioRequestDTO;
import br.senai.agenciaviagens.dto.UsuarioResponseDTO;
import br.senai.agenciaviagens.entity.Perfil;
import br.senai.agenciaviagens.entity.Usuario;
import br.senai.agenciaviagens.exception.RecursoNaoEncontradoException;
import br.senai.agenciaviagens.exception.RegraDeNegocioException;
import br.senai.agenciaviagens.repository.PerfilRepository;
import br.senai.agenciaviagens.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PerfilRepository perfilRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.perfilRepository = perfilRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listar() {
        return usuarioRepository.findAll().stream()
                .map(UsuarioResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(Long id) {
        return UsuarioResponseDTO.fromEntity(obterEntidade(id));
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorUsername(String username) {
        Usuario usuario = usuarioRepository.findByUsernameComPerfis(username)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Usuario nao encontrado: " + username));
        return UsuarioResponseDTO.fromEntity(usuario);
    }

    @Transactional
    public UsuarioResponseDTO cadastrar(UsuarioRequestDTO dto) {
        if (usuarioRepository.existsByUsername(dto.username())) {
            throw new RegraDeNegocioException("Username ja cadastrado: " + dto.username());
        }
        if (usuarioRepository.existsByEmail(dto.email())) {
            throw new RegraDeNegocioException("E-mail ja cadastrado: " + dto.email());
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dto.nome());
        usuario.setUsername(dto.username());
        usuario.setEmail(dto.email());
        usuario.setSenha(passwordEncoder.encode(dto.senha()));
        usuario.setAtivo(true);
        usuario.setPerfis(resolverPerfis(dto.perfis()));

        return UsuarioResponseDTO.fromEntity(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponseDTO alternarStatus(Long id, boolean ativo) {
        Usuario usuario = obterEntidade(id);
        usuario.setAtivo(ativo);
        return UsuarioResponseDTO.fromEntity(usuarioRepository.save(usuario));
    }

    @Transactional
    public void deletar(Long id) {
        usuarioRepository.delete(obterEntidade(id));
    }

    private Usuario obterEntidade(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Usuario nao encontrado para o id " + id));
    }

    /** Aceita "ADMIN"/"USER" ou "ROLE_ADMIN"/"ROLE_USER". */
    private Set<Perfil> resolverPerfis(Set<String> nomes) {
        Set<Perfil> perfis = new HashSet<>();
        for (String nome : nomes) {
            String normalizado = nome.toUpperCase().startsWith("ROLE_")
                    ? nome.toUpperCase()
                    : "ROLE_" + nome.toUpperCase();
            Perfil perfil = perfilRepository.findByNome(normalizado)
                    .orElseThrow(() -> new RegraDeNegocioException(
                            "Perfil inexistente: " + nome));
            perfis.add(perfil);
        }
        return perfis;
    }
}
