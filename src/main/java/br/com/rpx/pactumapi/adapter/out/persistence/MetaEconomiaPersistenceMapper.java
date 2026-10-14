package br.com.rpx.pactumapi.adapter.out.persistence;

import br.com.rpx.pactumapi.adapter.out.persistence.entity.MetaEconomiaJpaEntity;
import br.com.rpx.pactumapi.domain.model.MetaEconomia;

import java.time.YearMonth;

public class MetaEconomiaPersistenceMapper {

    private MetaEconomiaPersistenceMapper() {}

    public static MetaEconomia toDomain(MetaEconomiaJpaEntity entity) {
        return new MetaEconomia(
                entity.getId(),
                YearMonth.from(entity.getCompetencia()),
                entity.getValor(),
                entity.getUsuarioId()
        );
    }

    public static MetaEconomiaJpaEntity toEntity(MetaEconomia meta) {
        MetaEconomiaJpaEntity entity = new MetaEconomiaJpaEntity();
        entity.setId(meta.id());
        entity.setCompetencia(meta.competencia().atDay(1));
        entity.setValor(meta.valor());
        entity.setUsuarioId(meta.usuarioId());
        return entity;
    }
}
