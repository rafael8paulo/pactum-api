package br.com.rpx.pactumapi.application.mapper;

import br.com.rpx.pactumapi.application.dto.response.HistoricoMetasResponse;
import br.com.rpx.pactumapi.application.dto.response.MetaEconomiaResponse;
import br.com.rpx.pactumapi.application.dto.response.ProgressoMetaResponse;
import br.com.rpx.pactumapi.application.dto.response.ResultadoMensalMetaResponse;
import br.com.rpx.pactumapi.domain.model.MetaEconomia;
import br.com.rpx.pactumapi.domain.model.ProgressoMeta;
import br.com.rpx.pactumapi.domain.model.ResultadoMensalMeta;

import java.util.List;

public class MetaEconomiaMapper {

    private MetaEconomiaMapper() {}

    public static MetaEconomiaResponse toResponse(MetaEconomia meta) {
        return new MetaEconomiaResponse(meta.competencia(), meta.valor());
    }

    public static ProgressoMetaResponse toResponse(ProgressoMeta p) {
        return new ProgressoMetaResponse(p.competencia(), p.valorMeta(), p.valorEconomizado(), p.valorRestante(),
                p.percentual(), p.diasRestantes(), p.valorDiarioNecessario(), p.projecaoFechamento(),
                p.status(), p.marcosAtingidos());
    }

    public static HistoricoMetasResponse toHistoricoResponse(int ano, List<ResultadoMensalMeta> meses) {
        return new HistoricoMetasResponse(ano, meses.stream()
                .map(r -> new ResultadoMensalMetaResponse(r.competencia(), r.valorMeta(), r.valorEconomizado(),
                        r.percentual(), r.atingida()))
                .toList());
    }
}
