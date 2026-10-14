package br.com.rpx.pactumapi.adapter.out.persistence.repository;

import br.com.rpx.pactumapi.adapter.out.persistence.entity.MetaEconomiaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MetaEconomiaJpaRepository extends JpaRepository<MetaEconomiaJpaEntity, UUID> {
    Optional<MetaEconomiaJpaEntity> findByCompetenciaAndUsuarioId(LocalDate competencia, UUID usuarioId);
    List<MetaEconomiaJpaEntity> findByUsuarioIdAndCompetenciaBetweenOrderByCompetencia(
            UUID usuarioId, LocalDate inicio, LocalDate fim);
}
