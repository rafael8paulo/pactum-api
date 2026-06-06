package br.com.rpx.pactumapi.domain.port.in;

import br.com.rpx.pactumapi.domain.model.ResumoMensal;

import java.time.YearMonth;

public interface ConsultarResumoMensalUseCase {
    ResumoMensal consultar(YearMonth competencia);
}
