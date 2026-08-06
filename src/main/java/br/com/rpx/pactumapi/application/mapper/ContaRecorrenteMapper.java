package br.com.rpx.pactumapi.application.mapper;

import br.com.rpx.pactumapi.application.dto.request.CadastrarContaRecorrenteRequest;
import br.com.rpx.pactumapi.application.dto.request.EditarContaRecorrenteRequest;
import br.com.rpx.pactumapi.application.dto.response.ContaRecorrenteResponse;
import br.com.rpx.pactumapi.application.dto.response.ListaContasRecorrentesResponse;
import br.com.rpx.pactumapi.domain.model.ContaRecorrente;
import br.com.rpx.pactumapi.domain.model.StatusContaRecorrente;

import java.util.List;

public class ContaRecorrenteMapper {

    private ContaRecorrenteMapper() {}

    public static ContaRecorrente toDomain(CadastrarContaRecorrenteRequest request) {
        return new ContaRecorrente(null, request.descricao(), request.valorPadrao(), request.categoria(),
                request.diaVencimento(), request.competenciaInicio(), request.competenciaFim(),
                StatusContaRecorrente.ATIVA, null);
    }

    public static ContaRecorrente toDomain(EditarContaRecorrenteRequest request) {
        return new ContaRecorrente(null, request.descricao(), request.valorPadrao(), request.categoria(),
                request.diaVencimento(), request.competenciaInicio(), request.competenciaFim(),
                StatusContaRecorrente.ATIVA, null);
    }

    public static ContaRecorrenteResponse toResponse(ContaRecorrente contaRecorrente) {
        return new ContaRecorrenteResponse(contaRecorrente.id(), contaRecorrente.descricao(),
                contaRecorrente.valorPadrao(), contaRecorrente.categoria(), contaRecorrente.diaVencimento(),
                contaRecorrente.competenciaInicio(), contaRecorrente.competenciaFim(), contaRecorrente.status());
    }

    public static ListaContasRecorrentesResponse toListResponse(List<ContaRecorrente> contasRecorrentes) {
        return new ListaContasRecorrentesResponse(contasRecorrentes.stream().map(ContaRecorrenteMapper::toResponse).toList());
    }
}
