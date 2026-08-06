package br.com.rpx.pactumapi.adapter.out.persistence;

import br.com.rpx.pactumapi.adapter.out.persistence.repository.DespesaJpaRepository;
import br.com.rpx.pactumapi.domain.model.CategoriaDespesa;
import br.com.rpx.pactumapi.domain.model.Despesa;
import br.com.rpx.pactumapi.domain.model.StatusDespesa;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(DespesaPersistenceAdapter.class)
class DespesaPersistenceAdapterIT {

    @Autowired DespesaPersistenceAdapter adapter;
    @Autowired DespesaJpaRepository repository;

    private final UUID usuarioId = UUID.randomUUID();

    private Despesa novaDespesa() {
        return new Despesa(null, "Teste", new BigDecimal("500.00"),
                StatusDespesa.PENDENTE, YearMonth.of(2025, 7), CategoriaDespesa.OUTROS, usuarioId, null);
    }

    @Test
    void deve_salvar_e_buscar_despesa_por_id() {
        Despesa salva = adapter.salvar(novaDespesa());

        Optional<Despesa> encontrada = adapter.buscarPorId(salva.id());

        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().descricao()).isEqualTo("Teste");
    }

    @Test
    void deve_buscar_despesas_por_filtros() {
        adapter.salvar(novaDespesa());

        List<Despesa> resultado = adapter.buscarPorFiltros(YearMonth.of(2025, 7), null, null, usuarioId);

        assertThat(resultado).hasSize(1);
    }

    @Test
    void deve_filtrar_por_categoria() {
        adapter.salvar(novaDespesa());
        adapter.salvar(new Despesa(null, "Outra", new BigDecimal("100.00"),
                StatusDespesa.PAGA, YearMonth.of(2025, 7), CategoriaDespesa.LAZER, usuarioId, null));

        List<Despesa> resultado = adapter.buscarPorFiltros(YearMonth.of(2025, 7), CategoriaDespesa.OUTROS, null, usuarioId);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).categoria()).isEqualTo(CategoriaDespesa.OUTROS);
    }

    @Test
    void deve_retornar_apenas_despesas_do_usuario() {
        UUID outroUsuario = UUID.randomUUID();
        adapter.salvar(novaDespesa());
        adapter.salvar(new Despesa(null, "Alheia", new BigDecimal("200.00"),
                StatusDespesa.PENDENTE, YearMonth.of(2025, 7), CategoriaDespesa.OUTROS, outroUsuario, null));

        List<Despesa> resultado = adapter.buscarPorFiltros(YearMonth.of(2025, 7), null, null, usuarioId);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).descricao()).isEqualTo("Teste");
    }

    @Test
    void deve_remover_despesa() {
        Despesa salva = adapter.salvar(novaDespesa());

        adapter.remover(salva.id());

        assertThat(repository.findById(salva.id())).isEmpty();
    }

    @Test
    void deve_persistir_e_recuperar_contaRecorrenteId() {
        UUID contaRecorrenteId = UUID.randomUUID();
        Despesa gerada = new Despesa(null, "Streaming", new BigDecimal("39.90"),
                StatusDespesa.PENDENTE, YearMonth.of(2026, 8), CategoriaDespesa.LAZER, usuarioId, contaRecorrenteId);

        Despesa salva = adapter.salvar(gerada);

        assertThat(adapter.buscarPorId(salva.id())).get()
                .extracting(Despesa::contaRecorrenteId).isEqualTo(contaRecorrenteId);
    }

    @Test
    void deve_indicar_existencia_de_despesa_por_contaRecorrenteId_e_competencia() {
        UUID contaRecorrenteId = UUID.randomUUID();
        adapter.salvar(new Despesa(null, "Streaming", new BigDecimal("39.90"),
                StatusDespesa.PENDENTE, YearMonth.of(2026, 8), CategoriaDespesa.LAZER, usuarioId, contaRecorrenteId));

        boolean existeNaCompetenciaGerada = adapter.existePorContaRecorrenteECompetencia(contaRecorrenteId, YearMonth.of(2026, 8));
        boolean existeEmOutraCompetencia = adapter.existePorContaRecorrenteECompetencia(contaRecorrenteId, YearMonth.of(2026, 9));

        assertThat(existeNaCompetenciaGerada).isTrue();
        assertThat(existeEmOutraCompetencia).isFalse();
    }
}
