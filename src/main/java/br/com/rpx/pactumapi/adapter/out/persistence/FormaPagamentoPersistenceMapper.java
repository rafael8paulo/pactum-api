package br.com.rpx.pactumapi.adapter.out.persistence;

import br.com.rpx.pactumapi.adapter.out.persistence.entity.FormaPagamentoJpaEntity;
import br.com.rpx.pactumapi.domain.model.FormaPagamento;
import br.com.rpx.pactumapi.domain.model.TipoFormaPagamento;

public class FormaPagamentoPersistenceMapper {

    private FormaPagamentoPersistenceMapper() {}

    public static FormaPagamento toDomain(FormaPagamentoJpaEntity entity) {
        return new FormaPagamento(
                entity.getId(),
                entity.getNome(),
                TipoFormaPagamento.valueOf(entity.getTipo()),
                entity.getDiaFechamentoFatura(),
                entity.getUsuarioId()
        );
    }

    public static FormaPagamentoJpaEntity toEntity(FormaPagamento formaPagamento) {
        FormaPagamentoJpaEntity entity = new FormaPagamentoJpaEntity();
        entity.setId(formaPagamento.id());
        entity.setNome(formaPagamento.nome());
        entity.setTipo(formaPagamento.tipo().name());
        entity.setDiaFechamentoFatura(formaPagamento.diaFechamentoFatura());
        entity.setUsuarioId(formaPagamento.usuarioId());
        return entity;
    }
}
