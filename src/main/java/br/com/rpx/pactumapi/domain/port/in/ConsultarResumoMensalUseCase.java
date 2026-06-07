package br.com.rpx.pactumapi.domain.port.in;

import br.com.rpx.pactumapi.domain.model.ResumoMensal;

import java.time.YearMonth;
import java.util.UUID;

public interface ConsultarResumoMensalUseCase {
    ResumoMensal consultar(YearMonth competencia, UUID usuarioId);
}
