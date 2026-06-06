package br.com.rpx.pactumapi.adapter.out.persistence;

import br.com.rpx.pactumapi.adapter.out.persistence.repository.PatrimonioJpaRepository;
import br.com.rpx.pactumapi.domain.model.Patrimonio;
import br.com.rpx.pactumapi.domain.port.out.BuscarPatrimonioPort;
import br.com.rpx.pactumapi.domain.port.out.SalvarPatrimonioPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PatrimonioPersistenceAdapter implements SalvarPatrimonioPort, BuscarPatrimonioPort {

    private final PatrimonioJpaRepository repository;

    @Override
    public Patrimonio salvar(Patrimonio patrimonio) {
        var entity = PatrimonioPersistenceMapper.toEntity(patrimonio);
        return PatrimonioPersistenceMapper.toDomain(repository.save(entity));
    }

    @Override
    public List<Patrimonio> buscarPorCompetencia(YearMonth competencia) {
        return repository.findByCompetencia(competencia.atDay(1))
                .stream().map(PatrimonioPersistenceMapper::toDomain).toList();
    }
}
