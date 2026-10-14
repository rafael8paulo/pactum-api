package br.com.rpx.pactumapi.adapter.out.persistence.repository;

import br.com.rpx.pactumapi.adapter.out.persistence.entity.HistoricoValorContaRecorrenteJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HistoricoValorContaRecorrenteJpaRepository extends JpaRepository<HistoricoValorContaRecorrenteJpaEntity, UUID> {
    Optional<HistoricoValorContaRecorrenteJpaEntity> findByContaRecorrenteIdAndVigenteAteIsNull(UUID contaRecorrenteId);
    List<HistoricoValorContaRecorrenteJpaEntity> findByContaRecorrenteIdOrderByVigenteDesdeDesc(UUID contaRecorrenteId);
}
