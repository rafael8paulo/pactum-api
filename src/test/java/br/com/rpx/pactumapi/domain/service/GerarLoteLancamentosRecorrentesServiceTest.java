package br.com.rpx.pactumapi.domain.service;

import br.com.rpx.pactumapi.domain.exception.ContaRecorrenteNaoEncontradaException;
import br.com.rpx.pactumapi.domain.model.CategoriaDespesa;
import br.com.rpx.pactumapi.domain.model.ContaRecorrente;
import br.com.rpx.pactumapi.domain.model.Despesa;
import br.com.rpx.pactumapi.domain.model.StatusContaRecorrente;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GerarLoteLancamentosRecorrentesServiceTest {

    @Mock BuscarContasRecorrentesPort buscarContasRecorrentesPort;
    @Mock BuscarDespesasPort buscarDespesasPort;
    @Mock SalvarDespesaPort salvarDespesaPort;
    @InjectMocks GerarLoteLancamentosRecorrentesService service;

    private final UUID usuarioId = UUID.randomUUID();
    private final UUID contaId = UUID.randomUUID();

    private ContaRecorrente contaFixture(YearMonth inicio, YearMonth fim, StatusContaRecorrente status) {
        return new ContaRecorrente(contaId, "Financiamento", new BigDecimal("500.00"),
                CategoriaDespesa.FINANCIAMENTO, null, inicio, fim, status, usuarioId);
    }

    @Test
    void deve_gerar_uma_despesa_por_competencia_no_intervalo_fechado() {
        ContaRecorrente conta = contaFixture(YearMonth.of(2026, 1), YearMonth.of(2026, 4), StatusContaRecorrente.ATIVA);
        when(buscarContasRecorrentesPort.buscarPorId(contaId)).thenReturn(Optional.of(conta));
        when(buscarDespesasPort.existePorContaRecorrenteECompetencia(eq(contaId), any())).thenReturn(false);
        when(salvarDespesaPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        List<Despesa> resultado = service.gerarTodos(contaId, usuarioId);

        assertThat(resultado).hasSize(4);
        assertThat(resultado).extracting(Despesa::competencia)
                .containsExactly(YearMonth.of(2026, 1), YearMonth.of(2026, 2), YearMonth.of(2026, 3), YearMonth.of(2026, 4));
    }

    @Test
    void deve_cortar_geracao_no_mes_atual_para_conta_indefinida() {
        YearMonth mesAtual = YearMonth.now();
        ContaRecorrente conta = contaFixture(mesAtual.minusMonths(2), null, StatusContaRecorrente.ATIVA);
        when(buscarContasRecorrentesPort.buscarPorId(contaId)).thenReturn(Optional.of(conta));
        when(buscarDespesasPort.existePorContaRecorrenteECompetencia(eq(contaId), any())).thenReturn(false);
        when(salvarDespesaPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        List<Despesa> resultado = service.gerarTodos(contaId, usuarioId);

        assertThat(resultado).hasSize(3);
        assertThat(resultado).extracting(Despesa::competencia).doesNotContain(mesAtual.plusMonths(1));
        assertThat(resultado).extracting(Despesa::competencia).contains(mesAtual);
    }

    @Test
    void deve_gerar_ate_competenciaFim_futuro() {
        YearMonth mesAtual = YearMonth.now();
        ContaRecorrente conta = contaFixture(mesAtual, mesAtual.plusMonths(3), StatusContaRecorrente.ATIVA);
        when(buscarContasRecorrentesPort.buscarPorId(contaId)).thenReturn(Optional.of(conta));
        when(buscarDespesasPort.existePorContaRecorrenteECompetencia(eq(contaId), any())).thenReturn(false);
        when(salvarDespesaPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        List<Despesa> resultado = service.gerarTodos(contaId, usuarioId);

        assertThat(resultado).hasSize(4);
        assertThat(resultado).extracting(Despesa::competencia).contains(mesAtual.plusMonths(3));
    }

    @Test
    void segunda_chamada_nao_duplica_despesas_ja_geradas() {
        ContaRecorrente conta = contaFixture(YearMonth.of(2026, 1), YearMonth.of(2026, 3), StatusContaRecorrente.ATIVA);
        when(buscarContasRecorrentesPort.buscarPorId(contaId)).thenReturn(Optional.of(conta));
        when(buscarDespesasPort.existePorContaRecorrenteECompetencia(eq(contaId), any())).thenReturn(true);

        List<Despesa> resultado = service.gerarTodos(contaId, usuarioId);

        assertThat(resultado).isEmpty();
        verify(salvarDespesaPort, never()).salvar(any());
    }

    @Test
    void deve_retornar_lista_vazia_para_conta_pausada() {
        ContaRecorrente conta = contaFixture(YearMonth.of(2026, 1), YearMonth.of(2026, 3), StatusContaRecorrente.PAUSADA);
        when(buscarContasRecorrentesPort.buscarPorId(contaId)).thenReturn(Optional.of(conta));

        List<Despesa> resultado = service.gerarTodos(contaId, usuarioId);

        assertThat(resultado).isEmpty();
        verify(salvarDespesaPort, never()).salvar(any());
    }

    @Test
    void deve_retornar_lista_vazia_para_conta_encerrada() {
        ContaRecorrente conta = contaFixture(YearMonth.of(2026, 1), YearMonth.of(2026, 3), StatusContaRecorrente.ENCERRADA);
        when(buscarContasRecorrentesPort.buscarPorId(contaId)).thenReturn(Optional.of(conta));

        List<Despesa> resultado = service.gerarTodos(contaId, usuarioId);

        assertThat(resultado).isEmpty();
        verify(salvarDespesaPort, never()).salvar(any());
    }

    @Test
    void deve_lancar_excecao_para_conta_inexistente() {
        when(buscarContasRecorrentesPort.buscarPorId(contaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.gerarTodos(contaId, usuarioId))
                .isInstanceOf(ContaRecorrenteNaoEncontradaException.class);
    }

    @Test
    void deve_lancar_excecao_para_conta_de_outro_usuario() {
        ContaRecorrente conta = contaFixture(YearMonth.of(2026, 1), YearMonth.of(2026, 3), StatusContaRecorrente.ATIVA);
        when(buscarContasRecorrentesPort.buscarPorId(contaId)).thenReturn(Optional.of(conta));

        assertThatThrownBy(() -> service.gerarTodos(contaId, UUID.randomUUID()))
                .isInstanceOf(ContaRecorrenteNaoEncontradaException.class);
    }
}
