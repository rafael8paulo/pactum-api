package br.com.rpx.pactumapi.domain.port.in;

import java.time.YearMonth;
import java.util.UUID;

public interface RemoverMetaEconomiaUseCase {
    void remover(YearMonth competencia, UUID usuarioId);
}
