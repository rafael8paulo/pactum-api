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
    public Receita cadastrar(Receita receita, UUID usuarioId) {
        Receita comUsuario = new Receita(null, receita.descricao(), receita.valor(),
                receita.competencia(), receita.categoria(), usuarioId);
        return salvarReceitaPort.salvar(comUsuario);
    }

    @Override
    public List<Receita> listar(YearMonth competencia, CategoriaReceita categoria, UUID usuarioId) {
        return buscarReceitasPort.buscarPorFiltros(competencia, categoria, usuarioId);
    }

    @Override
    public Receita editar(UUID id, Receita receita, UUID usuarioId) {
        Receita existente = buscarPorIdEValidarDono(id, usuarioId);
        Receita receitaAtualizada = new Receita(id, receita.descricao(), receita.valor(),
                receita.competencia(), receita.categoria(), existente.usuarioId());
        return salvarReceitaPort.salvar(receitaAtualizada);
    }

    @Override
    public void remover(UUID id, UUID usuarioId) {
        buscarPorIdEValidarDono(id, usuarioId);
        removerReceitaPort.remover(id);
    }

    private Receita buscarPorIdEValidarDono(UUID id, UUID usuarioId) {
        Receita existente = buscarReceitasPort.buscarPorId(id)
                .orElseThrow(() -> new ReceitaNaoEncontradaException(id));
        if (!existente.usuarioId().equals(usuarioId)) {
            throw new ReceitaNaoEncontradaException(id);
        }
        return existente;
    }
}
