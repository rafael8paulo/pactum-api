package br.com.rpx.pactumapi.adapter.out.persistence;

import br.com.rpx.pactumapi.adapter.out.persistence.entity.PatrimonioJpaEntity;
import br.com.rpx.pactumapi.domain.model.Patrimonio;

import java.time.YearMonth;

public class PatrimonioPersistenceMapper {

    private PatrimonioPersistenceMapper() {}

    public static Patrimonio toDomain(PatrimonioJpaEntity entity) {
        return new Patrimonio(
                entity.getId(),
                entity.getDescricao(),
                entity.getValor(),
                YearMonth.from(entity.getCompetencia())
        );
    }

    public static PatrimonioJpaEntity toEntity(Patrimonio patrimonio) {
        PatrimonioJpaEntity entity = new PatrimonioJpaEntity();
        entity.setId(patrimonio.id());
        entity.setDescricao(patrimonio.descricao());
        entity.setValor(patrimonio.valor());
        entity.setCompetencia(patrimonio.competencia().atDay(1));
        return entity;
    }
}
