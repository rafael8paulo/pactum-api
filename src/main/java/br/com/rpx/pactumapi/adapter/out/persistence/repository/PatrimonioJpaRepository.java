package br.com.rpx.pactumapi.adapter.out.persistence.repository;

import br.com.rpx.pactumapi.adapter.out.persistence.entity.PatrimonioJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PatrimonioJpaRepository extends JpaRepository<PatrimonioJpaEntity, UUID> {
    List<PatrimonioJpaEntity> findByCompetencia(LocalDate competencia);
}
