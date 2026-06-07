package br.com.rpx.pactumapi.domain.port.in;

import br.com.rpx.pactumapi.domain.model.CategoriaReceita;
import br.com.rpx.pactumapi.domain.model.Receita;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

public interface ListarReceitasUseCase {
    List<Receita> listar(YearMonth competencia, CategoriaReceita categoria, UUID usuarioId);
}
