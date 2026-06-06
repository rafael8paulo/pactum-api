package br.com.rpx.pactumapi.domain.port.out;

import br.com.rpx.pactumapi.domain.model.Patrimonio;

import java.time.YearMonth;
import java.util.List;

public interface BuscarPatrimonioPort {
    List<Patrimonio> buscarPorCompetencia(YearMonth competencia);
}
