package br.com.rpx.pactumapi.adapter.out.persistence.repository;

import br.com.rpx.pactumapi.adapter.out.persistence.entity.DespesaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface DespesaJpaRepository extends JpaRepository<DespesaJpaEntity, UUID> {

    @Query("""
            SELECT d FROM DespesaJpaEntity d
            WHERE d.competencia = :competencia
              AND (:categoria IS NULL OR d.categoria = :categoria)
              AND (:status IS NULL OR d.status = :status)
            """)
    List<DespesaJpaEntity> findByFiltros(
            @Param("competencia") LocalDate competencia,
            @Param("categoria") String categoria,
            @Param("status") String status
    );
}
