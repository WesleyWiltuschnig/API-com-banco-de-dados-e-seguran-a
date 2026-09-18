-- =====================================================================
-- Carga inicial da API Agencia de Viagens
-- Executado automaticamente pelo Spring Boot no start da aplicacao.
--
-- Pre-requisitos no application.properties:
--   spring.sql.init.mode=always
--   spring.jpa.defer-datasource-initialization=true   <- obrigatorio
--   app.seed.enabled=false                            <- evita carga duplicada
--
-- Por que defer-datasource-initialization: por padrao o data.sql roda ANTES
-- do Hibernate criar as tabelas (ddl-auto=update), causando "relation does
-- not exist". Com a propriedade em true, a ordem passa a ser
-- Hibernate cria o schema -> data.sql insere os dados.
--
-- O script e idempotente: pode ser reexecutado a cada start sem duplicar
-- registros. As colunas de id sao omitidas de proposito, para que a identity
-- do PostgreSQL continue coerente.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. Perfis de acesso (authorities do Spring Security)
-- ---------------------------------------------------------------------
INSERT INTO perfis (nome, descricao)
SELECT 'ROLE_ADMIN', 'Administrador da agencia'
WHERE NOT EXISTS (SELECT 1 FROM perfis WHERE nome = 'ROLE_ADMIN');

INSERT INTO perfis (nome, descricao)
SELECT 'ROLE_USER', 'Usuario consultor de destinos'
WHERE NOT EXISTS (SELECT 1 FROM perfis WHERE nome = 'ROLE_USER');

-- ---------------------------------------------------------------------
-- 2. Usuarios de teste
--    Senhas em hash BCrypt, custo 10, compativeis com BCryptPasswordEncoder(10)
--      admin / admin123
--      user  / user123
-- ---------------------------------------------------------------------
INSERT INTO usuarios (nome, username, email, senha, ativo, criado_em)
SELECT 'Administrador', 'admin', 'admin@agencia.com',
       '$2a$10$mCnIXH0Xt0tD.dOE2dJCt.GG/YKQ772u18cv6vETVSHsifGiGQIeG', true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE username = 'admin');

INSERT INTO usuarios (nome, username, email, senha, ativo, criado_em)
SELECT 'Usuario Padrao', 'user', 'user@agencia.com',
       '$2a$10$6SNRSq5cu.SIz85jEKkWFeldHUI/V/v5WUCvH2tv7F9PTxxzckYN6', true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE username = 'user');

-- ---------------------------------------------------------------------
-- 3. Vinculo usuario x perfil (tabela de juncao do @ManyToMany)
--    admin -> ROLE_ADMIN + ROLE_USER
--    user  -> ROLE_USER
-- ---------------------------------------------------------------------
INSERT INTO usuario_perfis (usuario_id, perfil_id)
SELECT u.id, p.id
FROM usuarios u
JOIN perfis p ON p.nome IN ('ROLE_ADMIN', 'ROLE_USER')
WHERE u.username = 'admin'
  AND NOT EXISTS (
        SELECT 1 FROM usuario_perfis up
        WHERE up.usuario_id = u.id AND up.perfil_id = p.id);

INSERT INTO usuario_perfis (usuario_id, perfil_id)
SELECT u.id, p.id
FROM usuarios u
JOIN perfis p ON p.nome = 'ROLE_USER'
WHERE u.username = 'user'
  AND NOT EXISTS (
        SELECT 1 FROM usuario_perfis up
        WHERE up.usuario_id = u.id AND up.perfil_id = p.id);

-- ---------------------------------------------------------------------
-- 4. Destinos de exemplo (garante que a API nao suba com base vazia)
-- ---------------------------------------------------------------------
INSERT INTO destinos (nome, localizacao, pais, descricao, preco_base,
                      avaliacao, total_avaliacoes, ativo, criado_em, atualizado_em)
SELECT 'Fernando de Noronha', 'Pernambuco', 'Brasil',
       'Arquipelago com praias de agua cristalina e mergulho de alto nivel.',
       4890.00, 0.0, 0, true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM destinos WHERE nome = 'Fernando de Noronha');

INSERT INTO destinos (nome, localizacao, pais, descricao, preco_base,
                      avaliacao, total_avaliacoes, ativo, criado_em, atualizado_em)
SELECT 'Bariloche', 'Rio Negro', 'Argentina',
       'Destino de montanha com lagos, esqui no inverno e trilhas no verao.',
       3250.00, 0.0, 0, true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM destinos WHERE nome = 'Bariloche');

INSERT INTO destinos (nome, localizacao, pais, descricao, preco_base,
                      avaliacao, total_avaliacoes, ativo, criado_em, atualizado_em)
SELECT 'Lisboa', 'Regiao de Lisboa', 'Portugal',
       'Capital historica com bairros tradicionais, miradouros e gastronomia.',
       7100.00, 0.0, 0, true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM destinos WHERE nome = 'Lisboa');
