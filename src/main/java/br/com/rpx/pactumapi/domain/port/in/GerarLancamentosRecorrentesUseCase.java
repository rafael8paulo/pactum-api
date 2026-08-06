package br.com.rpx.pactumapi.domain.port.in;

import br.com.rpx.pactumapi.domain.model.Despesa;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

public interface GerarLancamentosRecorrentesUseCase {
    List<Despesa> gerar(YearMonth competencia, UUID usuarioId);
}
