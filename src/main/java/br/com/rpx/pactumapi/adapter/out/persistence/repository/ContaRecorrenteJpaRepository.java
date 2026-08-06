package br.com.rpx.pactumapi.adapter.out.persistence.repository;

import br.com.rpx.pactumapi.adapter.out.persistence.entity.ContaRecorrenteJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ContaRecorrenteJpaRepository extends JpaRepository<ContaRecorrenteJpaEntity, UUID> {

    @Query("""
            SELECT c FROM ContaRecorrenteJpaEntity c
            WHERE c.usuarioId = :usuarioId
              AND (:status IS NULL OR c.status = :status)
            """)
    List<ContaRecorrenteJpaEntity> findByFiltros(
            @Param("status") String status,
            @Param("usuarioId") UUID usuarioId
    );
}
