package br.com.rpx.pactumapi.adapter.out.persistence;

import br.com.rpx.pactumapi.adapter.out.persistence.repository.MetaEconomiaJpaRepository;
import br.com.rpx.pactumapi.domain.model.MetaEconomia;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(MetaEconomiaPersistenceAdapter.class)
class MetaEconomiaPersistenceAdapterIT {

    @Autowired MetaEconomiaPersistenceAdapter adapter;
    @Autowired MetaEconomiaJpaRepository repository;

    private final UUID usuarioId = UUID.randomUUID();
    private static final YearMonth OUTUBRO = YearMonth.of(2026, 10);

    private MetaEconomia meta(String valor, UUID usuario) {
        return new MetaEconomia(null, OUTUBRO, new BigDecimal(valor), usuario);
    }

    @Test
    void deve_salvar_e_buscar_por_competencia() {
        adapter.salvar(meta("1000.00", usuarioId));

        var encontrada = adapter.buscarPorCompetencia(OUTUBRO, usuarioId);

        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().valor()).isEqualByComparingTo("1000.00");
        assertThat(encontrada.get().competencia()).isEqualTo(OUTUBRO);
    }

    @Test
    void upsert_mantem_uma_unica_linha() {
        MetaEconomia salva = adapter.salvar(meta("1000.00", usuarioId));
        adapter.salvar(new MetaEconomia(salva.id(), OUTUBRO, new BigDecimal("1500.00"), usuarioId));
        repository.flush();

        assertThat(repository.count()).isEqualTo(1);
        assertThat(adapter.buscarPorCompetencia(OUTUBRO, usuarioId).orElseThrow().valor()).isEqualByComparingTo("1500.00");
    }

    @Test
    void deve_isolar_metas_entre_usuarios() {
        adapter.salvar(meta("1000.00", usuarioId));

        assertThat(adapter.buscarPorCompetencia(OUTUBRO, UUID.randomUUID())).isEmpty();
    }

    @Test
    void deve_listar_apenas_metas_do_ano_e_do_usuario_em_ordem() {
        adapter.salvar(new MetaEconomia(null, YearMonth.of(2026, 12), BigDecimal.TEN, usuarioId));
        adapter.salvar(new MetaEconomia(null, YearMonth.of(2026, 1), BigDecimal.TEN, usuarioId));
        adapter.salvar(new MetaEconomia(null, YearMonth.of(2025, 12), BigDecimal.TEN, usuarioId));
        adapter.salvar(new MetaEconomia(null, YearMonth.of(2026, 5), BigDecimal.TEN, UUID.randomUUID()));

        List<MetaEconomia> lista = adapter.listarPorAno(2026, usuarioId);

        assertThat(lista).extracting(MetaEconomia::competencia)
                .containsExactly(YearMonth.of(2026, 1), YearMonth.of(2026, 12));
    }

    @Test
    void deve_remover_por_id() {
        MetaEconomia salva = adapter.salvar(meta("1000.00", usuarioId));

        adapter.remover(salva.id());

        assertThat(adapter.buscarPorCompetencia(OUTUBRO, usuarioId)).isEmpty();
    }
}
