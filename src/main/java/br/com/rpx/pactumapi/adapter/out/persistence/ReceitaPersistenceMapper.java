package br.com.rpx.pactumapi.adapter.out.persistence;

import br.com.rpx.pactumapi.adapter.out.persistence.entity.ReceitaJpaEntity;
import br.com.rpx.pactumapi.domain.model.CategoriaReceita;
import br.com.rpx.pactumapi.domain.model.Receita;

import java.time.YearMonth;

public class ReceitaPersistenceMapper {

    private ReceitaPersistenceMapper() {}

    public static Receita toDomain(ReceitaJpaEntity entity) {
        return new Receita(
                entity.getId(),
                entity.getDescricao(),
                entity.getValor(),
                YearMonth.from(entity.getCompetencia()),
                CategoriaReceita.valueOf(entity.getCategoria()),
                entity.getUsuarioId()
        );
    }

    public static ReceitaJpaEntity toEntity(Receita receita) {
        ReceitaJpaEntity entity = new ReceitaJpaEntity();
        entity.setId(receita.id());
        entity.setDescricao(receita.descricao());
        entity.setValor(receita.valor());
        entity.setCompetencia(receita.competencia().atDay(1));
        entity.setCategoria(receita.categoria().name());
        entity.setUsuarioId(receita.usuarioId());
        return entity;
    }
}
