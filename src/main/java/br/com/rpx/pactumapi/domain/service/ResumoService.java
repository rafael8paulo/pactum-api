package br.com.rpx.pactumapi.domain.service;

import br.com.rpx.pactumapi.config.UseCase;
import br.com.rpx.pactumapi.domain.model.ResumoMensal;
import br.com.rpx.pactumapi.domain.port.in.ConsultarHistoricoAnualUseCase;
import br.com.rpx.pactumapi.domain.port.in.ConsultarResumoMensalUseCase;
import br.com.rpx.pactumapi.domain.port.out.BuscarDespesasPort;
import br.com.rpx.pactumapi.domain.port.out.BuscarReceitasPort;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.IntStream;

@UseCase
@RequiredArgsConstructor
public class ResumoService implements ConsultarResumoMensalUseCase, ConsultarHistoricoAnualUseCase {

    private final BuscarReceitasPort buscarReceitasPort;
    private final BuscarDespesasPort buscarDespesasPort;

    @Override
    public ResumoMensal consultar(YearMonth competencia) {
        BigDecimal totalReceitas = buscarReceitasPort.buscarPorFiltros(competencia, null).stream()
                .map(r -> r.valor())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDespesas = buscarDespesasPort.buscarPorFiltros(competencia, null, null).stream()
                .map(d -> d.valor())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ResumoMensal(competencia, totalReceitas, totalDespesas, totalReceitas.subtract(totalDespesas));
    }

    @Override
    public List<ResumoMensal> consultar(int ano) {
        return IntStream.rangeClosed(1, 12)
                .mapToObj(mes -> consultar(YearMonth.of(ano, mes)))
                .toList();
    }
}
