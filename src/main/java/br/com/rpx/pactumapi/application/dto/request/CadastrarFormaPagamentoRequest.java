package br.com.rpx.pactumapi.application.dto.request;

import br.com.rpx.pactumapi.domain.model.TipoFormaPagamento;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CadastrarFormaPagamentoRequest(
        @NotBlank @Size(max = 100) String nome,
        @NotNull TipoFormaPagamento tipo,
        @Min(1) @Max(31) Integer diaFechamentoFatura
) {}
