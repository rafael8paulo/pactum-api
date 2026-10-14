package br.com.rpx.pactumapi.domain.service;

import br.com.rpx.pactumapi.config.UseCase;
import br.com.rpx.pactumapi.domain.exception.ContaRecorrenteNaoEncontradaException;
import br.com.rpx.pactumapi.domain.model.ContaRecorrente;
import br.com.rpx.pactumapi.domain.model.StatusContaRecorrente;
import br.com.rpx.pactumapi.domain.port.in.AtualizarStatusContaRecorrenteUseCase;
import br.com.rpx.pactumapi.domain.port.in.CadastrarContaRecorrenteUseCase;
import br.com.rpx.pactumapi.domain.port.in.EditarContaRecorrenteUseCase;
import br.com.rpx.pactumapi.domain.port.in.ListarContasRecorrentesUseCase;
import br.com.rpx.pactumapi.domain.port.in.RemoverContaRecorrenteUseCase;
import br.com.rpx.pactumapi.domain.port.out.BuscarContasRecorrentesPort;
import br.com.rpx.pactumapi.domain.port.out.BuscarFormasPagamentoPort;
import br.com.rpx.pactumapi.domain.port.out.BuscarHistoricoValorPort;
import br.com.rpx.pactumapi.domain.port.out.RemoverContaRecorrentePort;
import br.com.rpx.pactumapi.domain.port.out.SalvarContaRecorrentePort;
import br.com.rpx.pactumapi.domain.port.out.SalvarHistoricoValorPort;

import java.util.List;
import java.util.UUID;

@UseCase
public class ContaRecorrenteService implements
        CadastrarContaRecorrenteUseCase,
        ListarContasRecorrentesUseCase,
        EditarContaRecorrenteUseCase,
        AtualizarStatusContaRecorrenteUseCase,
        RemoverContaRecorrenteUseCase {

    private final SalvarContaRecorrentePort salvarPort;
    private final BuscarContasRecorrentesPort buscarPort;
    private final RemoverContaRecorrentePort removerPort;
    private final SalvarHistoricoValorPort salvarHistoricoValorPort;
    private final BuscarHistoricoValorPort buscarHistoricoValorPort;
    private final BuscarFormasPagamentoPort buscarFormasPagamentoPort;

    public ContaRecorrenteService(SalvarContaRecorrentePort salvarPort,
                                  BuscarContasRecorrentesPort buscarPort,
                                  RemoverContaRecorrentePort removerPort,
                                  SalvarHistoricoValorPort salvarHistoricoValorPort,
                                  BuscarHistoricoValorPort buscarHistoricoValorPort,
                                  BuscarFormasPagamentoPort buscarFormasPagamentoPort) {
        this.salvarPort = salvarPort;
        this.buscarPort = buscarPort;
        this.removerPort = removerPort;
        this.salvarHistoricoValorPort = salvarHistoricoValorPort;
        this.buscarHistoricoValorPort = buscarHistoricoValorPort;
        this.buscarFormasPagamentoPort = buscarFormasPagamentoPort;
    }

    @Override
    public ContaRecorrente cadastrar(ContaRecorrente contaRecorrente, UUID usuarioId) {
        validarFormaPagamento(contaRecorrente.formaPagamentoId(), usuarioId);
        ContaRecorrente comUsuario = new ContaRecorrente(null, contaRecorrente.descricao(), contaRecorrente.valorPadrao(),
                contaRecorrente.categoria(), contaRecorrente.diaVencimento(), contaRecorrente.competenciaInicio(),
                contaRecorrente.competenciaFim(), StatusContaRecorrente.ATIVA, usuarioId,
                contaRecorrente.frequencia(), contaRecorrente.formaPagamentoId(), contaRecorrente.dataBaseCobranca());
        ContaRecorrente salva = salvarPort.salvar(comUsuario);
        HistoricoValorRecorder.registrarCadastro(salva, salvarHistoricoValorPort);
        return salva;
    }

    @Override
    public List<ContaRecorrente> listar(StatusContaRecorrente status, UUID usuarioId) {
        return buscarPort.buscarPorFiltros(status, usuarioId);
    }

    @Override
    public ContaRecorrente editar(UUID id, ContaRecorrente contaRecorrente, UUID usuarioId) {
        ContaRecorrente existente = buscarPorIdEValidarDono(id, usuarioId);
        validarFormaPagamento(contaRecorrente.formaPagamentoId(), usuarioId);
        ContaRecorrente editada = new ContaRecorrente(id, contaRecorrente.descricao(), contaRecorrente.valorPadrao(),
                contaRecorrente.categoria(), contaRecorrente.diaVencimento(), contaRecorrente.competenciaInicio(),
                contaRecorrente.competenciaFim(), existente.status(), usuarioId,
                contaRecorrente.frequencia(), contaRecorrente.formaPagamentoId(), contaRecorrente.dataBaseCobranca());
        ContaRecorrente salva = salvarPort.salvar(editada);
        HistoricoValorRecorder.registrarReajusteSeNecessario(existente, salva, buscarHistoricoValorPort, salvarHistoricoValorPort);
        return salva;
    }

    @Override
    public ContaRecorrente atualizar(UUID id, StatusContaRecorrente status, UUID usuarioId) {
        ContaRecorrente existente = buscarPorIdEValidarDono(id, usuarioId);
        ContaRecorrente atualizada = new ContaRecorrente(existente.id(), existente.descricao(), existente.valorPadrao(),
                existente.categoria(), existente.diaVencimento(), existente.competenciaInicio(),
                existente.competenciaFim(), status, usuarioId,
                existente.frequencia(), existente.formaPagamentoId(), existente.dataBaseCobranca());
        return salvarPort.salvar(atualizada);
    }

    @Override
    public void remover(UUID id, UUID usuarioId) {
        buscarPorIdEValidarDono(id, usuarioId);
        removerPort.remover(id);
    }

    private void validarFormaPagamento(UUID formaPagamentoId, UUID usuarioId) {
        if (formaPagamentoId == null) {
            return;
        }
        boolean valida = buscarFormasPagamentoPort.buscarPorId(formaPagamentoId)
                .filter(forma -> forma.usuarioId().equals(usuarioId))
                .isPresent();
        if (!valida) {
            throw new IllegalArgumentException("formaPagamentoId inválido: " + formaPagamentoId);
        }
    }

    private ContaRecorrente buscarPorIdEValidarDono(UUID id, UUID usuarioId) {
        ContaRecorrente existente = buscarPort.buscarPorId(id)
                .orElseThrow(() -> new ContaRecorrenteNaoEncontradaException(id));
        if (!existente.usuarioId().equals(usuarioId)) {
            throw new ContaRecorrenteNaoEncontradaException(id);
        }
        return existente;
    }
}
