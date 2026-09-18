package br.senai.agenciaviagens.config;

import br.senai.agenciaviagens.security.SecurityErrorHandler;
import br.senai.agenciaviagens.security.UsuarioDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final UsuarioDetailsService usuarioDetailsService;
    private final SecurityErrorHandler securityErrorHandler;

    public SecurityConfig(UsuarioDetailsService usuarioDetailsService,
                          SecurityErrorHandler securityErrorHandler) {
        this.usuarioDetailsService = usuarioDetailsService;
        this.securityErrorHandler = securityErrorHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    /**
     * Amarra explicitamente o UserDetailsService customizado (leitura no PostgreSQL)
     * ao BCryptPasswordEncoder. Com este provider registrado, o Spring Boot nao cria
     * o usuario em memoria com senha gerada no console.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(usuarioDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        // Propaga UsernameNotFoundException em vez de mascarar como BadCredentials;
        // manter false em producao para nao revelar quais usernames existem.
        provider.setHideUserNotFoundExceptions(true);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        // Consulta de destinos: publica
                        .requestMatchers(HttpMethod.GET, "/api/destinos", "/api/destinos/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/public/**", "/actuator/health").permitAll()

                        // Avaliacao: qualquer usuario autenticado (USER ou ADMIN).
                        // Declarada ANTES da regra generica de POST em /api/destinos/**,
                        // pois authorizeHttpRequests avalia pela primeira correspondencia.
                        .requestMatchers(HttpMethod.POST, "/api/destinos/*/avaliacoes")
                        .hasAnyRole("USER", "ADMIN")

                        .requestMatchers("/api/auth/me").authenticated()

                        // Operacoes sensiveis sobre destinos: somente ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/destinos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/destinos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/destinos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/destinos/**").hasRole("ADMIN")

                        // Gestao de usuarios: somente ADMIN
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                // 401 e 403 respondidos com o mesmo ErroResponseDTO do resto da API
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(securityErrorHandler)
                        .accessDeniedHandler(securityErrorHandler))
                .httpBasic(basic -> basic.authenticationEntryPoint(securityErrorHandler));

        return http.build();
    }
}
