package br.com.rpx.pactumapi.domain.service;

import br.com.rpx.pactumapi.domain.model.CategoriaDespesa;
import br.com.rpx.pactumapi.domain.model.ContaRecorrente;
import br.com.rpx.pactumapi.domain.model.Despesa;
import br.com.rpx.pactumapi.domain.model.StatusContaRecorrente;
import br.com.rpx.pactumapi.domain.model.StatusDespesa;
import br.com.rpx.pactumapi.domain.port.out.BuscarContasRecorrentesPort;
import br.com.rpx.pactumapi.domain.port.out.BuscarDespesasPort;
import br.com.rpx.pactumapi.domain.port.out.SalvarDespesaPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GerarLancamentosRecorrentesServiceTest {

    @Mock BuscarContasRecorrentesPort buscarContasRecorrentesPort;
    @Mock BuscarDespesasPort buscarDespesasPort;
    @Mock SalvarDespesaPort salvarDespesaPort;
    @InjectMocks GerarLancamentosRecorrentesService service;

    private final UUID usuarioId = UUID.randomUUID();
    private final YearMonth competencia = YearMonth.of(2026, 8);

    private ContaRecorrente contaFixture(YearMonth inicio, YearMonth fim, StatusContaRecorrente status) {
        return new ContaRecorrente(UUID.randomUUID(), "Streaming", new BigDecimal("39.90"),
                CategoriaDespesa.LAZER, null, inicio, fim, status, usuarioId);
    }

    @Test
    void deve_gerar_despesas_para_contas_ativas_elegiveis() {
        ContaRecorrente conta1 = contaFixture(YearMonth.of(2026, 1), null, StatusContaRecorrente.ATIVA);
        ContaRecorrente conta2 = contaFixture(YearMonth.of(2026, 1), null, StatusContaRecorrente.ATIVA);
        when(buscarContasRecorrentesPort.buscarPorFiltros(StatusContaRecorrente.ATIVA, usuarioId))
                .thenReturn(List.of(conta1, conta2));
        when(buscarDespesasPort.existePorContaRecorrenteECompetencia(any(), eq(competencia))).thenReturn(false);
        when(salvarDespesaPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        List<Despesa> resultado = service.gerar(competencia, usuarioId);

        assertThat(resultado).hasSize(2);
        assertThat(resultado).allSatisfy(d -> assertThat(d.status()).isEqualTo(StatusDespesa.PENDENTE));
        assertThat(resultado).extracting(Despesa::contaRecorrenteId)
                .containsExactlyInAnyOrder(conta1.id(), conta2.id());
    }

    @Test
    void nao_deve_gerar_despesa_para_conta_ja_processada_na_competencia() {
        ContaRecorrente conta = contaFixture(YearMonth.of(2026, 1), null, StatusContaRecorrente.ATIVA);
        when(buscarContasRecorrentesPort.buscarPorFiltros(StatusContaRecorrente.ATIVA, usuarioId))
                .thenReturn(List.of(conta));
        when(buscarDespesasPort.existePorContaRecorrenteECompetencia(conta.id(), competencia)).thenReturn(true);

        List<Despesa> resultado = service.gerar(competencia, usuarioId);

        assertThat(resultado).isEmpty();
        verify(salvarDespesaPort, never()).salvar(any());
    }

    @Test
    void nao_deve_gerar_despesa_para_conta_fora_da_janela_de_vigencia() {
        ContaRecorrente conta = contaFixture(YearMonth.of(2026, 1), YearMonth.of(2026, 6), StatusContaRecorrente.ATIVA);
        when(buscarContasRecorrentesPort.buscarPorFiltros(StatusContaRecorrente.ATIVA, usuarioId))
                .thenReturn(List.of(conta));

        List<Despesa> resultado = service.gerar(competencia, usuarioId);

        assertThat(resultado).isEmpty();
        verify(salvarDespesaPort, never()).salvar(any());
    }

    @Test
    void nao_deve_considerar_contas_pausadas_ou_encerradas() {
        when(buscarContasRecorrentesPort.buscarPorFiltros(StatusContaRecorrente.ATIVA, usuarioId))
                .thenReturn(List.of());

        List<Despesa> resultado = service.gerar(competencia, usuarioId);

        assertThat(resultado).isEmpty();
        verify(buscarContasRecorrentesPort).buscarPorFiltros(StatusContaRecorrente.ATIVA, usuarioId);
    }

    @Test
    void deve_isolar_geracao_por_usuario() {
        UUID outroUsuario = UUID.randomUUID();
        when(buscarContasRecorrentesPort.buscarPorFiltros(StatusContaRecorrente.ATIVA, usuarioId))
                .thenReturn(List.of());

        service.gerar(competencia, usuarioId);

        verify(buscarContasRecorrentesPort, never()).buscarPorFiltros(StatusContaRecorrente.ATIVA, outroUsuario);
    }
}
