package br.com.rpx.pactumapi.domain.service;

import br.com.rpx.pactumapi.domain.model.ContaRecorrente;
import br.com.rpx.pactumapi.domain.model.HistoricoValorContaRecorrente;
import br.com.rpx.pactumapi.domain.port.out.BuscarHistoricoValorPort;
import br.com.rpx.pactumapi.domain.port.out.SalvarHistoricoValorPort;

import java.time.LocalDate;

class HistoricoValorRecorder {

    private HistoricoValorRecorder() {}

    static void registrarCadastro(ContaRecorrente contaRecorrente, SalvarHistoricoValorPort salvarPort) {
        HistoricoValorContaRecorrente primeiroRegistro = new HistoricoValorContaRecorrente(
                null, contaRecorrente.id(), contaRecorrente.valorPadrao(), LocalDate.now(), null);
        salvarPort.salvar(primeiroRegistro);
    }

    static void registrarReajusteSeNecessario(ContaRecorrente existente, ContaRecorrente editada,
                                               BuscarHistoricoValorPort buscarPort,
                                               SalvarHistoricoValorPort salvarPort) {
        if (existente.valorPadrao().compareTo(editada.valorPadrao()) == 0) {
            return;
        }
        LocalDate hoje = LocalDate.now();
        buscarPort.buscarVigente(existente.id()).ifPresent(vigente -> {
            HistoricoValorContaRecorrente fechado = new HistoricoValorContaRecorrente(
                    vigente.id(), vigente.contaRecorrenteId(), vigente.valor(), vigente.vigenteDesde(), hoje);
            salvarPort.salvar(fechado);
        });
        HistoricoValorContaRecorrente novoRegistro = new HistoricoValorContaRecorrente(
                null, existente.id(), editada.valorPadrao(), hoje, null);
        salvarPort.salvar(novoRegistro);
    }
}
