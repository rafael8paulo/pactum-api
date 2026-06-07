package br.com.rpx.pactumapi.config.security;

import br.com.rpx.pactumapi.domain.port.out.BuscarUsuarioPorEmailPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UsuarioAutenticadoResolver {

    private final BuscarUsuarioPorEmailPort buscarUsuarioPorEmailPort;

    public UUID getUsuarioId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return buscarUsuarioPorEmailPort.buscarPorEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado: " + email))
                .id();
    }
}
