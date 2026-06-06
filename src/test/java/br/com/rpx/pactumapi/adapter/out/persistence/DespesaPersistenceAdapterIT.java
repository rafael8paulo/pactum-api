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

    private Despesa novaDespesa() {
        return new Despesa(null, "Teste", new BigDecimal("500.00"),
                StatusDespesa.PENDENTE, YearMonth.of(2025, 7), CategoriaDespesa.OUTROS);
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

        List<Despesa> resultado = adapter.buscarPorFiltros(YearMonth.of(2025, 7), null, null);

        assertThat(resultado).hasSize(1);
    }

    @Test
    void deve_filtrar_por_categoria() {
        adapter.salvar(novaDespesa());
        Despesa outra = new Despesa(null, "Outra", new BigDecimal("100.00"),
                StatusDespesa.PAGA, YearMonth.of(2025, 7), CategoriaDespesa.LAZER);
        adapter.salvar(outra);

        List<Despesa> resultado = adapter.buscarPorFiltros(YearMonth.of(2025, 7), CategoriaDespesa.OUTROS, null);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).categoria()).isEqualTo(CategoriaDespesa.OUTROS);
    }

    @Test
    void deve_remover_despesa() {
        Despesa salva = adapter.salvar(novaDespesa());

        adapter.remover(salva.id());

        assertThat(repository.findById(salva.id())).isEmpty();
    }
}
