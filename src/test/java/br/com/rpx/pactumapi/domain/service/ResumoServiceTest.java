package br.com.rpx.pactumapi.domain.service;

import br.com.rpx.pactumapi.domain.model.CategoriaReceita;
import br.com.rpx.pactumapi.domain.model.Receita;
import br.com.rpx.pactumapi.domain.model.ResumoMensal;
import br.com.rpx.pactumapi.domain.model.CategoriaDespesa;
import br.com.rpx.pactumapi.domain.model.Despesa;
import br.com.rpx.pactumapi.domain.model.StatusDespesa;
import br.com.rpx.pactumapi.domain.port.out.BuscarDespesasPort;
import br.com.rpx.pactumapi.domain.port.out.BuscarReceitasPort;
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
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResumoServiceTest {

    @Mock BuscarReceitasPort buscarReceitasPort;
    @Mock BuscarDespesasPort buscarDespesasPort;
    @InjectMocks ResumoService service;

    private final UUID usuarioId = UUID.randomUUID();

    private Receita receita(BigDecimal valor) {
        return new Receita(UUID.randomUUID(), "Receita", valor, YearMonth.of(2025, 7), CategoriaReceita.SALARIO, usuarioId);
    }

    private Despesa despesa(BigDecimal valor) {
        return new Despesa(UUID.randomUUID(), "Despesa", valor, StatusDespesa.PAGA, YearMonth.of(2025, 7), CategoriaDespesa.OUTROS, usuarioId);
    }

    @Test
    void deve_retornar_resumo_com_totais_e_saldo_corretos() {
        YearMonth competencia = YearMonth.of(2025, 7);
        when(buscarReceitasPort.buscarPorFiltros(competencia, null, usuarioId))
                .thenReturn(List.of(receita(new BigDecimal("5000.00"))));
        when(buscarDespesasPort.buscarPorFiltros(competencia, null, null, usuarioId))
                .thenReturn(List.of(despesa(new BigDecimal("3000.00"))));

        ResumoMensal resumo = service.consultar(competencia, usuarioId);

        assertThat(resumo.totalReceitas()).isEqualByComparingTo("5000.00");
        assertThat(resumo.totalDespesas()).isEqualByComparingTo("3000.00");
        assertThat(resumo.saldo()).isEqualByComparingTo("2000.00");
    }

    @Test
    void deve_retornar_zeros_para_mes_sem_lancamentos() {
        YearMonth competencia = YearMonth.of(2025, 1);
        when(buscarReceitasPort.buscarPorFiltros(competencia, null, usuarioId)).thenReturn(List.of());
        when(buscarDespesasPort.buscarPorFiltros(competencia, null, null, usuarioId)).thenReturn(List.of());

        ResumoMensal resumo = service.consultar(competencia, usuarioId);

        assertThat(resumo.totalReceitas()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resumo.totalDespesas()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resumo.saldo()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void deve_retornar_historico_com_12_meses() {
        when(buscarReceitasPort.buscarPorFiltros(any(), isNull(), any())).thenReturn(List.of());
        when(buscarDespesasPort.buscarPorFiltros(any(), isNull(), isNull(), any())).thenReturn(List.of());

        List<ResumoMensal> historico = service.consultar(2025, usuarioId);

        assertThat(historico).hasSize(12);
        assertThat(historico.get(0).competencia()).isEqualTo(YearMonth.of(2025, 1));
        assertThat(historico.get(11).competencia()).isEqualTo(YearMonth.of(2025, 12));
    }

    @Test
    void deve_retornar_historico_zerado_quando_sem_lancamentos() {
        when(buscarReceitasPort.buscarPorFiltros(any(), isNull(), any())).thenReturn(List.of());
        when(buscarDespesasPort.buscarPorFiltros(any(), isNull(), isNull(), any())).thenReturn(List.of());

        List<ResumoMensal> historico = service.consultar(2020, usuarioId);

        assertThat(historico).hasSize(12);
        assertThat(historico.stream().map(ResumoMensal::saldo))
                .allMatch(s -> s.compareTo(BigDecimal.ZERO) == 0);
    }
}
