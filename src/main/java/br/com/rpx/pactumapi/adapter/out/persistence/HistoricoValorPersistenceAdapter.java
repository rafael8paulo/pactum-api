package br.com.rpx.pactumapi.adapter.out.persistence;

import br.com.rpx.pactumapi.adapter.out.persistence.repository.HistoricoValorContaRecorrenteJpaRepository;
import br.com.rpx.pactumapi.domain.model.HistoricoValorContaRecorrente;
import br.com.rpx.pactumapi.domain.port.out.BuscarHistoricoValorPort;
import br.com.rpx.pactumapi.domain.port.out.SalvarHistoricoValorPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class HistoricoValorPersistenceAdapter implements SalvarHistoricoValorPort, BuscarHistoricoValorPort {

    private final HistoricoValorContaRecorrenteJpaRepository repository;

    @Override
    public HistoricoValorContaRecorrente salvar(HistoricoValorContaRecorrente historicoValor) {
        var entity = HistoricoValorPersistenceMapper.toEntity(historicoValor);
        return HistoricoValorPersistenceMapper.toDomain(repository.save(entity));
    }

    @Override
    public Optional<HistoricoValorContaRecorrente> buscarVigente(UUID contaRecorrenteId) {
        return repository.findByContaRecorrenteIdAndVigenteAteIsNull(contaRecorrenteId)
                .map(HistoricoValorPersistenceMapper::toDomain);
    }

    @Override
    public List<HistoricoValorContaRecorrente> buscarPorContaRecorrenteOrdenado(UUID contaRecorrenteId) {
        return repository.findByContaRecorrenteIdOrderByVigenteDesdeDesc(contaRecorrenteId).stream()
                .map(HistoricoValorPersistenceMapper::toDomain)
                .toList();
    }
}
