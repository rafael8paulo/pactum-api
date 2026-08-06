package br.com.rpx.pactumapi.adapter.out.persistence;

import br.com.rpx.pactumapi.adapter.out.persistence.repository.ContaRecorrenteJpaRepository;
import br.com.rpx.pactumapi.domain.model.CategoriaDespesa;
import br.com.rpx.pactumapi.domain.model.ContaRecorrente;
import br.com.rpx.pactumapi.domain.model.StatusContaRecorrente;
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
@Import(ContaRecorrentePersistenceAdapter.class)
class ContaRecorrentePersistenceAdapterIT {

    @Autowired ContaRecorrentePersistenceAdapter adapter;
    @Autowired ContaRecorrenteJpaRepository repository;

    private final UUID usuarioId = UUID.randomUUID();

    private ContaRecorrente novaConta(StatusContaRecorrente status) {
        return new ContaRecorrente(null, "Streaming", new BigDecimal("39.90"),
                CategoriaDespesa.LAZER, 10, YearMonth.of(2026, 1), null, status, usuarioId);
    }

    @Test
    void deve_salvar_e_buscar_conta_recorrente_por_id() {
        ContaRecorrente salva = adapter.salvar(novaConta(StatusContaRecorrente.ATIVA));

        Optional<ContaRecorrente> encontrada = adapter.buscarPorId(salva.id());

        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().descricao()).isEqualTo("Streaming");
    }

    @Test
    void deve_listar_contas_filtrando_por_status() {
        adapter.salvar(novaConta(StatusContaRecorrente.ATIVA));
        adapter.salvar(novaConta(StatusContaRecorrente.PAUSADA));

        List<ContaRecorrente> ativas = adapter.buscarPorFiltros(StatusContaRecorrente.ATIVA, usuarioId);

        assertThat(ativas).hasSize(1);
        assertThat(ativas.get(0).status()).isEqualTo(StatusContaRecorrente.ATIVA);
    }

    @Test
    void deve_listar_todas_as_contas_do_usuario_sem_filtro_de_status() {
        adapter.salvar(novaConta(StatusContaRecorrente.ATIVA));
        adapter.salvar(novaConta(StatusContaRecorrente.PAUSADA));

        List<ContaRecorrente> todas = adapter.buscarPorFiltros(null, usuarioId);

        assertThat(todas).hasSize(2);
    }

    @Test
    void deve_remover_conta_recorrente() {
        ContaRecorrente salva = adapter.salvar(novaConta(StatusContaRecorrente.ATIVA));

        adapter.remover(salva.id());

        assertThat(repository.findById(salva.id())).isEmpty();
    }
}
