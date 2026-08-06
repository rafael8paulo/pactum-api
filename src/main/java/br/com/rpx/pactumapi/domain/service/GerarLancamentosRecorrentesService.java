package br.com.rpx.pactumapi.domain.service;

import br.com.rpx.pactumapi.config.UseCase;
import br.com.rpx.pactumapi.domain.model.Despesa;
import br.com.rpx.pactumapi.domain.model.StatusContaRecorrente;
import br.com.rpx.pactumapi.domain.port.in.GerarLancamentosRecorrentesUseCase;
import br.com.rpx.pactumapi.domain.port.out.BuscarContasRecorrentesPort;
import br.com.rpx.pactumapi.domain.port.out.BuscarDespesasPort;
import br.com.rpx.pactumapi.domain.port.out.SalvarDespesaPort;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@UseCase
public class GerarLancamentosRecorrentesService implements GerarLancamentosRecorrentesUseCase {

    private final BuscarContasRecorrentesPort buscarContasRecorrentesPort;
    private final BuscarDespesasPort buscarDespesasPort;
    private final SalvarDespesaPort salvarDespesaPort;

    public GerarLancamentosRecorrentesService(BuscarContasRecorrentesPort buscarContasRecorrentesPort,
                                              BuscarDespesasPort buscarDespesasPort,
                                              SalvarDespesaPort salvarDespesaPort) {
        this.buscarContasRecorrentesPort = buscarContasRecorrentesPort;
        this.buscarDespesasPort = buscarDespesasPort;
        this.salvarDespesaPort = salvarDespesaPort;
    }

    @Override
    public List<Despesa> gerar(YearMonth competencia, UUID usuarioId) {
        return buscarContasRecorrentesPort.buscarPorFiltros(StatusContaRecorrente.ATIVA, usuarioId).stream()
                .filter(conta -> conta.cobreCompetencia(competencia))
                .map(conta -> GeradorDespesaRecorrente.gerarSeNecessario(conta, competencia, buscarDespesasPort, salvarDespesaPort))
                .flatMap(Optional::stream)
                .toList();
    }
}
