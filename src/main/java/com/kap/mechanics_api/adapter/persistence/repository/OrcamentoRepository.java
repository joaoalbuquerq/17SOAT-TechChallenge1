package com.kap.mechanics_api.adapter.persistence.repository;

import com.kap.mechanics_api.adapter.persistence.entity.OrcamentoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrcamentoRepository extends JpaRepository<OrcamentoJpaEntity, Integer> {
}
