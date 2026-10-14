package br.com.rpx.pactumapi.domain.port.in;

import br.com.rpx.pactumapi.domain.model.MetaEconomia;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

public interface DefinirMetaEconomiaUseCase {
    MetaEconomia definir(YearMonth competencia, BigDecimal valor, UUID usuarioId);
}
