package br.com.rpx.pactumapi.adapter.out.persistence;

import br.com.rpx.pactumapi.adapter.out.persistence.repository.DespesaJpaRepository;
import br.com.rpx.pactumapi.domain.model.CategoriaDespesa;
import br.com.rpx.pactumapi.domain.model.Despesa;
import br.com.rpx.pactumapi.domain.model.StatusDespesa;
import br.com.rpx.pactumapi.domain.port.out.BuscarDespesasPort;
import br.com.rpx.pactumapi.domain.port.out.RemoverDespesaPort;
import br.com.rpx.pactumapi.domain.port.out.SalvarDespesaPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class DespesaPersistenceAdapter implements SalvarDespesaPort, BuscarDespesasPort, RemoverDespesaPort {

    private final DespesaJpaRepository repository;

    @Override
    public Despesa salvar(Despesa despesa) {
        var entity = DespesaPersistenceMapper.toEntity(despesa);
        return DespesaPersistenceMapper.toDomain(repository.save(entity));
    }

    @Override
    public Optional<Despesa> buscarPorId(UUID id) {
        return repository.findById(id).map(DespesaPersistenceMapper::toDomain);
    }

    @Override
    public List<Despesa> buscarPorFiltros(YearMonth competencia, CategoriaDespesa categoria, StatusDespesa status, UUID usuarioId) {
        return repository.findByFiltros(
                competencia.atDay(1),
                categoria != null ? categoria.name() : null,
                status != null ? status.name() : null,
                usuarioId
        ).stream().map(DespesaPersistenceMapper::toDomain).toList();
    }

    @Override
    public void remover(UUID id) {
        repository.deleteById(id);
    }
}
