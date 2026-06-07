package br.com.rpx.pactumapi.application.mapper;

import br.com.rpx.pactumapi.application.dto.request.CadastrarReceitaRequest;
import br.com.rpx.pactumapi.application.dto.request.EditarReceitaRequest;
import br.com.rpx.pactumapi.application.dto.response.ListaReceitasResponse;
import br.com.rpx.pactumapi.application.dto.response.ReceitaResponse;
import br.com.rpx.pactumapi.domain.model.Receita;

import java.math.BigDecimal;
import java.util.List;

public class ReceitaMapper {

    private ReceitaMapper() {}

    public static Receita toDomain(CadastrarReceitaRequest request) {
        return new Receita(null, request.descricao(), request.valor(), request.competencia(), request.categoria(), null);
    }

    public static Receita toDomain(EditarReceitaRequest request) {
        return new Receita(null, request.descricao(), request.valor(), request.competencia(), request.categoria(), null);
    }

    public static ReceitaResponse toResponse(Receita receita) {
        return new ReceitaResponse(receita.id(), receita.descricao(), receita.valor(), receita.competencia(), receita.categoria());
    }

    public static ListaReceitasResponse toListResponse(List<Receita> receitas) {
        List<ReceitaResponse> responses = receitas.stream().map(ReceitaMapper::toResponse).toList();
        BigDecimal total = receitas.stream()
                .map(Receita::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ListaReceitasResponse(responses, total);
    }
}
