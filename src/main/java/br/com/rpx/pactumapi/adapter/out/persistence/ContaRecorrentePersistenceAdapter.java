package br.com.rpx.pactumapi.adapter.out.persistence;

import br.com.rpx.pactumapi.adapter.out.persistence.repository.ContaRecorrenteJpaRepository;
import br.com.rpx.pactumapi.domain.model.ContaRecorrente;
import br.com.rpx.pactumapi.domain.model.StatusContaRecorrente;
import br.com.rpx.pactumapi.domain.port.out.BuscarContasRecorrentesPort;
import br.com.rpx.pactumapi.domain.port.out.RemoverContaRecorrentePort;
import br.com.rpx.pactumapi.domain.port.out.SalvarContaRecorrentePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ContaRecorrentePersistenceAdapter implements
        SalvarContaRecorrentePort, BuscarContasRecorrentesPort, RemoverContaRecorrentePort {

    private final ContaRecorrenteJpaRepository repository;

    @Override
    public ContaRecorrente salvar(ContaRecorrente contaRecorrente) {
        var entity = ContaRecorrentePersistenceMapper.toEntity(contaRecorrente);
        return ContaRecorrentePersistenceMapper.toDomain(repository.save(entity));
    }

    @Override
    public Optional<ContaRecorrente> buscarPorId(UUID id) {
        return repository.findById(id).map(ContaRecorrentePersistenceMapper::toDomain);
    }

    @Override
    public List<ContaRecorrente> buscarPorFiltros(StatusContaRecorrente status, UUID usuarioId) {
        return repository.findByFiltros(status != null ? status.name() : null, usuarioId).stream()
                .map(ContaRecorrentePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public void remover(UUID id) {
        repository.deleteById(id);
    }
}
