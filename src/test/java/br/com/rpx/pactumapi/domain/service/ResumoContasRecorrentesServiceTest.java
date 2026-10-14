package br.com.rpx.pactumapi.domain.service;

import br.com.rpx.pactumapi.domain.model.CategoriaDespesa;
import br.com.rpx.pactumapi.domain.model.ContaRecorrente;
import br.com.rpx.pactumapi.domain.model.FormaPagamento;
import br.com.rpx.pactumapi.domain.model.FrequenciaCobranca;
import br.com.rpx.pactumapi.domain.model.ProximaCobranca;
import br.com.rpx.pactumapi.domain.model.ResumoContasRecorrentes;
import br.com.rpx.pactumapi.domain.model.StatusContaRecorrente;
import br.com.rpx.pactumapi.domain.model.TipoFormaPagamento;
import br.com.rpx.pactumapi.domain.port.out.BuscarContasRecorrentesPort;
import br.com.rpx.pactumapi.domain.port.out.BuscarFormasPagamentoPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResumoContasRecorrentesServiceTest {

    @Mock BuscarContasRecorrentesPort buscarContasRecorrentesPort;
    @Mock BuscarFormasPagamentoPort buscarFormasPagamentoPort;
    @InjectMocks ResumoContasRecorrentesService service;

    private final UUID usuarioId = UUID.randomUUID();

    private ContaRecorrente contaFixture(BigDecimal valor, FrequenciaCobranca frequencia, UUID formaPagamentoId) {
        return new ContaRecorrente(UUID.randomUUID(), "Assinatura", valor, CategoriaDespesa.LAZER, null,
                YearMonth.of(2026, 1), null, StatusContaRecorrente.ATIVA, usuarioId,
                frequencia, formaPagamentoId, LocalDate.of(2026, 1, 1));
    }

    @Test
    void deve_somar_custo_mensal_equivalente_das_contas_ativas() {
        when(buscarContasRecorrentesPort.buscarPorFiltros(StatusContaRecorrente.ATIVA, usuarioId)).thenReturn(List.of(
                contaFixture(new BigDecimal("50.00"), FrequenciaCobranca.MENSAL, null),
                contaFixture(new BigDecimal("1200.00"), FrequenciaCobranca.ANUAL, null),
                contaFixture(new BigDecimal("90.00"), FrequenciaCobranca.TRIMESTRAL, null)
        ));

        ResumoContasRecorrentes resumo = service.consultar(usuarioId);

        assertThat(resumo.totalMensalRecorrente()).isEqualByComparingTo("180.00");
    }

    @Test
    void deve_retornar_zero_quando_nao_ha_contas_recorrentes() {
        when(buscarContasRecorrentesPort.buscarPorFiltros(StatusContaRecorrente.ATIVA, usuarioId)).thenReturn(List.of());

        ResumoContasRecorrentes resumo = service.consultar(usuarioId);

        assertThat(resumo.totalMensalRecorrente()).isEqualByComparingTo("0");
        assertThat(resumo.porFormaPagamento()).isEmpty();
    }

    @Test
    void deve_agrupar_total_por_forma_de_pagamento() {
        UUID formaX = UUID.randomUUID();
        UUID formaY = UUID.randomUUID();
        when(buscarContasRecorrentesPort.buscarPorFiltros(StatusContaRecorrente.ATIVA, usuarioId)).thenReturn(List.of(
                contaFixture(new BigDecimal("30.00"), FrequenciaCobranca.MENSAL, formaX),
                contaFixture(new BigDecimal("30.00"), FrequenciaCobranca.MENSAL, formaX),
                contaFixture(new BigDecimal("20.00"), FrequenciaCobranca.MENSAL, formaY)
        ));
        when(buscarFormasPagamentoPort.buscarPorId(formaX))
                .thenReturn(Optional.of(new FormaPagamento(formaX, "X", TipoFormaPagamento.CARTAO_CREDITO, null, usuarioId)));
        when(buscarFormasPagamentoPort.buscarPorId(formaY))
                .thenReturn(Optional.of(new FormaPagamento(formaY, "Y", TipoFormaPagamento.CONTA_CORRENTE, null, usuarioId)));

        ResumoContasRecorrentes resumo = service.consultar(usuarioId);

        assertThat(resumo.porFormaPagamento()).hasSize(2);
        assertThat(resumo.porFormaPagamento()).anySatisfy(total -> {
            assertThat(total.formaPagamentoId()).isEqualTo(formaX);
            assertThat(total.totalMensal()).isEqualByComparingTo("60.00");
        });
        assertThat(resumo.porFormaPagamento()).anySatisfy(total -> {
            assertThat(total.formaPagamentoId()).isEqualTo(formaY);
            assertThat(total.totalMensal()).isEqualByComparingTo("20.00");
        });
    }

    @Test
    void deve_agrupar_contas_sem_forma_de_pagamento_com_chave_nula() {
        when(buscarContasRecorrentesPort.buscarPorFiltros(StatusContaRecorrente.ATIVA, usuarioId)).thenReturn(List.of(
                contaFixture(new BigDecimal("10.00"), FrequenciaCobranca.MENSAL, null)
        ));

        ResumoContasRecorrentes resumo = service.consultar(usuarioId);

        assertThat(resumo.porFormaPagamento()).hasSize(1);
        assertThat(resumo.porFormaPagamento().get(0).formaPagamentoId()).isNull();
        assertThat(resumo.porFormaPagamento().get(0).totalMensal()).isEqualByComparingTo("10.00");
    }

    @Test
    void deve_listar_apenas_cobrancas_dentro_da_janela_de_dias() {
        ContaRecorrente contaProxima = contaFixtureComDataBase(LocalDate.now().plusDays(3));
        ContaRecorrente contaDistante = contaFixtureComDataBase(LocalDate.now().plusDays(20));
        when(buscarContasRecorrentesPort.buscarPorFiltros(StatusContaRecorrente.ATIVA, usuarioId))
                .thenReturn(List.of(contaProxima, contaDistante));

        List<ProximaCobranca> resultado = service.consultar(7, usuarioId);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).contaRecorrente().id()).isEqualTo(contaProxima.id());
    }

    @Test
    void deve_retornar_lista_vazia_quando_nenhuma_cobranca_esta_na_janela() {
        ContaRecorrente contaDistante = contaFixtureComDataBase(LocalDate.now().plusDays(30));
        when(buscarContasRecorrentesPort.buscarPorFiltros(StatusContaRecorrente.ATIVA, usuarioId))
                .thenReturn(List.of(contaDistante));

        List<ProximaCobranca> resultado = service.consultar(7, usuarioId);

        assertThat(resultado).isEmpty();
    }

    private ContaRecorrente contaFixtureComDataBase(LocalDate dataBaseCobranca) {
        return new ContaRecorrente(UUID.randomUUID(), "Assinatura", new BigDecimal("39.90"), CategoriaDespesa.LAZER, null,
                YearMonth.from(dataBaseCobranca), null, StatusContaRecorrente.ATIVA, usuarioId,
                FrequenciaCobranca.MENSAL, null, dataBaseCobranca);
    }
}
