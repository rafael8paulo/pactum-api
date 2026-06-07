package br.com.rpx.pactumapi.domain.service;

import br.com.rpx.pactumapi.domain.exception.ReceitaNaoEncontradaException;
import br.com.rpx.pactumapi.domain.model.CategoriaReceita;
import br.com.rpx.pactumapi.domain.model.Receita;
import br.com.rpx.pactumapi.domain.port.out.BuscarReceitasPort;
import br.com.rpx.pactumapi.domain.port.out.RemoverReceitaPort;
import br.com.rpx.pactumapi.domain.port.out.SalvarReceitaPort;
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
class ReceitaServiceTest {

    @Mock SalvarReceitaPort salvarReceitaPort;
    @Mock BuscarReceitasPort buscarReceitasPort;
    @Mock RemoverReceitaPort removerReceitaPort;
    @InjectMocks ReceitaService service;

    private final UUID usuarioId = UUID.randomUUID();

    private Receita receitaFixture(UUID id) {
        return new Receita(id, "Salário", new BigDecimal("5000.00"),
                YearMonth.of(2025, 7), CategoriaReceita.SALARIO, usuarioId);
    }

    @Test
    void deve_cadastrar_receita() {
        when(salvarReceitaPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        Receita resultado = service.cadastrar(receitaFixture(null), usuarioId);

        assertThat(resultado.usuarioId()).isEqualTo(usuarioId);
        verify(salvarReceitaPort).salvar(any());
    }

    @Test
    void deve_listar_receitas_por_filtros() {
        YearMonth competencia = YearMonth.of(2025, 7);
        when(buscarReceitasPort.buscarPorFiltros(competencia, null, usuarioId)).thenReturn(List.of());

        List<Receita> resultado = service.listar(competencia, null, usuarioId);

        assertThat(resultado).isEmpty();
        verify(buscarReceitasPort).buscarPorFiltros(competencia, null, usuarioId);
    }

    @Test
    void deve_editar_receita() {
        UUID id = UUID.randomUUID();
        Receita existente = receitaFixture(id);
        Receita atualizada = new Receita(null, "Salário Atualizado", new BigDecimal("5500.00"),
                YearMonth.of(2025, 7), CategoriaReceita.SALARIO, usuarioId);
        when(buscarReceitasPort.buscarPorId(id)).thenReturn(Optional.of(existente));
        when(salvarReceitaPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        Receita resultado = service.editar(id, atualizada, usuarioId);

        assertThat(resultado.id()).isEqualTo(id);
        assertThat(resultado.descricao()).isEqualTo("Salário Atualizado");
    }

    @Test
    void deve_lancar_exception_ao_editar_receita_inexistente() {
        UUID id = UUID.randomUUID();
        when(buscarReceitasPort.buscarPorId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.editar(id, receitaFixture(id), usuarioId))
                .isInstanceOf(ReceitaNaoEncontradaException.class);
    }

    @Test
    void deve_lancar_exception_ao_editar_receita_de_outro_usuario() {
        UUID id = UUID.randomUUID();
        UUID outroUsuario = UUID.randomUUID();
        when(buscarReceitasPort.buscarPorId(id)).thenReturn(Optional.of(receitaFixture(id)));

        assertThatThrownBy(() -> service.editar(id, receitaFixture(id), outroUsuario))
                .isInstanceOf(ReceitaNaoEncontradaException.class);
    }

    @Test
    void deve_remover_receita() {
        UUID id = UUID.randomUUID();
        when(buscarReceitasPort.buscarPorId(id)).thenReturn(Optional.of(receitaFixture(id)));

        service.remover(id, usuarioId);

        verify(removerReceitaPort).remover(id);
    }

    @Test
    void deve_lancar_exception_ao_remover_receita_inexistente() {
        UUID id = UUID.randomUUID();
        when(buscarReceitasPort.buscarPorId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.remover(id, usuarioId))
                .isInstanceOf(ReceitaNaoEncontradaException.class);
    }

    @Test
    void deve_lancar_exception_ao_remover_receita_de_outro_usuario() {
        UUID id = UUID.randomUUID();
        UUID outroUsuario = UUID.randomUUID();
        when(buscarReceitasPort.buscarPorId(id)).thenReturn(Optional.of(receitaFixture(id)));

        assertThatThrownBy(() -> service.remover(id, outroUsuario))
                .isInstanceOf(ReceitaNaoEncontradaException.class);
    }
}
