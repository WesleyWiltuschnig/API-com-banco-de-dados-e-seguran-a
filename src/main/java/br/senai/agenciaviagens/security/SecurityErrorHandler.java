package br.senai.agenciaviagens.security;

import br.senai.agenciaviagens.dto.ErroResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Falhas de autenticacao e autorizacao ocorrem na cadeia de filtros, antes do
 * DispatcherServlet, e por isso nunca chegam ao @RestControllerAdvice.
 * Esta classe escreve o mesmo ErroResponseDTO usado no restante da API,
 * padronizando o corpo das respostas 401 e 403.
 */
@Component
public class SecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public SecurityErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        escrever(request, response, 401, "Unauthorized",
                "Credenciais ausentes ou invalidas");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        escrever(request, response, 403, "Forbidden",
                "Acesso negado: perfil sem permissao para este recurso");
    }

    private void escrever(HttpServletRequest request, HttpServletResponse response,
                          int status, String erro, String mensagem) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(),
                ErroResponseDTO.of(status, erro, mensagem, request.getRequestURI()));
    }
}
