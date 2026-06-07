package br.com.rpx.pactumapi.domain.port.in;

import br.com.rpx.pactumapi.domain.model.Usuario;

public interface CadastrarUsuarioUseCase {
    Usuario cadastrar(Usuario usuario);
}
