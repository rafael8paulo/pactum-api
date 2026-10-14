package br.com.rpx.pactumapi.domain.port.out;

import br.com.rpx.pactumapi.domain.model.FormaPagamento;

public interface SalvarFormaPagamentoPort {
    FormaPagamento salvar(FormaPagamento formaPagamento);
}
