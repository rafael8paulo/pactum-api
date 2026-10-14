package br.com.rpx.pactumapi.adapter.out.persistence;

import br.com.rpx.pactumapi.adapter.out.persistence.repository.FormaPagamentoJpaRepository;
import br.com.rpx.pactumapi.domain.model.FormaPagamento;
import br.com.rpx.pactumapi.domain.port.out.BuscarFormasPagamentoPort;
import br.com.rpx.pactumapi.domain.port.out.RemoverFormaPagamentoPort;
import br.com.rpx.pactumapi.domain.port.out.SalvarFormaPagamentoPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class FormaPagamentoPersistenceAdapter implements
        SalvarFormaPagamentoPort, BuscarFormasPagamentoPort, RemoverFormaPagamentoPort {

    private final FormaPagamentoJpaRepository repository;

    @Override
    public FormaPagamento salvar(FormaPagamento formaPagamento) {
        var entity = FormaPagamentoPersistenceMapper.toEntity(formaPagamento);
        return FormaPagamentoPersistenceMapper.toDomain(repository.save(entity));
    }

    @Override
    public Optional<FormaPagamento> buscarPorId(UUID id) {
        return repository.findById(id).map(FormaPagamentoPersistenceMapper::toDomain);
    }

    @Override
    public List<FormaPagamento> listarPorUsuario(UUID usuarioId) {
        return repository.findByUsuarioId(usuarioId).stream()
                .map(FormaPagamentoPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public void remover(UUID id) {
        repository.deleteById(id);
    }
}
