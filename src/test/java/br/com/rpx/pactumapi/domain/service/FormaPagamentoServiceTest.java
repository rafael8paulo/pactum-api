package br.com.rpx.pactumapi.domain.service;

import br.com.rpx.pactumapi.domain.exception.FormaPagamentoNaoEncontradaException;
import br.com.rpx.pactumapi.domain.model.FormaPagamento;
import br.com.rpx.pactumapi.domain.model.TipoFormaPagamento;
import br.com.rpx.pactumapi.domain.port.out.BuscarFormasPagamentoPort;
import br.com.rpx.pactumapi.domain.port.out.RemoverFormaPagamentoPort;
import br.com.rpx.pactumapi.domain.port.out.SalvarFormaPagamentoPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FormaPagamentoServiceTest {

    @Mock SalvarFormaPagamentoPort salvarPort;
    @Mock BuscarFormasPagamentoPort buscarPort;
    @Mock RemoverFormaPagamentoPort removerPort;
    @InjectMocks FormaPagamentoService service;

    private final UUID usuarioId = UUID.randomUUID();

    private FormaPagamento formaFixture(UUID id) {
        return new FormaPagamento(id, "Nubank", TipoFormaPagamento.CARTAO_CREDITO, 25, usuarioId);
    }

    @Test
    void deve_cadastrar_forma_de_pagamento() {
        FormaPagamento forma = formaFixture(null);
        when(salvarPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        FormaPagamento resultado = service.cadastrar(forma, usuarioId);

        assertThat(resultado.usuarioId()).isEqualTo(usuarioId);
        verify(salvarPort).salvar(any());
    }

    @Test
    void deve_listar_formas_de_pagamento_do_usuario() {
        when(buscarPort.listarPorUsuario(usuarioId)).thenReturn(List.of(formaFixture(UUID.randomUUID())));

        List<FormaPagamento> resultado = service.listar(usuarioId);

        assertThat(resultado).hasSize(1);
    }

    @Test
    void deve_editar_forma_de_pagamento() {
        UUID id = UUID.randomUUID();
        when(buscarPort.buscarPorId(id)).thenReturn(Optional.of(formaFixture(id)));
        when(salvarPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        FormaPagamento resultado = service.editar(id, formaFixture(null), usuarioId);

        assertThat(resultado.id()).isEqualTo(id);
    }

    @Test
    void deve_lancar_excecao_ao_editar_forma_inexistente() {
        UUID id = UUID.randomUUID();
        when(buscarPort.buscarPorId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.editar(id, formaFixture(null), usuarioId))
                .isInstanceOf(FormaPagamentoNaoEncontradaException.class);
    }

    @Test
    void deve_lancar_excecao_ao_editar_forma_de_outro_usuario() {
        UUID id = UUID.randomUUID();
        when(buscarPort.buscarPorId(id)).thenReturn(Optional.of(formaFixture(id)));

        assertThatThrownBy(() -> service.editar(id, formaFixture(null), UUID.randomUUID()))
                .isInstanceOf(FormaPagamentoNaoEncontradaException.class);
    }

    @Test
    void deve_remover_forma_de_pagamento() {
        UUID id = UUID.randomUUID();
        when(buscarPort.buscarPorId(id)).thenReturn(Optional.of(formaFixture(id)));

        service.remover(id, usuarioId);

        verify(removerPort).remover(id);
    }

    @Test
    void deve_lancar_excecao_ao_remover_forma_de_outro_usuario() {
        UUID id = UUID.randomUUID();
        when(buscarPort.buscarPorId(id)).thenReturn(Optional.of(formaFixture(id)));

        assertThatThrownBy(() -> service.remover(id, UUID.randomUUID()))
                .isInstanceOf(FormaPagamentoNaoEncontradaException.class);
    }

    @Test
    void deve_lancar_excecao_ao_remover_forma_inexistente() {
        UUID id = UUID.randomUUID();
        when(buscarPort.buscarPorId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.remover(id, usuarioId))
                .isInstanceOf(FormaPagamentoNaoEncontradaException.class);
    }
}
