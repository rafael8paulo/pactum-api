package br.com.rpx.pactumapi.adapter.out.persistence;

import br.com.rpx.pactumapi.adapter.out.persistence.repository.ReceitaJpaRepository;
import br.com.rpx.pactumapi.domain.model.CategoriaReceita;
import br.com.rpx.pactumapi.domain.model.Receita;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(ReceitaPersistenceAdapter.class)
class ReceitaPersistenceAdapterIT {

    @Autowired ReceitaPersistenceAdapter adapter;
    @Autowired ReceitaJpaRepository repository;

    private Receita novaReceita() {
        return new Receita(null, "Salário", new BigDecimal("5000.00"),
                YearMonth.of(2025, 7), CategoriaReceita.SALARIO);
    }

    @Test
    void deve_salvar_e_buscar_receita_por_id() {
        Receita salva = adapter.salvar(novaReceita());

        Optional<Receita> encontrada = adapter.buscarPorId(salva.id());

        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().descricao()).isEqualTo("Salário");
    }

    @Test
    void deve_buscar_receitas_por_filtros() {
        adapter.salvar(novaReceita());

        List<Receita> resultado = adapter.buscarPorFiltros(YearMonth.of(2025, 7), null);

        assertThat(resultado).hasSize(1);
    }

    @Test
    void deve_filtrar_por_categoria() {
        adapter.salvar(novaReceita());
        Receita outra = new Receita(null, "Freelance", new BigDecimal("1500.00"),
                YearMonth.of(2025, 7), CategoriaReceita.FREELANCE);
        adapter.salvar(outra);

        List<Receita> resultado = adapter.buscarPorFiltros(YearMonth.of(2025, 7), CategoriaReceita.SALARIO);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).categoria()).isEqualTo(CategoriaReceita.SALARIO);
    }

    @Test
    void deve_remover_receita() {
        Receita salva = adapter.salvar(novaReceita());

        adapter.remover(salva.id());

        assertThat(repository.findById(salva.id())).isEmpty();
    }
}
