package br.com.rpx.pactumapi.adapter.out.persistence.repository;

import br.com.rpx.pactumapi.adapter.out.persistence.entity.ReceitaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ReceitaJpaRepository extends JpaRepository<ReceitaJpaEntity, UUID> {

    @Query("""
            SELECT r FROM ReceitaJpaEntity r
            WHERE r.competencia = :competencia
              AND (:categoria IS NULL OR r.categoria = :categoria)
            """)
    List<ReceitaJpaEntity> findByFiltros(
            @Param("competencia") LocalDate competencia,
            @Param("categoria") String categoria
    );
}
