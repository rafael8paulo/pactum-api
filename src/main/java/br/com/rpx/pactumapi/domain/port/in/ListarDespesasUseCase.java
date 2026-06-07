package br.com.rpx.pactumapi.domain.port.in;

import br.com.rpx.pactumapi.domain.model.CategoriaDespesa;
import br.com.rpx.pactumapi.domain.model.Despesa;
import br.com.rpx.pactumapi.domain.model.StatusDespesa;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

public interface ListarDespesasUseCase {
    List<Despesa> listar(YearMonth competencia, CategoriaDespesa categoria, StatusDespesa status, UUID usuarioId);
}
