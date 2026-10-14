package br.com.rpx.pactumapi.domain.service;

import br.com.rpx.pactumapi.domain.exception.ContaRecorrenteNaoEncontradaException;
import br.com.rpx.pactumapi.domain.model.CategoriaDespesa;
import br.com.rpx.pactumapi.domain.model.ContaRecorrente;
import br.com.rpx.pactumapi.domain.model.FormaPagamento;
import br.com.rpx.pactumapi.domain.model.FrequenciaCobranca;
import br.com.rpx.pactumapi.domain.model.HistoricoValorContaRecorrente;
import br.com.rpx.pactumapi.domain.model.StatusContaRecorrente;
import br.com.rpx.pactumapi.domain.model.TipoFormaPagamento;
import br.com.rpx.pactumapi.domain.port.out.BuscarContasRecorrentesPort;
import br.com.rpx.pactumapi.domain.port.out.BuscarFormasPagamentoPort;
import br.com.rpx.pactumapi.domain.port.out.BuscarHistoricoValorPort;
import br.com.rpx.pactumapi.domain.port.out.RemoverContaRecorrentePort;
import br.com.rpx.pactumapi.domain.port.out.SalvarContaRecorrentePort;
import br.com.rpx.pactumapi.domain.port.out.SalvarHistoricoValorPort;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContaRecorrenteServiceTest {

    @Mock SalvarContaRecorrentePort salvarPort;
    @Mock BuscarContasRecorrentesPort buscarPort;
    @Mock RemoverContaRecorrentePort removerPort;
    @Mock SalvarHistoricoValorPort salvarHistoricoValorPort;
    @Mock BuscarHistoricoValorPort buscarHistoricoValorPort;
    @Mock BuscarFormasPagamentoPort buscarFormasPagamentoPort;
    @InjectMocks ContaRecorrenteService service;

    private final UUID usuarioId = UUID.randomUUID();

    private ContaRecorrente contaFixture(UUID id, StatusContaRecorrente status) {
        return contaFixture(id, status, new BigDecimal("1335.50"), null);
    }

    private ContaRecorrente contaFixture(UUID id, StatusContaRecorrente status, BigDecimal valor, UUID formaPagamentoId) {
        return new ContaRecorrente(id, "Financiamento do Carro", valor,
                CategoriaDespesa.FINANCIAMENTO, 10, YearMonth.of(2026, 1), null, status, usuarioId,
                FrequenciaCobranca.MENSAL, formaPagamentoId, LocalDate.of(2026, 1, 10));
    }

    @Test
    void deve_cadastrar_conta_recorrente_com_status_ativa() {
        ContaRecorrente conta = contaFixture(null, StatusContaRecorrente.ATIVA);
        when(salvarPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        ContaRecorrente resultado = service.cadastrar(conta, usuarioId);

        assertThat(resultado.status()).isEqualTo(StatusContaRecorrente.ATIVA);
        assertThat(resultado.usuarioId()).isEqualTo(usuarioId);
        verify(salvarPort).salvar(any());
    }

    @Test
    void deve_registrar_primeiro_historico_ao_cadastrar() {
        ContaRecorrente conta = contaFixture(null, StatusContaRecorrente.ATIVA);
        when(salvarPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        service.cadastrar(conta, usuarioId);

        verify(salvarHistoricoValorPort).salvar(any(HistoricoValorContaRecorrente.class));
    }

    @Test
    void deve_lancar_excecao_ao_cadastrar_com_formaPagamento_de_outro_usuario() {
        UUID formaPagamentoId = UUID.randomUUID();
        ContaRecorrente conta = contaFixture(null, StatusContaRecorrente.ATIVA, new BigDecimal("1335.50"), formaPagamentoId);
        FormaPagamento formaDeOutroUsuario = new FormaPagamento(formaPagamentoId, "Nubank",
                TipoFormaPagamento.CARTAO_CREDITO, 10, UUID.randomUUID());
        when(buscarFormasPagamentoPort.buscarPorId(formaPagamentoId)).thenReturn(Optional.of(formaDeOutroUsuario));

        assertThatThrownBy(() -> service.cadastrar(conta, usuarioId))
                .isInstanceOf(IllegalArgumentException.class);
        verify(salvarPort, never()).salvar(any());
    }

    @Test
    void deve_lancar_excecao_ao_cadastrar_com_formaPagamento_inexistente() {
        UUID formaPagamentoId = UUID.randomUUID();
        ContaRecorrente conta = contaFixture(null, StatusContaRecorrente.ATIVA, new BigDecimal("1335.50"), formaPagamentoId);
        when(buscarFormasPagamentoPort.buscarPorId(formaPagamentoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cadastrar(conta, usuarioId))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deve_listar_contas_recorrentes_por_status() {
        when(buscarPort.buscarPorFiltros(StatusContaRecorrente.ATIVA, usuarioId)).thenReturn(List.of());

        List<ContaRecorrente> resultado = service.listar(StatusContaRecorrente.ATIVA, usuarioId);

        assertThat(resultado).isEmpty();
        verify(buscarPort).buscarPorFiltros(StatusContaRecorrente.ATIVA, usuarioId);
    }

    @Test
    void deve_editar_conta_recorrente_preservando_status() {
        UUID id = UUID.randomUUID();
        ContaRecorrente existente = contaFixture(id, StatusContaRecorrente.PAUSADA);
        ContaRecorrente novosValores = contaFixture(null, StatusContaRecorrente.ATIVA);
        when(buscarPort.buscarPorId(id)).thenReturn(Optional.of(existente));
        when(salvarPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        ContaRecorrente resultado = service.editar(id, novosValores, usuarioId);

        assertThat(resultado.status()).isEqualTo(StatusContaRecorrente.PAUSADA);
        assertThat(resultado.id()).isEqualTo(id);
    }

    @Test
    void deve_registrar_reajuste_no_historico_quando_valor_muda() {
        UUID id = UUID.randomUUID();
        ContaRecorrente existente = contaFixture(id, StatusContaRecorrente.ATIVA, new BigDecimal("39.90"), null);
        ContaRecorrente novosValores = contaFixture(null, StatusContaRecorrente.ATIVA, new BigDecimal("44.90"), null);
        when(buscarPort.buscarPorId(id)).thenReturn(Optional.of(existente));
        when(salvarPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(buscarHistoricoValorPort.buscarVigente(id)).thenReturn(Optional.of(
                new HistoricoValorContaRecorrente(UUID.randomUUID(), id, new BigDecimal("39.90"), LocalDate.of(2026, 1, 10), null)));

        service.editar(id, novosValores, usuarioId);

        verify(salvarHistoricoValorPort, org.mockito.Mockito.times(2)).salvar(any(HistoricoValorContaRecorrente.class));
    }

    @Test
    void nao_deve_registrar_historico_quando_valor_nao_muda() {
        UUID id = UUID.randomUUID();
        ContaRecorrente existente = contaFixture(id, StatusContaRecorrente.ATIVA, new BigDecimal("39.90"), null);
        ContaRecorrente novosValores = contaFixture(null, StatusContaRecorrente.ATIVA, new BigDecimal("39.90"), null);
        when(buscarPort.buscarPorId(id)).thenReturn(Optional.of(existente));
        when(salvarPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        service.editar(id, novosValores, usuarioId);

        verify(salvarHistoricoValorPort, never()).salvar(any());
    }

    @Test
    void deve_lancar_exception_ao_editar_conta_inexistente() {
        UUID id = UUID.randomUUID();
        when(buscarPort.buscarPorId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.editar(id, contaFixture(null, StatusContaRecorrente.ATIVA), usuarioId))
                .isInstanceOf(ContaRecorrenteNaoEncontradaException.class);
    }

    @Test
    void deve_lancar_exception_ao_editar_conta_de_outro_usuario() {
        UUID id = UUID.randomUUID();
        UUID outroUsuario = UUID.randomUUID();
        when(buscarPort.buscarPorId(id)).thenReturn(Optional.of(contaFixture(id, StatusContaRecorrente.ATIVA)));

        assertThatThrownBy(() -> service.editar(id, contaFixture(null, StatusContaRecorrente.ATIVA), outroUsuario))
                .isInstanceOf(ContaRecorrenteNaoEncontradaException.class);
    }

    @Test
    void deve_pausar_conta_recorrente() {
        UUID id = UUID.randomUUID();
        when(buscarPort.buscarPorId(id)).thenReturn(Optional.of(contaFixture(id, StatusContaRecorrente.ATIVA)));
        when(salvarPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        ContaRecorrente resultado = service.atualizar(id, StatusContaRecorrente.PAUSADA, usuarioId);

        assertThat(resultado.status()).isEqualTo(StatusContaRecorrente.PAUSADA);
    }

    @Test
    void deve_lancar_exception_ao_atualizar_status_de_conta_inexistente() {
        UUID id = UUID.randomUUID();
        when(buscarPort.buscarPorId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizar(id, StatusContaRecorrente.PAUSADA, usuarioId))
                .isInstanceOf(ContaRecorrenteNaoEncontradaException.class);
    }

    @Test
    void deve_remover_conta_recorrente() {
        UUID id = UUID.randomUUID();
        when(buscarPort.buscarPorId(id)).thenReturn(Optional.of(contaFixture(id, StatusContaRecorrente.ATIVA)));

        service.remover(id, usuarioId);

        verify(removerPort).remover(id);
    }

    @Test
    void deve_lancar_exception_ao_remover_conta_de_outro_usuario() {
        UUID id = UUID.randomUUID();
        UUID outroUsuario = UUID.randomUUID();
        when(buscarPort.buscarPorId(id)).thenReturn(Optional.of(contaFixture(id, StatusContaRecorrente.ATIVA)));

        assertThatThrownBy(() -> service.remover(id, outroUsuario))
                .isInstanceOf(ContaRecorrenteNaoEncontradaException.class);
    }
}
