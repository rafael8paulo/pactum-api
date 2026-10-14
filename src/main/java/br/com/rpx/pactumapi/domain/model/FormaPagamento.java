package br.com.rpx.pactumapi.domain.model;

import java.util.UUID;

public record FormaPagamento(
        UUID id,
        String nome,
        TipoFormaPagamento tipo,
        Integer diaFechamentoFatura,
        UUID usuarioId
) {}
