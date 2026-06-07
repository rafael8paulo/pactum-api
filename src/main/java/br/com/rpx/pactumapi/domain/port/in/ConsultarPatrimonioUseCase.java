package br.com.rpx.pactumapi.domain.port.in;

import br.com.rpx.pactumapi.domain.model.Patrimonio;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

public interface ConsultarPatrimonioUseCase {
    List<Patrimonio> consultar(YearMonth competencia, UUID usuarioId);
}
