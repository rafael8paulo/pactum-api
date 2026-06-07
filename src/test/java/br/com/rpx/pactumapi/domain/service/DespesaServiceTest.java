package br.com.rpx.pactumapi.domain.service;

import br.com.rpx.pactumapi.domain.exception.DespesaNaoEncontradaException;
import br.com.rpx.pactumapi.domain.model.CategoriaDespesa;
import br.com.rpx.pactumapi.domain.model.Despesa;
import br.com.rpx.pactumapi.domain.model.StatusDespesa;
import br.com.rpx.pactumapi.domain.port.out.BuscarDespesasPort;
import br.com.rpx.pactumapi.domain.port.out.RemoverDespesaPort;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DespesaServiceTest {

    @Mock SalvarDespesaPort salvarPort;
    @Mock BuscarDespesasPort buscarPort;
    @Mock RemoverDespesaPort removerPort;
    @InjectMocks DespesaService service;

    private final UUID usuarioId = UUID.randomUUID();

    private Despesa despesaFixture(UUID id) {
        return new Despesa(id, "Financiamento", new BigDecimal("1335.50"),
                StatusDespesa.PAGA, YearMonth.of(2025, 7), CategoriaDespesa.FINANCIAMENTO, usuarioId);
    }

    @Test
    void deve_cadastrar_despesa() {
        Despesa despesa = despesaFixture(null);
        when(salvarPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        Despesa resultado = service.cadastrar(despesa, usuarioId);

        assertThat(resultado.usuarioId()).isEqualTo(usuarioId);
        verify(salvarPort).salvar(any());
    }

    @Test
    void deve_listar_despesas_por_filtros() {
        YearMonth competencia = YearMonth.of(2025, 7);
        when(buscarPort.buscarPorFiltros(competencia, null, null, usuarioId)).thenReturn(List.of());

        List<Despesa> resultado = service.listar(competencia, null, null, usuarioId);

        assertThat(resultado).isEmpty();
        verify(buscarPort).buscarPorFiltros(competencia, null, null, usuarioId);
    }

    @Test
    void deve_atualizar_status_despesa() {
        UUID id = UUID.randomUUID();
        Despesa existente = despesaFixture(id);
        when(buscarPort.buscarPorId(id)).thenReturn(Optional.of(existente));
        when(salvarPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        Despesa resultado = service.atualizar(id, StatusDespesa.PENDENTE, usuarioId);

        assertThat(resultado.status()).isEqualTo(StatusDespesa.PENDENTE);
        assertThat(resultado.id()).isEqualTo(id);
    }

    @Test
    void deve_lancar_exception_ao_atualizar_despesa_inexistente() {
        UUID id = UUID.randomUUID();
        when(buscarPort.buscarPorId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizar(id, StatusDespesa.PAGA, usuarioId))
                .isInstanceOf(DespesaNaoEncontradaException.class);
    }

    @Test
    void deve_lancar_exception_ao_atualizar_despesa_de_outro_usuario() {
        UUID id = UUID.randomUUID();
        UUID outroUsuario = UUID.randomUUID();
        when(buscarPort.buscarPorId(id)).thenReturn(Optional.of(despesaFixture(id)));

        assertThatThrownBy(() -> service.atualizar(id, StatusDespesa.PAGA, outroUsuario))
                .isInstanceOf(DespesaNaoEncontradaException.class);
    }

    @Test
    void deve_remover_despesa() {
        UUID id = UUID.randomUUID();
        when(buscarPort.buscarPorId(id)).thenReturn(Optional.of(despesaFixture(id)));

        service.remover(id, usuarioId);

        verify(removerPort).remover(id);
    }

    @Test
    void deve_lancar_exception_ao_remover_despesa_inexistente() {
        UUID id = UUID.randomUUID();
        when(buscarPort.buscarPorId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.remover(id, usuarioId))
                .isInstanceOf(DespesaNaoEncontradaException.class);
    }

    @Test
    void deve_lancar_exception_ao_remover_despesa_de_outro_usuario() {
        UUID id = UUID.randomUUID();
        UUID outroUsuario = UUID.randomUUID();
        when(buscarPort.buscarPorId(id)).thenReturn(Optional.of(despesaFixture(id)));

        assertThatThrownBy(() -> service.remover(id, outroUsuario))
                .isInstanceOf(DespesaNaoEncontradaException.class);
    }
}
