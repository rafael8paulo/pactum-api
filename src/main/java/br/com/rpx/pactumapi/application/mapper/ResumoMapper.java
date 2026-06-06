package br.com.rpx.pactumapi.application.mapper;

import br.com.rpx.pactumapi.application.dto.response.HistoricoAnualResponse;
import br.com.rpx.pactumapi.application.dto.response.ResumoMensalResponse;
import br.com.rpx.pactumapi.domain.model.ResumoMensal;

import java.math.BigDecimal;
import java.util.List;

public class ResumoMapper {

    private ResumoMapper() {}

    public static ResumoMensalResponse toResponse(ResumoMensal resumo) {
        return new ResumoMensalResponse(resumo.competencia(), resumo.totalReceitas(), resumo.totalDespesas(), resumo.saldo());
    }

    public static HistoricoAnualResponse toHistoricoResponse(int ano, List<ResumoMensal> meses) {
        List<ResumoMensalResponse> respostas = meses.stream().map(ResumoMapper::toResponse).toList();
        BigDecimal totalAnualReceitas = meses.stream().map(ResumoMensal::totalReceitas).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalAnualDespesas = meses.stream().map(ResumoMensal::totalDespesas).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal saldoAnual = meses.stream().map(ResumoMensal::saldo).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new HistoricoAnualResponse(ano, respostas, totalAnualReceitas, totalAnualDespesas, saldoAnual);
    }
}
