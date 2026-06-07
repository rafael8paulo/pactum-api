package br.com.rpx.pactumapi.adapter.out.persistence;

import br.com.rpx.pactumapi.adapter.out.persistence.entity.UsuarioJpaEntity;
import br.com.rpx.pactumapi.domain.model.Usuario;

public class UsuarioPersistenceMapper {

    private UsuarioPersistenceMapper() {}

    public static Usuario toDomain(UsuarioJpaEntity entity) {
        return new Usuario(entity.getId(), entity.getNome(), entity.getEmail(), entity.getSenha());
    }

    public static UsuarioJpaEntity toEntity(Usuario usuario) {
        UsuarioJpaEntity entity = new UsuarioJpaEntity();
        entity.setId(usuario.id());
        entity.setNome(usuario.nome());
        entity.setEmail(usuario.email());
        entity.setSenha(usuario.senha());
        return entity;
    }
}
