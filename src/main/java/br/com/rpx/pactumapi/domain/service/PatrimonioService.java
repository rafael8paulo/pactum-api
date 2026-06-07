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
import java.util.UUID;

@UseCase
@RequiredArgsConstructor
public class PatrimonioService implements CadastrarPatrimonioUseCase, ConsultarPatrimonioUseCase {

    private final SalvarPatrimonioPort salvarPatrimonioPort;
    private final BuscarPatrimonioPort buscarPatrimonioPort;

    @Override
    public Patrimonio cadastrar(Patrimonio patrimonio, UUID usuarioId) {
        Patrimonio comUsuario = new Patrimonio(null, patrimonio.descricao(), patrimonio.valor(),
                patrimonio.competencia(), usuarioId);
        return salvarPatrimonioPort.salvar(comUsuario);
    }

    @Override
    public List<Patrimonio> consultar(YearMonth competencia, UUID usuarioId) {
        return buscarPatrimonioPort.buscarPorCompetencia(competencia, usuarioId);
    }
}
