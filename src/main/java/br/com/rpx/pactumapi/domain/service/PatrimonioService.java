package br.com.rpx.pactumapi.domain.service;

import br.com.rpx.pactumapi.config.UseCase;
import br.com.rpx.pactumapi.domain.model.Patrimonio;
import br.com.rpx.pactumapi.domain.port.in.CadastrarPatrimonioUseCase;
import br.com.rpx.pactumapi.domain.port.in.ConsultarPatrimonioUseCase;
import br.com.rpx.pactumapi.domain.port.out.BuscarPatrimonioPort;
import br.com.rpx.pactumapi.domain.port.out.SalvarPatrimonioPort;
import lombok.RequiredArgsConstructor;

import java.time.YearMonth;
import java.util.List;

@UseCase
@RequiredArgsConstructor
public class PatrimonioService implements CadastrarPatrimonioUseCase, ConsultarPatrimonioUseCase {

    private final SalvarPatrimonioPort salvarPatrimonioPort;
    private final BuscarPatrimonioPort buscarPatrimonioPort;

    @Override
    public Patrimonio cadastrar(Patrimonio patrimonio) {
        return salvarPatrimonioPort.salvar(patrimonio);
    }

    @Override
    public List<Patrimonio> consultar(YearMonth competencia) {
        return buscarPatrimonioPort.buscarPorCompetencia(competencia);
    }
}
