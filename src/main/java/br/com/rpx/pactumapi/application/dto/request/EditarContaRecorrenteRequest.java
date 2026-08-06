package br.com.rpx.pactumapi.application.dto.request;

import br.com.rpx.pactumapi.domain.model.CategoriaDespesa;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.YearMonth;

public record EditarContaRecorrenteRequest(
        @NotBlank @Size(max = 100) String descricao,
        @NotNull @Positive BigDecimal valorPadrao,
        @NotNull CategoriaDespesa categoria,
        @Min(1) @Max(31) Integer diaVencimento,
        @NotNull @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM") YearMonth competenciaInicio,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM") YearMonth competenciaFim
) {}
