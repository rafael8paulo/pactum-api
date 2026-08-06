package br.com.rpx.pactumapi.domain.service;

import br.com.rpx.pactumapi.domain.exception.ContaRecorrenteNaoEncontradaException;
import br.com.rpx.pactumapi.domain.model.CategoriaDespesa;
import br.com.rpx.pactumapi.domain.model.ContaRecorrente;
import br.com.rpx.pactumapi.domain.model.StatusContaRecorrente;
import br.com.rpx.pactumapi.domain.port.out.BuscarContasRecorrentesPort;
import br.com.rpx.pactumapi.domain.port.out.RemoverContaRecorrentePort;
import br.com.rpx.pactumapi.domain.port.out.SalvarContaRecorrentePort;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContaRecorrenteServiceTest {

    @Mock SalvarContaRecorrentePort salvarPort;
    @Mock BuscarContasRecorrentesPort buscarPort;
    @Mock RemoverContaRecorrentePort removerPort;
    @InjectMocks ContaRecorrenteService service;

    private final UUID usuarioId = UUID.randomUUID();

    private ContaRecorrente contaFixture(UUID id, StatusContaRecorrente status) {
        return new ContaRecorrente(id, "Financiamento do Carro", new BigDecimal("1335.50"),
                CategoriaDespesa.FINANCIAMENTO, 10, YearMonth.of(2026, 1), null, status, usuarioId);
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
