package br.com.rpx.pactumapi.application.mapper;

import br.com.rpx.pactumapi.application.dto.request.CadastrarPatrimonioRequest;
import br.com.rpx.pactumapi.application.dto.response.ListaPatrimonioResponse;
import br.com.rpx.pactumapi.application.dto.response.PatrimonioResponse;
import br.com.rpx.pactumapi.domain.model.Patrimonio;

import java.math.BigDecimal;
import java.util.List;

public class PatrimonioMapper {

    private PatrimonioMapper() {}

    public static Patrimonio toDomain(CadastrarPatrimonioRequest request) {
        return new Patrimonio(null, request.descricao(), request.valor(), request.competencia());
    }

    public static PatrimonioResponse toResponse(Patrimonio patrimonio) {
        return new PatrimonioResponse(patrimonio.id(), patrimonio.descricao(), patrimonio.valor(), patrimonio.competencia());
    }

    public static ListaPatrimonioResponse toListResponse(List<Patrimonio> itens) {
        List<PatrimonioResponse> responses = itens.stream().map(PatrimonioMapper::toResponse).toList();
        BigDecimal total = itens.stream()
                .map(Patrimonio::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ListaPatrimonioResponse(responses, total);
    }
}
