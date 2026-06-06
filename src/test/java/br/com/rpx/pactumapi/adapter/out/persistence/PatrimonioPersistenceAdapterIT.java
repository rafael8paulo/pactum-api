package br.com.rpx.pactumapi.adapter.out.persistence;

import br.com.rpx.pactumapi.adapter.out.persistence.repository.PatrimonioJpaRepository;
import br.com.rpx.pactumapi.domain.model.Patrimonio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(PatrimonioPersistenceAdapter.class)
class PatrimonioPersistenceAdapterIT {

    @Autowired PatrimonioPersistenceAdapter adapter;
    @Autowired PatrimonioJpaRepository repository;

    private Patrimonio novoPatrimonio() {
        return new Patrimonio(null, "Caixinha Turbo Nubank", new BigDecimal("5069.02"), YearMonth.of(2025, 7));
    }

    @Test
    void deve_salvar_e_buscar_patrimonio_por_competencia() {
        adapter.salvar(novoPatrimonio());

        List<Patrimonio> resultado = adapter.buscarPorCompetencia(YearMonth.of(2025, 7));

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).descricao()).isEqualTo("Caixinha Turbo Nubank");
    }

    @Test
    void deve_retornar_lista_vazia_para_competencia_sem_itens() {
        adapter.salvar(novoPatrimonio());

        List<Patrimonio> resultado = adapter.buscarPorCompetencia(YearMonth.of(2025, 1));

        assertThat(resultado).isEmpty();
    }

    @Test
    void deve_retornar_multiplos_itens_do_mesmo_mes() {
        adapter.salvar(novoPatrimonio());
        adapter.salvar(new Patrimonio(null, "Carteira", new BigDecimal("500.00"), YearMonth.of(2025, 7)));

        List<Patrimonio> resultado = adapter.buscarPorCompetencia(YearMonth.of(2025, 7));

        assertThat(resultado).hasSize(2);
    }
}
