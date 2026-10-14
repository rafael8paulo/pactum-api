package br.com.rpx.pactumapi.domain.service;

import br.com.rpx.pactumapi.domain.exception.CompetenciaEncerradaException;
import br.com.rpx.pactumapi.domain.exception.MetaEconomiaNaoEncontradaException;
import br.com.rpx.pactumapi.domain.model.MetaEconomia;
import br.com.rpx.pactumapi.domain.model.ProgressoMeta;
import br.com.rpx.pactumapi.domain.model.ResultadoMensalMeta;
import br.com.rpx.pactumapi.domain.model.ResumoMensal;
import br.com.rpx.pactumapi.domain.model.StatusMeta;
import br.com.rpx.pactumapi.domain.port.in.ConsultarResumoMensalUseCase;
import br.com.rpx.pactumapi.domain.port.out.BuscarMetaEconomiaPort;
import br.com.rpx.pactumapi.domain.port.out.RemoverMetaEconomiaPort;
import br.com.rpx.pactumapi.domain.port.out.SalvarMetaEconomiaPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetaEconomiaServiceTest {

    @Mock BuscarMetaEconomiaPort buscarPort;
    @Mock SalvarMetaEconomiaPort salvarPort;
    @Mock RemoverMetaEconomiaPort removerPort;
    @Mock ConsultarResumoMensalUseCase resumoUseCase;

    private final UUID usuarioId = UUID.randomUUID();
    private static final YearMonth OUTUBRO = YearMonth.of(2026, 10);

    private MetaEconomiaService serviceEm(String data) {
        Clock clock = Clock.fixed(Instant.parse(data + "T12:00:00Z"), ZoneId.of("UTC"));
        return new MetaEconomiaService(buscarPort, salvarPort, removerPort, resumoUseCase, clock);
    }

    private void meta(YearMonth competencia, String valor, String economizado) {
        when(buscarPort.buscarPorCompetencia(competencia, usuarioId))
                .thenReturn(Optional.of(new MetaEconomia(UUID.randomUUID(), competencia, new BigDecimal(valor), usuarioId)));
        when(resumoUseCase.consultar(competencia, usuarioId)).thenReturn(resumo(competencia, economizado));
    }

    private ResumoMensal resumo(YearMonth competencia, String saldo) {
        BigDecimal s = new BigDecimal(saldo);
        return new ResumoMensal(competencia, s, BigDecimal.ZERO, s);
    }

    @Test
    void deve_calcular_exemplo_de_referencia_600_de_1000_com_10_dias_restantes() {
        meta(OUTUBRO, "1000.00", "600.00");

        ProgressoMeta p = serviceEm("2026-10-22").consultar(OUTUBRO, usuarioId);

        assertThat(p.percentual()).isEqualByComparingTo("60.00");
        assertThat(p.valorRestante()).isEqualByComparingTo("400.00");
        assertThat(p.diasRestantes()).isEqualTo(10);
        assertThat(p.valorDiarioNecessario()).isEqualByComparingTo("40.00");
        assertThat(p.marcosAtingidos()).containsExactly(25, 50);
    }

    @Test
    void deve_projetar_fechamento_linearmente_no_mes_corrente() {
        meta(OUTUBRO, "1000.00", "600.00");

        ProgressoMeta p = serviceEm("2026-10-20").consultar(OUTUBRO, usuarioId);

        // 600 / 20 dias * 31 dias = 930
        assertThat(p.projecaoFechamento()).isEqualByComparingTo("930.00");
    }

    @Test
    void deve_zerar_restante_e_diario_quando_meta_superada() {
        meta(OUTUBRO, "1000.00", "1200.00");

        ProgressoMeta p = serviceEm("2026-10-10").consultar(OUTUBRO, usuarioId);

        assertThat(p.percentual()).isEqualByComparingTo("120.00");
        assertThat(p.valorRestante()).isEqualByComparingTo("0.00");
        assertThat(p.valorDiarioNecessario()).isEqualByComparingTo("0.00");
        assertThat(p.status()).isEqualTo(StatusMeta.CONCLUIDA);
        assertThat(p.marcosAtingidos()).containsExactly(25, 50, 75, 100);
    }

    @Test
    void deve_limitar_percentual_a_zero_com_saldo_negativo() {
        meta(OUTUBRO, "1000.00", "-200.00");

        ProgressoMeta p = serviceEm("2026-10-10").consultar(OUTUBRO, usuarioId);

        assertThat(p.percentual()).isEqualByComparingTo("0.00");
        assertThat(p.valorRestante()).isEqualByComparingTo("1200.00");
        assertThat(p.marcosAtingidos()).isEmpty();
    }

    @Test
    void nao_deve_atingir_marco_25_com_24_99_por_cento() {
        meta(OUTUBRO, "10000.00", "2499.00");

        assertThat(serviceEm("2026-10-10").consultar(OUTUBRO, usuarioId).marcosAtingidos()).isEmpty();
    }

    @Test
    void deve_marcar_adiantada() {
        YearMonth setembro = YearMonth.of(2026, 9); // 30 dias
        meta(setembro, "900.00", "400.00");

        assertThat(serviceEm("2026-09-10").consultar(setembro, usuarioId).status()).isEqualTo(StatusMeta.ADIANTADA);
    }

    @Test
    void deve_marcar_no_planejado() {
        YearMonth setembro = YearMonth.of(2026, 9);
        meta(setembro, "900.00", "290.00");

        assertThat(serviceEm("2026-09-10").consultar(setembro, usuarioId).status()).isEqualTo(StatusMeta.NO_PLANEJADO);
    }

    @Test
    void deve_marcar_em_risco() {
        YearMonth setembro = YearMonth.of(2026, 9);
        meta(setembro, "900.00", "300.00");

        assertThat(serviceEm("2026-09-20").consultar(setembro, usuarioId).status()).isEqualTo(StatusMeta.EM_RISCO);
    }

    @Test
    void competencia_passada_nao_atingida_tem_zero_dias_e_status_em_risco() {
        YearMonth agosto = YearMonth.of(2026, 8);
        meta(agosto, "1000.00", "500.00");

        ProgressoMeta p = serviceEm("2026-10-10").consultar(agosto, usuarioId);

        assertThat(p.diasRestantes()).isZero();
        assertThat(p.valorDiarioNecessario()).isEqualByComparingTo("0.00");
        assertThat(p.projecaoFechamento()).isEqualByComparingTo("500.00");
        assertThat(p.status()).isEqualTo(StatusMeta.EM_RISCO);
    }

    @Test
    void competencia_futura_tem_todos_os_dias_e_status_no_planejado() {
        YearMonth novembro = YearMonth.of(2026, 11);
        meta(novembro, "900.00", "0.00");

        ProgressoMeta p = serviceEm("2026-10-10").consultar(novembro, usuarioId);

        assertThat(p.diasRestantes()).isEqualTo(30);
        assertThat(p.valorDiarioNecessario()).isEqualByComparingTo("30.00");
        assertThat(p.status()).isEqualTo(StatusMeta.NO_PLANEJADO);
    }

    @Test
    void deve_lancar_nao_encontrada_sem_meta() {
        when(buscarPort.buscarPorCompetencia(OUTUBRO, usuarioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceEm("2026-10-10").consultar(OUTUBRO, usuarioId))
                .isInstanceOf(MetaEconomiaNaoEncontradaException.class);
    }

    @Test
    void deve_criar_meta_nova_sem_id() {
        when(buscarPort.buscarPorCompetencia(OUTUBRO, usuarioId)).thenReturn(Optional.empty());
        when(salvarPort.salvar(any())).thenAnswer(i -> i.getArgument(0));

        serviceEm("2026-10-10").definir(OUTUBRO, new BigDecimal("1000"), usuarioId);

        ArgumentCaptor<MetaEconomia> captor = ArgumentCaptor.forClass(MetaEconomia.class);
        verify(salvarPort).salvar(captor.capture());
        assertThat(captor.getValue().id()).isNull();
        assertThat(captor.getValue().valor()).isEqualByComparingTo("1000.00");
    }

    @Test
    void deve_reaproveitar_id_ao_ajustar_meta_existente() {
        UUID id = UUID.randomUUID();
        when(buscarPort.buscarPorCompetencia(OUTUBRO, usuarioId))
                .thenReturn(Optional.of(new MetaEconomia(id, OUTUBRO, new BigDecimal("1000.00"), usuarioId)));
        when(salvarPort.salvar(any())).thenAnswer(i -> i.getArgument(0));

        serviceEm("2026-10-10").definir(OUTUBRO, new BigDecimal("1500"), usuarioId);

        ArgumentCaptor<MetaEconomia> captor = ArgumentCaptor.forClass(MetaEconomia.class);
        verify(salvarPort).salvar(captor.capture());
        assertThat(captor.getValue().id()).isEqualTo(id);
    }

    @Test
    void deve_rejeitar_definir_meta_de_competencia_passada() {
        assertThatThrownBy(() -> serviceEm("2026-10-10").definir(YearMonth.of(2026, 8), BigDecimal.TEN, usuarioId))
                .isInstanceOf(CompetenciaEncerradaException.class);
        verify(salvarPort, never()).salvar(any());
    }

    @Test
    void deve_remover_meta_futura() {
        YearMonth novembro = YearMonth.of(2026, 11);
        UUID id = UUID.randomUUID();
        when(buscarPort.buscarPorCompetencia(novembro, usuarioId))
                .thenReturn(Optional.of(new MetaEconomia(id, novembro, BigDecimal.TEN, usuarioId)));

        serviceEm("2026-10-10").remover(novembro, usuarioId);

        verify(removerPort).remover(id);
    }

    @Test
    void deve_rejeitar_remover_meta_de_competencia_passada() {
        assertThatThrownBy(() -> serviceEm("2026-10-10").remover(YearMonth.of(2026, 8), usuarioId))
                .isInstanceOf(CompetenciaEncerradaException.class);
        verify(removerPort, never()).remover(any());
    }

    @Test
    void deve_lancar_nao_encontrada_ao_remover_sem_meta() {
        when(buscarPort.buscarPorCompetencia(OUTUBRO, usuarioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceEm("2026-10-10").remover(OUTUBRO, usuarioId))
                .isInstanceOf(MetaEconomiaNaoEncontradaException.class);
    }

    @Test
    void historico_lista_meses_com_meta_ja_iniciados_em_ordem() {
        YearMonth agosto = YearMonth.of(2026, 8);
        YearMonth setembro = YearMonth.of(2026, 9);
        YearMonth novembro = YearMonth.of(2026, 11);
        when(buscarPort.listarPorAno(2026, usuarioId)).thenReturn(List.of(
                new MetaEconomia(UUID.randomUUID(), setembro, new BigDecimal("1000.00"), usuarioId),
                new MetaEconomia(UUID.randomUUID(), novembro, new BigDecimal("500.00"), usuarioId),
                new MetaEconomia(UUID.randomUUID(), agosto, new BigDecimal("1000.00"), usuarioId)));
        when(resumoUseCase.consultar(agosto, usuarioId)).thenReturn(resumo(agosto, "1200.00"));
        when(resumoUseCase.consultar(setembro, usuarioId)).thenReturn(resumo(setembro, "400.00"));

        List<ResultadoMensalMeta> historico = serviceEm("2026-10-10").consultar(2026, usuarioId);

        assertThat(historico).extracting(ResultadoMensalMeta::competencia).containsExactly(agosto, setembro);
        assertThat(historico).extracting(ResultadoMensalMeta::atingida).containsExactly(true, false);
        assertThat(historico.get(1).percentual()).isEqualByComparingTo("40.00");
    }

    @Test
    void historico_vazio_sem_metas() {
        when(buscarPort.listarPorAno(2025, usuarioId)).thenReturn(List.of());

        assertThat(serviceEm("2026-10-10").consultar(2025, usuarioId)).isEmpty();
    }
}
