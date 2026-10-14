package br.com.rpx.pactumapi.adapter.out.persistence;

import br.com.rpx.pactumapi.adapter.out.persistence.repository.MetaEconomiaJpaRepository;
import br.com.rpx.pactumapi.domain.model.MetaEconomia;
import br.com.rpx.pactumapi.domain.port.out.BuscarMetaEconomiaPort;
import br.com.rpx.pactumapi.domain.port.out.RemoverMetaEconomiaPort;
import br.com.rpx.pactumapi.domain.port.out.SalvarMetaEconomiaPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MetaEconomiaPersistenceAdapter implements
        SalvarMetaEconomiaPort, BuscarMetaEconomiaPort, RemoverMetaEconomiaPort {

    private final MetaEconomiaJpaRepository repository;

    @Override
    public MetaEconomia salvar(MetaEconomia meta) {
        var entity = MetaEconomiaPersistenceMapper.toEntity(meta);
        if (meta.id() != null) {
            // Preserva created_at (coluna não atualizável) ao atualizar uma meta existente.
            var existente = repository.findById(meta.id()).orElse(null);
            if (existente != null) {
                existente.setValor(entity.getValor());
                return MetaEconomiaPersistenceMapper.toDomain(repository.save(existente));
            }
        }
        return MetaEconomiaPersistenceMapper.toDomain(repository.save(entity));
    }

    @Override
    public Optional<MetaEconomia> buscarPorCompetencia(YearMonth competencia, UUID usuarioId) {
        return repository.findByCompetenciaAndUsuarioId(competencia.atDay(1), usuarioId)
                .map(MetaEconomiaPersistenceMapper::toDomain);
    }

    @Override
    public List<MetaEconomia> listarPorAno(int ano, UUID usuarioId) {
        var inicio = Year.of(ano).atDay(1);
        var fim = Year.of(ano).atMonth(12).atEndOfMonth();
        return repository.findByUsuarioIdAndCompetenciaBetweenOrderByCompetencia(usuarioId, inicio, fim).stream()
                .map(MetaEconomiaPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public void remover(UUID id) {
        repository.deleteById(id);
    }
}
