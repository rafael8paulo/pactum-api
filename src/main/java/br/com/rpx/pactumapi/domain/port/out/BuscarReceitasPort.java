package br.com.rpx.pactumapi.domain.port.out;

import br.com.rpx.pactumapi.domain.model.CategoriaReceita;
import br.com.rpx.pactumapi.domain.model.Receita;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BuscarReceitasPort {
    Optional<Receita> buscarPorId(UUID id);
    List<Receita> buscarPorFiltros(YearMonth competencia, CategoriaReceita categoria);
}
