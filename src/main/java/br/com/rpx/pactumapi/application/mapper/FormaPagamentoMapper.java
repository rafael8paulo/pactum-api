package br.com.rpx.pactumapi.application.mapper;

import br.com.rpx.pactumapi.application.dto.request.CadastrarFormaPagamentoRequest;
import br.com.rpx.pactumapi.application.dto.request.EditarFormaPagamentoRequest;
import br.com.rpx.pactumapi.application.dto.response.FormaPagamentoResponse;
import br.com.rpx.pactumapi.application.dto.response.ListaFormasPagamentoResponse;
import br.com.rpx.pactumapi.domain.model.FormaPagamento;

import java.util.List;

public class FormaPagamentoMapper {

    private FormaPagamentoMapper() {}

    public static FormaPagamento toDomain(CadastrarFormaPagamentoRequest request) {
        return new FormaPagamento(null, request.nome(), request.tipo(), request.diaFechamentoFatura(), null);
    }

    public static FormaPagamento toDomain(EditarFormaPagamentoRequest request) {
        return new FormaPagamento(null, request.nome(), request.tipo(), request.diaFechamentoFatura(), null);
    }

    public static FormaPagamentoResponse toResponse(FormaPagamento formaPagamento) {
        return new FormaPagamentoResponse(formaPagamento.id(), formaPagamento.nome(), formaPagamento.tipo(),
                formaPagamento.diaFechamentoFatura());
    }

    public static ListaFormasPagamentoResponse toListResponse(List<FormaPagamento> formasPagamento) {
        return new ListaFormasPagamentoResponse(formasPagamento.stream().map(FormaPagamentoMapper::toResponse).toList());
    }
}
