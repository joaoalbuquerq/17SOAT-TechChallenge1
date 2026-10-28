package com.kap.mechanics_api.adapter.persistence.repository;

import com.kap.mechanics_api.adapter.persistence.entity.ServicoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServicoRepository extends JpaRepository<ServicoJpaEntity, Integer> {
}
