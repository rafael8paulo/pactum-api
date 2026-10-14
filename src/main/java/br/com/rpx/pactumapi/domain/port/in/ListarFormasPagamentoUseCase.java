package br.com.rpx.pactumapi.domain.port.in;

import br.com.rpx.pactumapi.domain.model.FormaPagamento;

import java.util.List;
import java.util.UUID;

public interface ListarFormasPagamentoUseCase {
    List<FormaPagamento> listar(UUID usuarioId);
}
