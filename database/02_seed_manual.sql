-- Carga manual alternativa ao DataInitializer (app.seed.enabled=false).
-- Executar DEPOIS da primeira inicializacao da aplicacao, pois o Hibernate
-- (spring.jpa.hibernate.ddl-auto=update) cria as tabelas automaticamente.

-- Perfis de acesso
INSERT INTO perfis (nome, descricao) VALUES
    ('ROLE_ADMIN', 'Administrador da agencia'),
    ('ROLE_USER',  'Usuario consultor de destinos')
ON CONFLICT (nome) DO NOTHING;

-- Usuarios de teste (senhas em hash BCrypt, custo 10)
-- admin / admin123
-- user  / user123
INSERT INTO usuarios (nome, username, email, senha, ativo, criado_em) VALUES
    ('Administrador', 'admin', 'admin@agencia.com',
     '$2a$10$mCnIXH0Xt0tD.dOE2dJCt.GG/YKQ772u18cv6vETVSHsifGiGQIeG', true, NOW()),
    ('Usuario Padrao', 'user', 'user@agencia.com',
     '$2a$10$6SNRSq5cu.SIz85jEKkWFeldHUI/V/v5WUCvH2tv7F9PTxxzckYN6', true, NOW())
ON CONFLICT (username) DO NOTHING;

-- Vinculo usuario x perfil
INSERT INTO usuario_perfis (usuario_id, perfil_id)
SELECT u.id, p.id FROM usuarios u, perfis p
WHERE u.username = 'admin' AND p.nome IN ('ROLE_ADMIN', 'ROLE_USER')
ON CONFLICT DO NOTHING;

INSERT INTO usuario_perfis (usuario_id, perfil_id)
SELECT u.id, p.id FROM usuarios u, perfis p
WHERE u.username = 'user' AND p.nome = 'ROLE_USER'
ON CONFLICT DO NOTHING;

-- Destinos de exemplo
INSERT INTO destinos (nome, localizacao, pais, descricao, preco_base, avaliacao,
                      total_avaliacoes, ativo, criado_em, atualizado_em) VALUES
    ('Fernando de Noronha', 'Pernambuco', 'Brasil',
     'Arquipelago com praias de agua cristalina e mergulho de alto nivel.',
     4890.00, 0.0, 0, true, NOW(), NOW()),
    ('Bariloche', 'Rio Negro', 'Argentina',
     'Destino de montanha com lagos, esqui no inverno e trilhas no verao.',
     3250.00, 0.0, 0, true, NOW(), NOW()),
    ('Lisboa', 'Regiao de Lisboa', 'Portugal',
     'Capital historica com bairros tradicionais, miradouros e gastronomia.',
     7100.00, 0.0, 0, true, NOW(), NOW());
