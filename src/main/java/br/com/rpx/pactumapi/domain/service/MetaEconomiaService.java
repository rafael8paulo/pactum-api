package br.com.rpx.pactumapi.domain.service;

import br.com.rpx.pactumapi.config.UseCase;
import br.com.rpx.pactumapi.domain.exception.CompetenciaEncerradaException;
import br.com.rpx.pactumapi.domain.exception.MetaEconomiaNaoEncontradaException;
import br.com.rpx.pactumapi.domain.model.MetaEconomia;
import br.com.rpx.pactumapi.domain.model.ProgressoMeta;
import br.com.rpx.pactumapi.domain.model.ResultadoMensalMeta;
import br.com.rpx.pactumapi.domain.model.StatusMeta;
import br.com.rpx.pactumapi.domain.port.in.ConsultarHistoricoMetasUseCase;
import br.com.rpx.pactumapi.domain.port.in.ConsultarProgressoMetaUseCase;
import br.com.rpx.pactumapi.domain.port.in.ConsultarResumoMensalUseCase;
import br.com.rpx.pactumapi.domain.port.in.DefinirMetaEconomiaUseCase;
import br.com.rpx.pactumapi.domain.port.in.RemoverMetaEconomiaUseCase;
import br.com.rpx.pactumapi.domain.port.out.BuscarMetaEconomiaPort;
import br.com.rpx.pactumapi.domain.port.out.RemoverMetaEconomiaPort;
import br.com.rpx.pactumapi.domain.port.out.SalvarMetaEconomiaPort;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@UseCase
@RequiredArgsConstructor
public class MetaEconomiaService implements DefinirMetaEconomiaUseCase, ConsultarProgressoMetaUseCase,
        RemoverMetaEconomiaUseCase, ConsultarHistoricoMetasUseCase {

    static final BigDecimal LIMIAR_ADIANTADA = new BigDecimal("1.10");
    static final BigDecimal LIMIAR_NO_PLANEJADO = new BigDecimal("0.90");
    static final List<Integer> MARCOS = List.of(25, 50, 75, 100);

    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    private final BuscarMetaEconomiaPort buscarMetaPort;
    private final SalvarMetaEconomiaPort salvarMetaPort;
    private final RemoverMetaEconomiaPort removerMetaPort;
    private final ConsultarResumoMensalUseCase resumoMensalUseCase;
    private final Clock clock;

    @Override
    public MetaEconomia definir(YearMonth competencia, BigDecimal valor, UUID usuarioId) {
        garantirCompetenciaAberta(competencia);
        UUID id = buscarMetaPort.buscarPorCompetencia(competencia, usuarioId).map(MetaEconomia::id).orElse(null);
        return salvarMetaPort.salvar(new MetaEconomia(id, competencia, valor.setScale(2, RoundingMode.HALF_UP), usuarioId));
    }

    @Override
    public ProgressoMeta consultar(YearMonth competencia, UUID usuarioId) {
        MetaEconomia meta = buscarMetaPort.buscarPorCompetencia(competencia, usuarioId)
                .orElseThrow(() -> new MetaEconomiaNaoEncontradaException(competencia));
        BigDecimal economizado = resumoMensalUseCase.consultar(competencia, usuarioId).saldo();
        return calcular(meta, economizado);
    }

    @Override
    public void remover(YearMonth competencia, UUID usuarioId) {
        garantirCompetenciaAberta(competencia);
        MetaEconomia meta = buscarMetaPort.buscarPorCompetencia(competencia, usuarioId)
                .orElseThrow(() -> new MetaEconomiaNaoEncontradaException(competencia));
        removerMetaPort.remover(meta.id());
    }

    @Override
    public List<ResultadoMensalMeta> consultar(int ano, UUID usuarioId) {
        YearMonth atual = competenciaAtual();
        return buscarMetaPort.listarPorAno(ano, usuarioId).stream()
                .filter(m -> !m.competencia().isAfter(atual))
                .sorted((a, b) -> a.competencia().compareTo(b.competencia()))
                .map(m -> {
                    BigDecimal economizado = resumoMensalUseCase.consultar(m.competencia(), usuarioId).saldo();
                    return new ResultadoMensalMeta(m.competencia(), m.valor(), economizado,
                            percentual(economizado, m.valor()), economizado.compareTo(m.valor()) >= 0);
                })
                .toList();
    }

    private ProgressoMeta calcular(MetaEconomia meta, BigDecimal economizado) {
        YearMonth competencia = meta.competencia();
        BigDecimal valorMeta = meta.valor();
        int diasNoMes = competencia.lengthOfMonth();
        int comparacao = competencia.compareTo(competenciaAtual());
        int diaAtual = LocalDate.now(clock).getDayOfMonth();

        int diasDecorridos = comparacao < 0 ? diasNoMes : comparacao == 0 ? diaAtual : 0;
        int diasRestantes = comparacao < 0 ? 0 : comparacao == 0 ? diasNoMes - diaAtual + 1 : diasNoMes;

        BigDecimal valorRestante = valorMeta.subtract(economizado).max(BigDecimal.ZERO);
        BigDecimal valorDiario = valorRestante.signum() > 0 && diasRestantes > 0
                ? valorRestante.divide(BigDecimal.valueOf(diasRestantes), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2);
        BigDecimal projecao = comparacao == 0
                ? economizado.multiply(BigDecimal.valueOf(diasNoMes))
                        .divide(BigDecimal.valueOf(diasDecorridos), 2, RoundingMode.HALF_UP)
                : economizado;

        BigDecimal percentualBruto = economizado.multiply(CEM).divide(valorMeta, 10, RoundingMode.HALF_UP).max(BigDecimal.ZERO);

        return new ProgressoMeta(competencia, valorMeta, economizado, valorRestante,
                percentual(economizado, valorMeta), diasRestantes, valorDiario, projecao,
                status(comparacao, valorMeta, economizado, diasDecorridos, diasNoMes),
                MARCOS.stream().filter(m -> percentualBruto.compareTo(BigDecimal.valueOf(m)) >= 0).toList());
    }

    private StatusMeta status(int comparacao, BigDecimal valorMeta, BigDecimal economizado,
                              int diasDecorridos, int diasNoMes) {
        if (economizado.compareTo(valorMeta) >= 0) return StatusMeta.CONCLUIDA;
        if (comparacao < 0) return StatusMeta.EM_RISCO;
        if (comparacao > 0) return StatusMeta.NO_PLANEJADO;
        BigDecimal esperado = valorMeta.multiply(BigDecimal.valueOf(diasDecorridos))
                .divide(BigDecimal.valueOf(diasNoMes), 10, RoundingMode.HALF_UP);
        if (economizado.compareTo(esperado.multiply(LIMIAR_ADIANTADA)) >= 0) return StatusMeta.ADIANTADA;
        if (economizado.compareTo(esperado.multiply(LIMIAR_NO_PLANEJADO)) >= 0) return StatusMeta.NO_PLANEJADO;
        return StatusMeta.EM_RISCO;
    }

    private BigDecimal percentual(BigDecimal economizado, BigDecimal valorMeta) {
        return economizado.multiply(CEM).divide(valorMeta, 2, RoundingMode.HALF_UP).max(BigDecimal.ZERO.setScale(2));
    }

    private YearMonth competenciaAtual() {
        return YearMonth.now(clock);
    }

    private void garantirCompetenciaAberta(YearMonth competencia) {
        if (competencia.isBefore(competenciaAtual())) {
            throw new CompetenciaEncerradaException(competencia);
        }
    }
}
