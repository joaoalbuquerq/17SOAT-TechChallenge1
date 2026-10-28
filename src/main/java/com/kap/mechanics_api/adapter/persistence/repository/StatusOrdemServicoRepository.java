package com.kap.mechanics_api.adapter.persistence.repository;

import com.kap.mechanics_api.adapter.persistence.entity.StatusOrdemServicoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StatusOrdemServicoRepository extends JpaRepository<StatusOrdemServicoJpaEntity, Integer> {
    Optional<StatusOrdemServicoJpaEntity> findByNome(String nome);
}
