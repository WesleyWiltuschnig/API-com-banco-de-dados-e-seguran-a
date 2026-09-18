package br.senai.agenciaviagens.security;

import br.senai.agenciaviagens.entity.Perfil;
import br.senai.agenciaviagens.entity.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Adapta a entidade Usuario ao contrato UserDetails.
 * As authorities sao os proprios nomes persistidos na tabela "perfis"
 * (ROLE_ADMIN, ROLE_USER), formato exigido por hasRole(...) e hasAnyRole(...).
 */
public class UsuarioAutenticado implements UserDetails {

    private final Usuario usuario;
    private final List<GrantedAuthority> authorities;

    public UsuarioAutenticado(Usuario usuario) {
        this.usuario = usuario;
        this.authorities = usuario.getPerfis().stream()
                .map(Perfil::getNome)
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Long getId() {
        return usuario.getId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return usuario.getSenha();
    }

    @Override
    public String getUsername() {
        return usuario.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(usuario.getAtivo());
    }
}
