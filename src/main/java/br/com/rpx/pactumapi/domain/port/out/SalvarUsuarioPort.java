package br.com.rpx.pactumapi.domain.port.out;

import br.com.rpx.pactumapi.domain.model.Usuario;

public interface SalvarUsuarioPort {
    Usuario salvar(Usuario usuario);
}
