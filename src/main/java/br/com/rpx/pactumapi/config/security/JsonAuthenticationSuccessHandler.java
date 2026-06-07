package br.com.rpx.pactumapi.config.security;

import br.com.rpx.pactumapi.application.dto.response.UsuarioResponse;
import br.com.rpx.pactumapi.domain.model.Usuario;
import br.com.rpx.pactumapi.domain.port.out.BuscarUsuarioPorEmailPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JsonAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final BuscarUsuarioPorEmailPort buscarUsuarioPorEmailPort;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        Usuario usuario = buscarUsuarioPorEmailPort.buscarPorEmail(authentication.getName())
                .orElseThrow();
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(),
                new UsuarioResponse(usuario.id(), usuario.nome(), usuario.email()));
    }
}
