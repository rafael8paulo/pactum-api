package br.com.rpx.pactumapi.application.dto.response;

import br.com.rpx.pactumapi.domain.model.TipoFormaPagamento;

import java.util.UUID;

public record FormaPagamentoResponse(
        UUID id,
        String nome,
        TipoFormaPagamento tipo,
        Integer diaFechamentoFatura
) {}
