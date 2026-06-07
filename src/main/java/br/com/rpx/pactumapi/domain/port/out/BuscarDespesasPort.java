package br.com.rpx.pactumapi.domain.port.out;

import br.com.rpx.pactumapi.domain.model.CategoriaDespesa;
import br.com.rpx.pactumapi.domain.model.Despesa;
import br.com.rpx.pactumapi.domain.model.StatusDespesa;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BuscarDespesasPort {
    Optional<Despesa> buscarPorId(UUID id);
    List<Despesa> buscarPorFiltros(YearMonth competencia, CategoriaDespesa categoria, StatusDespesa status, UUID usuarioId);
}
