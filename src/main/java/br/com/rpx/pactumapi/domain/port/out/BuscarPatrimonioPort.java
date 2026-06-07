package br.com.rpx.pactumapi.domain.port.out;

import br.com.rpx.pactumapi.domain.model.Patrimonio;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

public interface BuscarPatrimonioPort {
    List<Patrimonio> buscarPorCompetencia(YearMonth competencia, UUID usuarioId);
}
