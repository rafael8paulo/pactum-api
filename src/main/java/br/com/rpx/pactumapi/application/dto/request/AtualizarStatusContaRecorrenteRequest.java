package br.com.rpx.pactumapi.application.dto.request;

import br.com.rpx.pactumapi.domain.model.StatusContaRecorrente;
import jakarta.validation.constraints.NotNull;

public record AtualizarStatusContaRecorrenteRequest(@NotNull StatusContaRecorrente status) {}
