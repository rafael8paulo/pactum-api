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
    public Despesa cadastrar(Despesa despesa, UUID usuarioId) {
        Despesa comUsuario = new Despesa(null, despesa.descricao(), despesa.valor(),
                despesa.status(), despesa.competencia(), despesa.categoria(), usuarioId);
        return salvarPort.salvar(comUsuario);
    }

    @Override
    public List<Despesa> listar(YearMonth competencia, CategoriaDespesa categoria, StatusDespesa status, UUID usuarioId) {
        return buscarPort.buscarPorFiltros(competencia, categoria, status, usuarioId);
    }

    @Override
    public Despesa atualizar(UUID id, StatusDespesa status, UUID usuarioId) {
        Despesa existente = buscarPorIdEValidarDono(id, usuarioId);
        Despesa atualizada = new Despesa(existente.id(), existente.descricao(), existente.valor(),
                status, existente.competencia(), existente.categoria(), usuarioId);
        return salvarPort.salvar(atualizada);
    }

    @Override
    public Despesa editar(UUID id, Despesa despesa, UUID usuarioId) {
        buscarPorIdEValidarDono(id, usuarioId);
        Despesa editada = new Despesa(id, despesa.descricao(), despesa.valor(),
                despesa.status(), despesa.competencia(), despesa.categoria(), usuarioId);
        return salvarPort.salvar(editada);
    }

    @Override
    public void remover(UUID id, UUID usuarioId) {
        buscarPorIdEValidarDono(id, usuarioId);
        removerPort.remover(id);
    }

    private Despesa buscarPorIdEValidarDono(UUID id, UUID usuarioId) {
        Despesa existente = buscarPort.buscarPorId(id)
                .orElseThrow(() -> new DespesaNaoEncontradaException(id));
        if (!existente.usuarioId().equals(usuarioId)) {
            throw new DespesaNaoEncontradaException(id);
        }
        return existente;
    }
}
