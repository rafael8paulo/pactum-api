package br.com.rpx.pactumapi.domain.service;

import br.com.rpx.pactumapi.config.UseCase;
import br.com.rpx.pactumapi.domain.exception.ContaRecorrenteNaoEncontradaException;
import br.com.rpx.pactumapi.domain.model.HistoricoValorContaRecorrente;
import br.com.rpx.pactumapi.domain.port.in.ConsultarHistoricoValorUseCase;
import br.com.rpx.pactumapi.domain.port.out.BuscarContasRecorrentesPort;
import br.com.rpx.pactumapi.domain.port.out.BuscarHistoricoValorPort;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@UseCase
@RequiredArgsConstructor
public class HistoricoValorService implements ConsultarHistoricoValorUseCase {

    private final BuscarContasRecorrentesPort buscarContasRecorrentesPort;
    private final BuscarHistoricoValorPort buscarHistoricoValorPort;

    @Override
    public List<HistoricoValorContaRecorrente> consultar(UUID contaRecorrenteId, UUID usuarioId) {
        var contaRecorrente = buscarContasRecorrentesPort.buscarPorId(contaRecorrenteId)
                .orElseThrow(() -> new ContaRecorrenteNaoEncontradaException(contaRecorrenteId));
        if (!contaRecorrente.usuarioId().equals(usuarioId)) {
            throw new ContaRecorrenteNaoEncontradaException(contaRecorrenteId);
        }
        return buscarHistoricoValorPort.buscarPorContaRecorrenteOrdenado(contaRecorrenteId);
    }
}
