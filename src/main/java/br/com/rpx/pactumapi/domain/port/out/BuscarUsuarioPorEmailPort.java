package br.com.rpx.pactumapi.domain.port.out;

import br.com.rpx.pactumapi.domain.model.Usuario;

import java.util.Optional;

public interface BuscarUsuarioPorEmailPort {
    Optional<Usuario> buscarPorEmail(String email);
}
