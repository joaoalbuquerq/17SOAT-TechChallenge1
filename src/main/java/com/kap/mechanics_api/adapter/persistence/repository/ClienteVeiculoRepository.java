package com.kap.mechanics_api.adapter.persistence.repository;

import com.kap.mechanics_api.adapter.persistence.entity.ClienteVeiculoJpaEntity;
import com.kap.mechanics_api.adapter.persistence.entity.ClienteVeiculoJpaId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteVeiculoRepository extends JpaRepository<ClienteVeiculoJpaEntity, ClienteVeiculoJpaId> {
}
