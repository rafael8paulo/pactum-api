package br.com.rpx.pactumapi.application.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record DefinirMetaRequest(
        @NotNull @Positive @DecimalMax("99999999.99") BigDecimal valor
) {}
