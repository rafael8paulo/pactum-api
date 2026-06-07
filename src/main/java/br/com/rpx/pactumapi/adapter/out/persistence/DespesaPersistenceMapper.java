package br.com.rpx.pactumapi.adapter.out.persistence;

import br.com.rpx.pactumapi.adapter.out.persistence.entity.DespesaJpaEntity;
import br.com.rpx.pactumapi.domain.model.CategoriaDespesa;
import br.com.rpx.pactumapi.domain.model.Despesa;
import br.com.rpx.pactumapi.domain.model.StatusDespesa;

import java.time.YearMonth;

public class DespesaPersistenceMapper {

    private DespesaPersistenceMapper() {}

    public static Despesa toDomain(DespesaJpaEntity entity) {
        return new Despesa(
                entity.getId(),
                entity.getDescricao(),
                entity.getValor(),
                StatusDespesa.valueOf(entity.getStatus()),
                YearMonth.from(entity.getCompetencia()),
                CategoriaDespesa.valueOf(entity.getCategoria()),
                entity.getUsuarioId()
        );
    }

    public static DespesaJpaEntity toEntity(Despesa despesa) {
        DespesaJpaEntity entity = new DespesaJpaEntity();
        entity.setId(despesa.id());
        entity.setDescricao(despesa.descricao());
        entity.setValor(despesa.valor());
        entity.setStatus(despesa.status().name());
        entity.setCompetencia(despesa.competencia().atDay(1));
        entity.setCategoria(despesa.categoria().name());
        entity.setUsuarioId(despesa.usuarioId());
        return entity;
    }
}
