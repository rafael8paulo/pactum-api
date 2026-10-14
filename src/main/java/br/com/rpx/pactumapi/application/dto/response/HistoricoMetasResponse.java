package br.com.rpx.pactumapi.application.dto.response;

import java.util.List;

public record HistoricoMetasResponse(int ano, List<ResultadoMensalMetaResponse> meses) {}
