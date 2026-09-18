package br.senai.agenciaviagens.config;

import br.senai.agenciaviagens.entity.Destino;
import br.senai.agenciaviagens.entity.Perfil;
import br.senai.agenciaviagens.entity.Usuario;
import br.senai.agenciaviagens.repository.DestinoRepository;
import br.senai.agenciaviagens.repository.PerfilRepository;
import br.senai.agenciaviagens.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Carga inicial: perfis ROLE_ADMIN / ROLE_USER, usuarios de teste e destinos de exemplo.
 * Executa apenas quando app.seed.enabled=true e a base ainda esta vazia.
 */
@Configuration
public class DataInitializer implements CommandLineRunner {

    private final PerfilRepository perfilRepository;
    private final UsuarioRepository usuarioRepository;
    private final DestinoRepository destinoRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Value("${app.seed.admin-username:admin}")
    private String adminUsername;

    @Value("${app.seed.admin-password:admin123}")
    private String adminPassword;

    @Value("${app.seed.user-username:user}")
    private String userUsername;

    @Value("${app.seed.user-password:user123}")
    private String userPassword;

    public DataInitializer(PerfilRepository perfilRepository,
                           UsuarioRepository usuarioRepository,
                           DestinoRepository destinoRepository,
                           PasswordEncoder passwordEncoder) {
        this.perfilRepository = perfilRepository;
        this.usuarioRepository = usuarioRepository;
        this.destinoRepository = destinoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled) {
            return;
        }

        Perfil admin = obterOuCriarPerfil("ROLE_ADMIN", "Administrador da agencia");
        Perfil user = obterOuCriarPerfil("ROLE_USER", "Usuario consultor de destinos");

        criarUsuarioSeNecessario(adminUsername, "Administrador", adminUsername + "@agencia.com",
                adminPassword, admin, user);
        criarUsuarioSeNecessario(userUsername, "Usuario Padrao", userUsername + "@agencia.com",
                userPassword, user);

        if (destinoRepository.count() == 0) {
            destinoRepository.save(criarDestino("Fernando de Noronha", "Pernambuco", "Brasil",
                    "Arquipelago com praias de agua cristalina e mergulho de alto nivel.",
                    new BigDecimal("4890.00")));
            destinoRepository.save(criarDestino("Bariloche", "Rio Negro", "Argentina",
                    "Destino de montanha com lagos, esqui no inverno e trilhas no verao.",
                    new BigDecimal("3250.00")));
            destinoRepository.save(criarDestino("Lisboa", "Regiao de Lisboa", "Portugal",
                    "Capital historica com bairros tradicionais, miradouros e gastronomia.",
                    new BigDecimal("7100.00")));
        }
    }

    private Perfil obterOuCriarPerfil(String nome, String descricao) {
        return perfilRepository.findByNome(nome)
                .orElseGet(() -> perfilRepository.save(new Perfil(nome, descricao)));
    }

    private void criarUsuarioSeNecessario(String username, String nome, String email,
                                          String senhaEmTextoPlano, Perfil... perfis) {
        if (usuarioRepository.existsByUsername(username)) {
            return;
        }
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setUsername(username);
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(senhaEmTextoPlano));
        usuario.setAtivo(true);
        for (Perfil perfil : perfis) {
            usuario.adicionarPerfil(perfil);
        }
        usuarioRepository.save(usuario);
    }

    private Destino criarDestino(String nome, String localizacao, String pais,
                                 String descricao, BigDecimal preco) {
        Destino destino = new Destino();
        destino.setNome(nome);
        destino.setLocalizacao(localizacao);
        destino.setPais(pais);
        destino.setDescricao(descricao);
        destino.setPrecoBase(preco);
        return destino;
    }
}
