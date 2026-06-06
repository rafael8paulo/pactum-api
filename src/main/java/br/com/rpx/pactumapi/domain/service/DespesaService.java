package br.com.rpx.pactumapi.domain.service;

import br.com.rpx.pactumapi.config.UseCase;
import br.com.rpx.pactumapi.domain.exception.DespesaNaoEncontradaException;
import br.com.rpx.pactumapi.domain.model.CategoriaDespesa;
import br.com.rpx.pactumapi.domain.model.Despesa;
import br.com.rpx.pactumapi.domain.model.StatusDespesa;
import br.com.rpx.pactumapi.domain.port.in.AtualizarStatusDespesaUseCase;
import br.com.rpx.pactumapi.domain.port.in.CadastrarDespesaUseCase;
import br.com.rpx.pactumapi.domain.port.in.EditarDespesaUseCase;
import br.com.rpx.pactumapi.domain.port.in.ListarDespesasUseCase;
import br.com.rpx.pactumapi.domain.port.in.RemoverDespesaUseCase;
import br.com.rpx.pactumapi.domain.port.out.BuscarDespesasPort;
import br.com.rpx.pactumapi.domain.port.out.RemoverDespesaPort;
import br.com.rpx.pactumapi.domain.port.out.SalvarDespesaPort;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@UseCase
public class DespesaService implements
        CadastrarDespesaUseCase,
        ListarDespesasUseCase,
        AtualizarStatusDespesaUseCase,
        EditarDespesaUseCase,
        RemoverDespesaUseCase {

    private final SalvarDespesaPort salvarPort;
    private final BuscarDespesasPort buscarPort;
    private final RemoverDespesaPort removerPort;

    public DespesaService(SalvarDespesaPort salvarPort,
                          BuscarDespesasPort buscarPort,
                          RemoverDespesaPort removerPort) {
        this.salvarPort = salvarPort;
        this.buscarPort = buscarPort;
        this.removerPort = removerPort;
    }

    @Override
    public Despesa cadastrar(Despesa despesa) {
        return salvarPort.salvar(despesa);
    }

    @Override
    public List<Despesa> listar(YearMonth competencia, CategoriaDespesa categoria, StatusDespesa status) {
        return buscarPort.buscarPorFiltros(competencia, categoria, status);
    }

    @Override
    public Despesa atualizar(UUID id, StatusDespesa status) {
        Despesa existente = buscarPort.buscarPorId(id)
                .orElseThrow(() -> new DespesaNaoEncontradaException(id));
        Despesa atualizada = new Despesa(
                existente.id(),
                existente.descricao(),
                existente.valor(),
                status,
                existente.competencia(),
                existente.categoria()
        );
        return salvarPort.salvar(atualizada);
    }

    @Override
    public Despesa editar(UUID id, Despesa despesa) {
        buscarPort.buscarPorId(id)
                .orElseThrow(() -> new DespesaNaoEncontradaException(id));
        Despesa editada = new Despesa(
                id,
                despesa.descricao(),
                despesa.valor(),
                despesa.status(),
                despesa.competencia(),
                despesa.categoria()
        );
        return salvarPort.salvar(editada);
    }

    @Override
    public void remover(UUID id) {
        buscarPort.buscarPorId(id)
                .orElseThrow(() -> new DespesaNaoEncontradaException(id));
        removerPort.remover(id);
    }
}
