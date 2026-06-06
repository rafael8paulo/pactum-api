package br.com.rpx.pactumapi.application.dto.request;

import br.com.rpx.pactumapi.domain.model.StatusDespesa;
import jakarta.validation.constraints.NotNull;

public record AtualizarStatusDespesaRequest(@NotNull StatusDespesa status) {}
