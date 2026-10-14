package br.com.rpx.pactumapi.adapter.out.persistence;

import br.com.rpx.pactumapi.adapter.out.persistence.entity.HistoricoValorContaRecorrenteJpaEntity;
import br.com.rpx.pactumapi.domain.model.HistoricoValorContaRecorrente;

public class HistoricoValorPersistenceMapper {

    private HistoricoValorPersistenceMapper() {}

    public static HistoricoValorContaRecorrente toDomain(HistoricoValorContaRecorrenteJpaEntity entity) {
        return new HistoricoValorContaRecorrente(
                entity.getId(),
                entity.getContaRecorrenteId(),
                entity.getValor(),
                entity.getVigenteDesde(),
                entity.getVigenteAte()
        );
    }

    public static HistoricoValorContaRecorrenteJpaEntity toEntity(HistoricoValorContaRecorrente historicoValor) {
        HistoricoValorContaRecorrenteJpaEntity entity = new HistoricoValorContaRecorrenteJpaEntity();
        entity.setId(historicoValor.id());
        entity.setContaRecorrenteId(historicoValor.contaRecorrenteId());
        entity.setValor(historicoValor.valor());
        entity.setVigenteDesde(historicoValor.vigenteDesde());
        entity.setVigenteAte(historicoValor.vigenteAte());
        return entity;
    }
}
