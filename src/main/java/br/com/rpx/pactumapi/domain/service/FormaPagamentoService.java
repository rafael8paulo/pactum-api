package br.com.rpx.pactumapi.domain.service;

import br.com.rpx.pactumapi.config.UseCase;
import br.com.rpx.pactumapi.domain.exception.FormaPagamentoNaoEncontradaException;
import br.com.rpx.pactumapi.domain.model.FormaPagamento;
import br.com.rpx.pactumapi.domain.port.in.CadastrarFormaPagamentoUseCase;
import br.com.rpx.pactumapi.domain.port.in.EditarFormaPagamentoUseCase;
import br.com.rpx.pactumapi.domain.port.in.ListarFormasPagamentoUseCase;
import br.com.rpx.pactumapi.domain.port.in.RemoverFormaPagamentoUseCase;
import br.com.rpx.pactumapi.domain.port.out.BuscarFormasPagamentoPort;
import br.com.rpx.pactumapi.domain.port.out.RemoverFormaPagamentoPort;
import br.com.rpx.pactumapi.domain.port.out.SalvarFormaPagamentoPort;

import java.util.List;
import java.util.UUID;

@UseCase
public class FormaPagamentoService implements
        CadastrarFormaPagamentoUseCase,
        ListarFormasPagamentoUseCase,
        EditarFormaPagamentoUseCase,
        RemoverFormaPagamentoUseCase {

    private final SalvarFormaPagamentoPort salvarPort;
    private final BuscarFormasPagamentoPort buscarPort;
    private final RemoverFormaPagamentoPort removerPort;

    public FormaPagamentoService(SalvarFormaPagamentoPort salvarPort,
                                  BuscarFormasPagamentoPort buscarPort,
                                  RemoverFormaPagamentoPort removerPort) {
        this.salvarPort = salvarPort;
        this.buscarPort = buscarPort;
        this.removerPort = removerPort;
    }

    @Override
    public FormaPagamento cadastrar(FormaPagamento formaPagamento, UUID usuarioId) {
        FormaPagamento comUsuario = new FormaPagamento(null, formaPagamento.nome(), formaPagamento.tipo(),
                formaPagamento.diaFechamentoFatura(), usuarioId);
        return salvarPort.salvar(comUsuario);
    }

    @Override
    public List<FormaPagamento> listar(UUID usuarioId) {
        return buscarPort.listarPorUsuario(usuarioId);
    }

    @Override
    public FormaPagamento editar(UUID id, FormaPagamento formaPagamento, UUID usuarioId) {
        buscarPorIdEValidarDono(id, usuarioId);
        FormaPagamento editada = new FormaPagamento(id, formaPagamento.nome(), formaPagamento.tipo(),
                formaPagamento.diaFechamentoFatura(), usuarioId);
        return salvarPort.salvar(editada);
    }

    @Override
    public void remover(UUID id, UUID usuarioId) {
        buscarPorIdEValidarDono(id, usuarioId);
        removerPort.remover(id);
    }

    private FormaPagamento buscarPorIdEValidarDono(UUID id, UUID usuarioId) {
        FormaPagamento existente = buscarPort.buscarPorId(id)
                .orElseThrow(() -> new FormaPagamentoNaoEncontradaException(id));
        if (!existente.usuarioId().equals(usuarioId)) {
            throw new FormaPagamentoNaoEncontradaException(id);
        }
        return existente;
    }
}
