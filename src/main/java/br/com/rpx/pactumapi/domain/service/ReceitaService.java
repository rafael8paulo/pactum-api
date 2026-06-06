package br.com.rpx.pactumapi.domain.service;

import br.com.rpx.pactumapi.config.UseCase;
import br.com.rpx.pactumapi.domain.exception.ReceitaNaoEncontradaException;
import br.com.rpx.pactumapi.domain.model.CategoriaReceita;
import br.com.rpx.pactumapi.domain.model.Receita;
import br.com.rpx.pactumapi.domain.port.in.CadastrarReceitaUseCase;
import br.com.rpx.pactumapi.domain.port.in.EditarReceitaUseCase;
import br.com.rpx.pactumapi.domain.port.in.ListarReceitasUseCase;
import br.com.rpx.pactumapi.domain.port.in.RemoverReceitaUseCase;
import br.com.rpx.pactumapi.domain.port.out.BuscarReceitasPort;
import br.com.rpx.pactumapi.domain.port.out.RemoverReceitaPort;
import br.com.rpx.pactumapi.domain.port.out.SalvarReceitaPort;
import lombok.RequiredArgsConstructor;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@UseCase
@RequiredArgsConstructor
public class ReceitaService implements CadastrarReceitaUseCase, ListarReceitasUseCase, EditarReceitaUseCase, RemoverReceitaUseCase {

    private final SalvarReceitaPort salvarReceitaPort;
    private final BuscarReceitasPort buscarReceitasPort;
    private final RemoverReceitaPort removerReceitaPort;

    @Override
    public Receita cadastrar(Receita receita) {
        return salvarReceitaPort.salvar(receita);
    }

    @Override
    public List<Receita> listar(YearMonth competencia, CategoriaReceita categoria) {
        return buscarReceitasPort.buscarPorFiltros(competencia, categoria);
    }

    @Override
    public Receita editar(UUID id, Receita receita) {
        buscarReceitasPort.buscarPorId(id)
                .orElseThrow(() -> new ReceitaNaoEncontradaException(id));
        Receita receitaAtualizada = new Receita(id, receita.descricao(), receita.valor(), receita.competencia(), receita.categoria());
        return salvarReceitaPort.salvar(receitaAtualizada);
    }

    @Override
    public void remover(UUID id) {
        buscarReceitasPort.buscarPorId(id)
                .orElseThrow(() -> new ReceitaNaoEncontradaException(id));
        removerReceitaPort.remover(id);
    }
}
