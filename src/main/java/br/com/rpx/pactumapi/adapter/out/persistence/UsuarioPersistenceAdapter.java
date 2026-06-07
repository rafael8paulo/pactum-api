package br.com.rpx.pactumapi.adapter.out.persistence;

import br.com.rpx.pactumapi.adapter.out.persistence.repository.UsuarioJpaRepository;
import br.com.rpx.pactumapi.domain.model.Usuario;
import br.com.rpx.pactumapi.domain.port.out.BuscarUsuarioPorEmailPort;
import br.com.rpx.pactumapi.domain.port.out.SalvarUsuarioPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UsuarioPersistenceAdapter implements SalvarUsuarioPort, BuscarUsuarioPorEmailPort {

    private final UsuarioJpaRepository repository;

    @Override
    public Usuario salvar(Usuario usuario) {
        var entity = UsuarioPersistenceMapper.toEntity(usuario);
        return UsuarioPersistenceMapper.toDomain(repository.save(entity));
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return repository.findByEmail(email).map(UsuarioPersistenceMapper::toDomain);
    }
}
