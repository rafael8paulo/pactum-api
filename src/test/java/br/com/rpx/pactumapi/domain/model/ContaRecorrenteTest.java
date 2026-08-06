package br.com.rpx.pactumapi.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContaRecorrenteTest {

    private ContaRecorrente contaFixture(YearMonth competenciaInicio, YearMonth competenciaFim) {
        return new ContaRecorrente(UUID.randomUUID(), "Financiamento do Carro", new BigDecimal("1335.50"),
                CategoriaDespesa.FINANCIAMENTO, 10, competenciaInicio, competenciaFim,
                StatusContaRecorrente.ATIVA, UUID.randomUUID());
    }

    @Test
    void deve_criar_conta_recorrente_imutavel_com_todos_os_campos() {
        UUID id = UUID.randomUUID();
        ContaRecorrente conta = new ContaRecorrente(id, "Streaming", new BigDecimal("39.90"),
                CategoriaDespesa.LAZER, null, YearMonth.of(2026, 1), null,
                StatusContaRecorrente.ATIVA, UUID.randomUUID());

        assertThat(conta.id()).isEqualTo(id);
        assertThat(conta.descricao()).isEqualTo("Streaming");
        assertThat(conta.competenciaFim()).isNull();
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
                StatusContaRecorrente.ATIVA, UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deve_conter_todos_os_valores_de_status() {
        assertThat(StatusContaRecorrente.values()).containsExactly(
                StatusContaRecorrente.ATIVA, StatusContaRecorrente.PAUSADA, StatusContaRecorrente.ENCERRADA);
    }
}
