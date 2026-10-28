package com.kap.mechanics_api.adapter.persistence.repository;

import com.kap.mechanics_api.adapter.persistence.entity.VeiculoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VeiculoRepository extends JpaRepository<VeiculoJpaEntity, Integer> {
}
