package br.com.rpx.pactumapi.domain.port.out;

import br.com.rpx.pactumapi.domain.model.MetaEconomia;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BuscarMetaEconomiaPort {
    Optional<MetaEconomia> buscarPorCompetencia(YearMonth competencia, UUID usuarioId);
    List<MetaEconomia> listarPorAno(int ano, UUID usuarioId);
}
