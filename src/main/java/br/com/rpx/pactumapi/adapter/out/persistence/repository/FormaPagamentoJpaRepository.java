package br.com.rpx.pactumapi.adapter.out.persistence.repository;

import br.com.rpx.pactumapi.adapter.out.persistence.entity.FormaPagamentoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FormaPagamentoJpaRepository extends JpaRepository<FormaPagamentoJpaEntity, UUID> {
    List<FormaPagamentoJpaEntity> findByUsuarioId(UUID usuarioId);
}
