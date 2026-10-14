package br.com.rpx.pactumapi.domain.model;

import java.time.LocalDate;

public record ProximaCobranca(
        ContaRecorrente contaRecorrente,
        LocalDate data
) {}
