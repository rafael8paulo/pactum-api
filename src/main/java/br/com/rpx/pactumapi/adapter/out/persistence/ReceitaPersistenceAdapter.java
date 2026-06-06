package br.com.rpx.pactumapi.adapter.out.persistence;

import br.com.rpx.pactumapi.adapter.out.persistence.repository.ReceitaJpaRepository;
import br.com.rpx.pactumapi.domain.model.CategoriaReceita;
import br.com.rpx.pactumapi.domain.model.Receita;
import br.com.rpx.pactumapi.domain.port.out.BuscarReceitasPort;
import br.com.rpx.pactumapi.domain.port.out.RemoverReceitaPort;
import br.com.rpx.pactumapi.domain.port.out.SalvarReceitaPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ReceitaPersistenceAdapter implements SalvarReceitaPort, BuscarReceitasPort, RemoverReceitaPort {

    private final ReceitaJpaRepository repository;

    @Override
    public Receita salvar(Receita receita) {
        var entity = ReceitaPersistenceMapper.toEntity(receita);
        return ReceitaPersistenceMapper.toDomain(repository.save(entity));
    }

    @Override
    public Optional<Receita> buscarPorId(UUID id) {
        return repository.findById(id).map(ReceitaPersistenceMapper::toDomain);
    }

    @Override
    public List<Receita> buscarPorFiltros(YearMonth competencia, CategoriaReceita categoria) {
        return repository.findByFiltros(
                competencia.atDay(1),
                categoria != null ? categoria.name() : null
        ).stream().map(ReceitaPersistenceMapper::toDomain).toList();
    }

    @Override
    public void remover(UUID id) {
        repository.deleteById(id);
    }
}
