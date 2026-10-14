package br.com.rpx.pactumapi.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContaRecorrenteTest {

    private ContaRecorrente contaFixture(YearMonth competenciaInicio, YearMonth competenciaFim) {
        return new ContaRecorrente(UUID.randomUUID(), "Financiamento do Carro", new BigDecimal("1335.50"),
                CategoriaDespesa.FINANCIAMENTO, 10, competenciaInicio, competenciaFim,
                StatusContaRecorrente.ATIVA, UUID.randomUUID(), FrequenciaCobranca.MENSAL, null,
                competenciaInicio.atDay(10));
    }

    private ContaRecorrente contaFixture(FrequenciaCobranca frequencia, LocalDate dataBaseCobranca, StatusContaRecorrente status) {
        return new ContaRecorrente(UUID.randomUUID(), "Streaming", new BigDecimal("39.90"),
                CategoriaDespesa.LAZER, null, YearMonth.from(dataBaseCobranca), null,
                status, UUID.randomUUID(), frequencia, null, dataBaseCobranca);
    }

    @Test
    void deve_criar_conta_recorrente_imutavel_com_todos_os_campos() {
        UUID id = UUID.randomUUID();
        ContaRecorrente conta = new ContaRecorrente(id, "Streaming", new BigDecimal("39.90"),
                CategoriaDespesa.LAZER, null, YearMonth.of(2026, 1), null,
                StatusContaRecorrente.ATIVA, UUID.randomUUID(), FrequenciaCobranca.MENSAL, null,
                LocalDate.of(2026, 1, 5));

        assertThat(conta.id()).isEqualTo(id);
        assertThat(conta.descricao()).isEqualTo("Streaming");
        assertThat(conta.competenciaFim()).isNull();
        assertThat(conta.formaPagamentoId()).isNull();
    }

    @Test
    void deve_cobrir_competencia_quando_dentro_da_janela_de_vigencia() {
        ContaRecorrente conta = contaFixture(YearMonth.of(2026, 1), YearMonth.of(2029, 12));

        assertThat(conta.cobreCompetencia(YearMonth.of(2027, 6))).isTrue();
    }

    @Test
    void deve_cobrir_competencia_quando_sem_competenciaFim() {
        ContaRecorrente conta = contaFixture(YearMonth.of(2026, 1), null);

        assertThat(conta.cobreCompetencia(YearMonth.of(2030, 1))).isTrue();
    }

    @Test
    void nao_deve_cobrir_competencia_anterior_ao_inicio() {
        ContaRecorrente conta = contaFixture(YearMonth.of(2026, 1), null);

        assertThat(conta.cobreCompetencia(YearMonth.of(2025, 12))).isFalse();
    }

    @Test
    void nao_deve_cobrir_competencia_posterior_ao_fim() {
        ContaRecorrente conta = contaFixture(YearMonth.of(2026, 1), YearMonth.of(2026, 6));

        assertThat(conta.cobreCompetencia(YearMonth.of(2026, 7))).isFalse();
    }

    @Test
    void deve_lancar_excecao_quando_competenciaFim_anterior_a_competenciaInicio() {
        assertThatThrownBy(() -> new ContaRecorrente(UUID.randomUUID(), "Financiamento", new BigDecimal("100.00"),
                CategoriaDespesa.FINANCIAMENTO, null, YearMonth.of(2026, 8), YearMonth.of(2026, 1),
                StatusContaRecorrente.ATIVA, UUID.randomUUID(), FrequenciaCobranca.MENSAL, null,
                LocalDate.of(2026, 8, 1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deve_lancar_excecao_quando_dataBaseCobranca_fora_do_mes_de_competenciaInicio() {
        assertThatThrownBy(() -> new ContaRecorrente(UUID.randomUUID(), "Streaming", new BigDecimal("39.90"),
                CategoriaDespesa.LAZER, null, YearMonth.of(2026, 8), null,
                StatusContaRecorrente.ATIVA, UUID.randomUUID(), FrequenciaCobranca.MENSAL, null,
                LocalDate.of(2026, 9, 5)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deve_conter_todos_os_valores_de_status() {
        assertThat(StatusContaRecorrente.values()).containsExactly(
                StatusContaRecorrente.ATIVA, StatusContaRecorrente.PAUSADA, StatusContaRecorrente.ENCERRADA);
    }

    @Test
    void deve_conter_todos_os_valores_de_frequencia() {
        assertThat(FrequenciaCobranca.values()).containsExactly(
                FrequenciaCobranca.SEMANAL, FrequenciaCobranca.MENSAL,
                FrequenciaCobranca.TRIMESTRAL, FrequenciaCobranca.ANUAL);
    }

    @Test
    void proximaCobranca_mensal_avanca_em_meses() {
        ContaRecorrente conta = contaFixture(FrequenciaCobranca.MENSAL, LocalDate.of(2026, 8, 10), StatusContaRecorrente.ATIVA);

        Optional<LocalDate> resultado = conta.proximaCobranca(LocalDate.of(2026, 8, 20));

        assertThat(resultado).contains(LocalDate.of(2026, 9, 10));
    }

    @Test
    void proximaCobranca_semanal_avanca_em_semanas() {
        ContaRecorrente conta = contaFixture(FrequenciaCobranca.SEMANAL, LocalDate.of(2026, 8, 4), StatusContaRecorrente.ATIVA);

        Optional<LocalDate> resultado = conta.proximaCobranca(LocalDate.of(2026, 8, 10));

        assertThat(resultado).contains(LocalDate.of(2026, 8, 11));
    }

    @Test
    void proximaCobranca_trimestral_avanca_em_blocos_de_3_meses() {
        ContaRecorrente conta = contaFixture(FrequenciaCobranca.TRIMESTRAL, LocalDate.of(2026, 1, 15), StatusContaRecorrente.ATIVA);

        Optional<LocalDate> resultado = conta.proximaCobranca(LocalDate.of(2026, 8, 20));

        assertThat(resultado).contains(LocalDate.of(2026, 10, 15));
    }

    @Test
    void proximaCobranca_anual_avanca_em_anos() {
        ContaRecorrente conta = contaFixture(FrequenciaCobranca.ANUAL, LocalDate.of(2026, 3, 1), StatusContaRecorrente.ATIVA);

        Optional<LocalDate> resultado = conta.proximaCobranca(LocalDate.of(2026, 8, 20));

        assertThat(resultado).contains(LocalDate.of(2027, 3, 1));
    }

    @Test
    void proximaCobranca_vazia_quando_conta_nao_esta_ativa() {
        ContaRecorrente conta = contaFixture(FrequenciaCobranca.MENSAL, LocalDate.of(2026, 8, 10), StatusContaRecorrente.PAUSADA);

        Optional<LocalDate> resultado = conta.proximaCobranca(LocalDate.of(2026, 8, 20));

        assertThat(resultado).isEmpty();
    }

    @Test
    void custoMensal_mensal_retorna_o_proprio_valor() {
        ContaRecorrente conta = contaFixture(FrequenciaCobranca.MENSAL, LocalDate.of(2026, 1, 1), StatusContaRecorrente.ATIVA);

        assertThat(conta.custoMensal()).isEqualByComparingTo("39.90");
    }

    @Test
    void custoMensal_trimestral_divide_por_3() {
        ContaRecorrente conta = new ContaRecorrente(UUID.randomUUID(), "Seguro", new BigDecimal("90.00"),
                CategoriaDespesa.SERVICOS, null, YearMonth.of(2026, 1), null, StatusContaRecorrente.ATIVA,
                UUID.randomUUID(), FrequenciaCobranca.TRIMESTRAL, null, LocalDate.of(2026, 1, 1));

        assertThat(conta.custoMensal()).isEqualByComparingTo("30.00");
    }

    @Test
    void custoMensal_anual_divide_por_12() {
        ContaRecorrente conta = new ContaRecorrente(UUID.randomUUID(), "Seguro Anual", new BigDecimal("1200.00"),
                CategoriaDespesa.SERVICOS, null, YearMonth.of(2026, 1), null, StatusContaRecorrente.ATIVA,
                UUID.randomUUID(), FrequenciaCobranca.ANUAL, null, LocalDate.of(2026, 1, 1));

        assertThat(conta.custoMensal()).isEqualByComparingTo("100.00");
    }

    @Test
    void custoMensal_semanal_normaliza_para_52_dividido_por_12() {
        ContaRecorrente conta = new ContaRecorrente(UUID.randomUUID(), "Feira", new BigDecimal("50.00"),
                CategoriaDespesa.OUTROS, null, YearMonth.of(2026, 1), null, StatusContaRecorrente.ATIVA,
                UUID.randomUUID(), FrequenciaCobranca.SEMANAL, null, LocalDate.of(2026, 1, 1));

        assertThat(conta.custoMensal()).isEqualByComparingTo(new BigDecimal("50.00")
                .multiply(new BigDecimal("52")).divide(new BigDecimal("12"), 2, java.math.RoundingMode.HALF_UP));
    }
}
