package br.com.rpx.pactumapi.application.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record ListaPatrimonioResponse(List<PatrimonioResponse> itens, BigDecimal totalPatrimonio) {}
