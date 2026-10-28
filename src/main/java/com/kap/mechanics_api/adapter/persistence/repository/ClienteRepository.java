package com.kap.mechanics_api.adapter.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kap.mechanics_api.adapter.persistence.entity.ClienteJpaEntity;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<ClienteJpaEntity, Integer> {
    Optional<ClienteJpaEntity> findByCpfCnpj(String cpfCnpj);
}
