package br.com.rpx.pactumapi.application.mapper;

import br.com.rpx.pactumapi.application.dto.request.CadastroUsuarioRequest;
import br.com.rpx.pactumapi.application.dto.response.UsuarioResponse;
import br.com.rpx.pactumapi.domain.model.Usuario;

public class UsuarioMapper {

    private UsuarioMapper() {}

    public static Usuario toDomain(CadastroUsuarioRequest request) {
        return new Usuario(null, request.nome(), request.email(), request.senha());
    }

    public static UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(usuario.id(), usuario.nome(), usuario.email());
    }
}
