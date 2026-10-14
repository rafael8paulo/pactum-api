package br.com.rpx.pactumapi.adapter.out.persistence;

import br.com.rpx.pactumapi.adapter.out.persistence.repository.HistoricoValorContaRecorrenteJpaRepository;
import br.com.rpx.pactumapi.domain.model.HistoricoValorContaRecorrente;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(HistoricoValorPersistenceAdapter.class)
class HistoricoValorPersistenceAdapterIT {

    @Autowired HistoricoValorPersistenceAdapter adapter;
    @Autowired HistoricoValorContaRecorrenteJpaRepository repository;

    private final UUID contaRecorrenteId = UUID.randomUUID();

    @Test
    void deve_salvar_e_buscar_registro_vigente() {
        adapter.salvar(new HistoricoValorContaRecorrente(null, contaRecorrenteId, new BigDecimal("39.90"),
                LocalDate.of(2026, 1, 10), null));

        Optional<HistoricoValorContaRecorrente> vigente = adapter.buscarVigente(contaRecorrenteId);

        assertThat(vigente).isPresent();
        assertThat(vigente.get().valor()).isEqualByComparingTo("39.90");
        assertThat(vigente.get().vigenteAte()).isNull();
    }

    @Test
    void nao_deve_encontrar_vigente_quando_todos_os_registros_estao_fechados() {
        adapter.salvar(new HistoricoValorContaRecorrente(null, contaRecorrenteId, new BigDecimal("39.90"),
                LocalDate.of(2026, 1, 10), LocalDate.of(2026, 6, 1)));

        Optional<HistoricoValorContaRecorrente> vigente = adapter.buscarVigente(contaRecorrenteId);

        assertThat(vigente).isEmpty();
    }

    @Test
    void deve_listar_historico_ordenado_do_mais_recente_para_o_mais_antigo() {
        adapter.salvar(new HistoricoValorContaRecorrente(null, contaRecorrenteId, new BigDecimal("39.90"),
                LocalDate.of(2026, 1, 10), LocalDate.of(2026, 6, 1)));
        adapter.salvar(new HistoricoValorContaRecorrente(null, contaRecorrenteId, new BigDecimal("44.90"),
                LocalDate.of(2026, 6, 1), null));

        List<HistoricoValorContaRecorrente> historico = adapter.buscarPorContaRecorrenteOrdenado(contaRecorrenteId);

        assertThat(historico).hasSize(2);
        assertThat(historico.get(0).valor()).isEqualByComparingTo("44.90");
        assertThat(historico.get(1).valor()).isEqualByComparingTo("39.90");
    }

    @Test
    void deve_atualizar_registro_existente_ao_salvar_com_mesmo_id() {
        HistoricoValorContaRecorrente salvo = adapter.salvar(new HistoricoValorContaRecorrente(
                null, contaRecorrenteId, new BigDecimal("39.90"), LocalDate.of(2026, 1, 10), null));

        adapter.salvar(new HistoricoValorContaRecorrente(
                salvo.id(), salvo.contaRecorrenteId(), salvo.valor(), salvo.vigenteDesde(), LocalDate.of(2026, 6, 1)));

        assertThat(repository.findById(salvo.id()).orElseThrow().getVigenteAte()).isEqualTo(LocalDate.of(2026, 6, 1));
    }
}
