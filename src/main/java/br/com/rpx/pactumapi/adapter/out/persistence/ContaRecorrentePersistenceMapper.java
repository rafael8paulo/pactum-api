package br.com.rpx.pactumapi.adapter.out.persistence;

import br.com.rpx.pactumapi.adapter.out.persistence.entity.ContaRecorrenteJpaEntity;
import br.com.rpx.pactumapi.domain.model.CategoriaDespesa;
import br.com.rpx.pactumapi.domain.model.ContaRecorrente;
import br.com.rpx.pactumapi.domain.model.StatusContaRecorrente;

import java.time.YearMonth;

public class ContaRecorrentePersistenceMapper {

    private ContaRecorrentePersistenceMapper() {}

    public static ContaRecorrente toDomain(ContaRecorrenteJpaEntity entity) {
        return new ContaRecorrente(
                entity.getId(),
                entity.getDescricao(),
                entity.getValorPadrao(),
                CategoriaDespesa.valueOf(entity.getCategoria()),
                entity.getDiaVencimento(),
                YearMonth.from(entity.getCompetenciaInicio()),
                entity.getCompetenciaFim() != null ? YearMonth.from(entity.getCompetenciaFim()) : null,
                StatusContaRecorrente.valueOf(entity.getStatus()),
                entity.getUsuarioId()
        );
    }

    public static ContaRecorrenteJpaEntity toEntity(ContaRecorrente contaRecorrente) {
        ContaRecorrenteJpaEntity entity = new ContaRecorrenteJpaEntity();
        entity.setId(contaRecorrente.id());
        entity.setDescricao(contaRecorrente.descricao());
        entity.setValorPadrao(contaRecorrente.valorPadrao());
        entity.setCategoria(contaRecorrente.categoria().name());
        entity.setDiaVencimento(contaRecorrente.diaVencimento());
        entity.setCompetenciaInicio(contaRecorrente.competenciaInicio().atDay(1));
        entity.setCompetenciaFim(contaRecorrente.competenciaFim() != null ? contaRecorrente.competenciaFim().atDay(1) : null);
        entity.setStatus(contaRecorrente.status().name());
        entity.setUsuarioId(contaRecorrente.usuarioId());
        return entity;
    }
}
