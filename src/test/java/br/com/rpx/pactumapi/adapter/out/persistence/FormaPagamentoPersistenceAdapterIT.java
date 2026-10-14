package br.com.rpx.pactumapi.adapter.out.persistence;

import br.com.rpx.pactumapi.adapter.out.persistence.repository.FormaPagamentoJpaRepository;
import br.com.rpx.pactumapi.domain.model.FormaPagamento;
import br.com.rpx.pactumapi.domain.model.TipoFormaPagamento;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(FormaPagamentoPersistenceAdapter.class)
class FormaPagamentoPersistenceAdapterIT {

    @Autowired FormaPagamentoPersistenceAdapter adapter;
    @Autowired FormaPagamentoJpaRepository repository;

    private final UUID usuarioId = UUID.randomUUID();

    private FormaPagamento novaForma() {
        return new FormaPagamento(null, "Nubank", TipoFormaPagamento.CARTAO_CREDITO, 25, usuarioId);
    }

    @Test
    void deve_salvar_e_buscar_forma_de_pagamento_por_id() {
        FormaPagamento salva = adapter.salvar(novaForma());

        Optional<FormaPagamento> encontrada = adapter.buscarPorId(salva.id());

        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().nome()).isEqualTo("Nubank");
    }

    @Test
    void deve_listar_formas_de_pagamento_por_usuario() {
        adapter.salvar(novaForma());
        adapter.salvar(novaForma());

        List<FormaPagamento> formas = adapter.listarPorUsuario(usuarioId);

        assertThat(formas).hasSize(2);
    }

    @Test
    void deve_remover_forma_de_pagamento() {
        FormaPagamento salva = adapter.salvar(novaForma());

        adapter.remover(salva.id());

        assertThat(repository.findById(salva.id())).isEmpty();
    }
}
