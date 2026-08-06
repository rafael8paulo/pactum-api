package br.com.rpx.pactumapi.domain.service;

import br.com.rpx.pactumapi.config.UseCase;
import br.com.rpx.pactumapi.domain.exception.ContaRecorrenteNaoEncontradaException;
import br.com.rpx.pactumapi.domain.model.ContaRecorrente;
import br.com.rpx.pactumapi.domain.model.Despesa;
import br.com.rpx.pactumapi.domain.model.StatusContaRecorrente;
import br.com.rpx.pactumapi.domain.port.in.GerarLoteLancamentosRecorrentesUseCase;
import br.com.rpx.pactumapi.domain.port.out.BuscarContasRecorrentesPort;
import br.com.rpx.pactumapi.domain.port.out.BuscarDespesasPort;
import br.com.rpx.pactumapi.domain.port.out.SalvarDespesaPort;

import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@UseCase
public class GerarLoteLancamentosRecorrentesService implements GerarLoteLancamentosRecorrentesUseCase {

    private final BuscarContasRecorrentesPort buscarContasRecorrentesPort;
    private final BuscarDespesasPort buscarDespesasPort;
    private final SalvarDespesaPort salvarDespesaPort;

    public GerarLoteLancamentosRecorrentesService(BuscarContasRecorrentesPort buscarContasRecorrentesPort,
                                                   BuscarDespesasPort buscarDespesasPort,
                                                   SalvarDespesaPort salvarDespesaPort) {
        this.buscarContasRecorrentesPort = buscarContasRecorrentesPort;
        this.buscarDespesasPort = buscarDespesasPort;
        this.salvarDespesaPort = salvarDespesaPort;
    }

    @Override
    public List<Despesa> gerarTodos(UUID contaRecorrenteId, UUID usuarioId) {
        ContaRecorrente conta = buscarPorIdEValidarDono(contaRecorrenteId, usuarioId);
        if (conta.status() != StatusContaRecorrente.ATIVA) {
            return List.of();
        }
        YearMonth limite = conta.competenciaFim() != null ? conta.competenciaFim() : YearMonth.now();
        return competencias(conta.competenciaInicio(), limite)
                .map(competencia -> GeradorDespesaRecorrente.gerarSeNecessario(conta, competencia, buscarDespesasPort, salvarDespesaPort))
                .flatMap(Optional::stream)
                .toList();
    }

    private Stream<YearMonth> competencias(YearMonth inicio, YearMonth fim) {
        int totalMeses = (int) ChronoUnit.MONTHS.between(inicio, fim) + 1;
        return Stream.iterate(inicio, competencia -> competencia.plusMonths(1)).limit(Math.max(totalMeses, 0));
    }

    private ContaRecorrente buscarPorIdEValidarDono(UUID id, UUID usuarioId) {
        ContaRecorrente existente = buscarContasRecorrentesPort.buscarPorId(id)
                .orElseThrow(() -> new ContaRecorrenteNaoEncontradaException(id));
        if (!existente.usuarioId().equals(usuarioId)) {
            throw new ContaRecorrenteNaoEncontradaException(id);
        }
        return existente;
    }
}
