package br.com.rpx.pactumapi.domain.port.out;

import br.com.rpx.pactumapi.domain.model.FormaPagamento;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BuscarFormasPagamentoPort {
    Optional<FormaPagamento> buscarPorId(UUID id);
    List<FormaPagamento> listarPorUsuario(UUID usuarioId);
}
