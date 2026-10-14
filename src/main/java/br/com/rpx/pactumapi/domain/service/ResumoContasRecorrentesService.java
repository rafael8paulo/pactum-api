package br.com.rpx.pactumapi.domain.service;

import br.com.rpx.pactumapi.config.UseCase;
import br.com.rpx.pactumapi.domain.model.ContaRecorrente;
import br.com.rpx.pactumapi.domain.model.FormaPagamento;
import br.com.rpx.pactumapi.domain.model.ProximaCobranca;
import br.com.rpx.pactumapi.domain.model.ResumoContasRecorrentes;
import br.com.rpx.pactumapi.domain.model.StatusContaRecorrente;
import br.com.rpx.pactumapi.domain.model.TotalPorFormaPagamento;
import br.com.rpx.pactumapi.domain.port.in.ConsultarProximasCobrancasUseCase;
import br.com.rpx.pactumapi.domain.port.in.ConsultarResumoContasRecorrentesUseCase;
import br.com.rpx.pactumapi.domain.port.out.BuscarContasRecorrentesPort;
import br.com.rpx.pactumapi.domain.port.out.BuscarFormasPagamentoPort;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@UseCase
@RequiredArgsConstructor
public class ResumoContasRecorrentesService implements
        ConsultarResumoContasRecorrentesUseCase, ConsultarProximasCobrancasUseCase {

    private final BuscarContasRecorrentesPort buscarContasRecorrentesPort;
    private final BuscarFormasPagamentoPort buscarFormasPagamentoPort;

    @Override
    public ResumoContasRecorrentes consultar(UUID usuarioId) {
        List<ContaRecorrente> ativas = buscarContasRecorrentesPort.buscarPorFiltros(StatusContaRecorrente.ATIVA, usuarioId);

        BigDecimal totalMensalRecorrente = ativas.stream()
                .map(ContaRecorrente::custoMensal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<UUID, BigDecimal> totalPorFormaId = new LinkedHashMap<>();
        ativas.forEach(conta -> totalPorFormaId.merge(conta.formaPagamentoId(), conta.custoMensal(), BigDecimal::add));

        List<TotalPorFormaPagamento> porFormaPagamento = totalPorFormaId.entrySet().stream()
                .map(entry -> new TotalPorFormaPagamento(entry.getKey(), nomeFormaPagamento(entry.getKey()), entry.getValue()))
                .toList();

        return new ResumoContasRecorrentes(totalMensalRecorrente, porFormaPagamento);
    }

    @Override
    public List<ProximaCobranca> consultar(int dias, UUID usuarioId) {
        LocalDate hoje = LocalDate.now();
        LocalDate limite = hoje.plusDays(dias);
        return buscarContasRecorrentesPort.buscarPorFiltros(StatusContaRecorrente.ATIVA, usuarioId).stream()
                .map(conta -> conta.proximaCobranca(hoje).map(data -> new ProximaCobranca(conta, data)))
                .flatMap(Optional::stream)
                .filter(proxima -> !proxima.data().isAfter(limite))
                .sorted(Comparator.comparing(ProximaCobranca::data))
                .toList();
    }

    private String nomeFormaPagamento(UUID formaPagamentoId) {
        if (formaPagamentoId == null) {
            return null;
        }
        return buscarFormasPagamentoPort.buscarPorId(formaPagamentoId).map(FormaPagamento::nome).orElse(null);
    }
}
