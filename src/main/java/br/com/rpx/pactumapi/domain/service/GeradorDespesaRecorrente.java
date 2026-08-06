package br.com.rpx.pactumapi.domain.service;

import br.com.rpx.pactumapi.domain.model.ContaRecorrente;
import br.com.rpx.pactumapi.domain.model.Despesa;
import br.com.rpx.pactumapi.domain.model.StatusDespesa;
import br.com.rpx.pactumapi.domain.port.out.BuscarDespesasPort;
import br.com.rpx.pactumapi.domain.port.out.SalvarDespesaPort;

import java.time.YearMonth;
import java.util.Optional;

class GeradorDespesaRecorrente {

    private GeradorDespesaRecorrente() {}

    static Optional<Despesa> gerarSeNecessario(ContaRecorrente conta, YearMonth competencia,
                                                BuscarDespesasPort buscarDespesasPort,
                                                SalvarDespesaPort salvarDespesaPort) {
        if (buscarDespesasPort.existePorContaRecorrenteECompetencia(conta.id(), competencia)) {
            return Optional.empty();
        }
        Despesa despesa = new Despesa(null, conta.descricao(), conta.valorPadrao(), StatusDespesa.PENDENTE,
                competencia, conta.categoria(), conta.usuarioId(), conta.id());
        return Optional.of(salvarDespesaPort.salvar(despesa));
    }
}
