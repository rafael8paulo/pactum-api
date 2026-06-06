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

    @Mock SalvarReceitaPort salvarPort;
    @Mock BuscarReceitasPort buscarPort;
    @Mock RemoverReceitaPort removerPort;
    @InjectMocks ReceitaService service;

    private Receita receitaFixture(UUID id) {
        return new Receita(id, "Salário", new BigDecimal("5000.00"),
                YearMonth.of(2025, 7), CategoriaReceita.SALARIO);
    }

    @Test
    void deve_cadastrar_receita() {
        UUID id = UUID.randomUUID();
        Receita receita = receitaFixture(id);
        when(salvarPort.salvar(receita)).thenReturn(receita);

        Receita resultado = service.cadastrar(receita);

        assertThat(resultado).isEqualTo(receita);
        verify(salvarPort).salvar(receita);
    }

    @Test
    void deve_listar_receitas_por_filtros() {
        YearMonth competencia = YearMonth.of(2025, 7);
        when(buscarPort.buscarPorFiltros(competencia, null)).thenReturn(List.of());

        List<Receita> resultado = service.listar(competencia, null);

        assertThat(resultado).isEmpty();
        verify(buscarPort).buscarPorFiltros(competencia, null);
    }

    @Test
    void deve_editar_receita() {
        UUID id = UUID.randomUUID();
        Receita existente = receitaFixture(id);
        Receita atualizada = new Receita(null, "Salário Atualizado", new BigDecimal("5500.00"),
                YearMonth.of(2025, 7), CategoriaReceita.SALARIO);
        when(buscarPort.buscarPorId(id)).thenReturn(Optional.of(existente));
        when(salvarPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        Receita resultado = service.editar(id, atualizada);

        assertThat(resultado.id()).isEqualTo(id);
        assertThat(resultado.descricao()).isEqualTo("Salário Atualizado");
    }

    @Test
    void deve_lancar_exception_ao_editar_receita_inexistente() {
        UUID id = UUID.randomUUID();
        when(buscarPort.buscarPorId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.editar(id, receitaFixture(id)))
                .isInstanceOf(ReceitaNaoEncontradaException.class);
    }

    @Test
    void deve_remover_receita() {
        UUID id = UUID.randomUUID();
        when(buscarPort.buscarPorId(id)).thenReturn(Optional.of(receitaFixture(id)));

        service.remover(id);

        verify(removerPort).remover(id);
    }

    @Test
    void deve_lancar_exception_ao_remover_receita_inexistente() {
        UUID id = UUID.randomUUID();
        when(buscarPort.buscarPorId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.remover(id))
                .isInstanceOf(ReceitaNaoEncontradaException.class);
    }
}
