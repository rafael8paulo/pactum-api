package br.com.rpx.pactumapi.application.mapper;

import br.com.rpx.pactumapi.application.dto.response.HistoricoValorResponse;
import br.com.rpx.pactumapi.application.dto.response.ListaHistoricoValorResponse;
import br.com.rpx.pactumapi.application.dto.response.ListaProximasCobrancasResponse;
import br.com.rpx.pactumapi.application.dto.response.ProximaCobrancaResponse;
import br.com.rpx.pactumapi.application.dto.response.ResumoContasRecorrentesResponse;
import br.com.rpx.pactumapi.application.dto.response.TotalPorFormaPagamentoResponse;
import br.com.rpx.pactumapi.domain.model.HistoricoValorContaRecorrente;
import br.com.rpx.pactumapi.domain.model.ProximaCobranca;
import br.com.rpx.pactumapi.domain.model.ResumoContasRecorrentes;

import java.util.List;

public class ResumoContasRecorrentesMapper {

    private ResumoContasRecorrentesMapper() {}

    public static ResumoContasRecorrentesResponse toResponse(ResumoContasRecorrentes resumo) {
        List<TotalPorFormaPagamentoResponse> porFormaPagamento = resumo.porFormaPagamento().stream()
                .map(total -> new TotalPorFormaPagamentoResponse(
                        total.formaPagamentoId(), total.nomeFormaPagamento(), total.totalMensal()))
                .toList();
        return new ResumoContasRecorrentesResponse(resumo.totalMensalRecorrente(), porFormaPagamento);
    }

    public static ListaProximasCobrancasResponse toListResponse(List<ProximaCobranca> proximasCobrancas) {
        List<ProximaCobrancaResponse> itens = proximasCobrancas.stream()
                .map(proxima -> new ProximaCobrancaResponse(
                        proxima.contaRecorrente().id(),
                        proxima.contaRecorrente().descricao(),
                        proxima.contaRecorrente().valorPadrao(),
                        proxima.contaRecorrente().formaPagamentoId(),
                        proxima.data()))
                .toList();
        return new ListaProximasCobrancasResponse(itens);
    }

    public static ListaHistoricoValorResponse toHistoricoResponse(List<HistoricoValorContaRecorrente> historico) {
        List<HistoricoValorResponse> itens = historico.stream()
                .map(item -> new HistoricoValorResponse(item.valor(), item.vigenteDesde(), item.vigenteAte()))
                .toList();
        return new ListaHistoricoValorResponse(itens);
    }
}
