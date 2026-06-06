package br.com.rpx.pactumapi.application.mapper;

import br.com.rpx.pactumapi.application.dto.request.CadastrarDespesaRequest;
import br.com.rpx.pactumapi.application.dto.request.EditarDespesaRequest;
import br.com.rpx.pactumapi.application.dto.response.DespesaResponse;
import br.com.rpx.pactumapi.application.dto.response.ListaDespesasResponse;
import br.com.rpx.pactumapi.domain.model.Despesa;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class DespesaMapper {

    private DespesaMapper() {}

    public static Despesa toDomain(CadastrarDespesaRequest request) {
        return new Despesa(null, request.descricao(), request.valor(),
                request.status(), request.competencia(), request.categoria());
    }

    public static Despesa toDomain(EditarDespesaRequest request) {
        return new Despesa(null, request.descricao(), request.valor(),
                request.status(), request.competencia(), request.categoria());
    }

    public static DespesaResponse toResponse(Despesa despesa) {
        return new DespesaResponse(despesa.id(), despesa.descricao(), despesa.valor(),
                despesa.status(), despesa.competencia(), despesa.categoria());
    }

    public static ListaDespesasResponse toListResponse(List<Despesa> despesas) {
        List<DespesaResponse> responses = despesas.stream().map(DespesaMapper::toResponse).toList();
        BigDecimal total = despesas.stream()
                .map(Despesa::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ListaDespesasResponse(responses, total);
    }
}
