package br.com.rpx.pactumapi.application.dto.request;

import br.com.rpx.pactumapi.domain.model.CategoriaReceita;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.YearMonth;

public record EditarReceitaRequest(
        @NotBlank @Size(max = 100) String descricao,
        @NotNull @Positive BigDecimal valor,
        @NotNull @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM") YearMonth competencia,
        @NotNull CategoriaReceita categoria
) {}
