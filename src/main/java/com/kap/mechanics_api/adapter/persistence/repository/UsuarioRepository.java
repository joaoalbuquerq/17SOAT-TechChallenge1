package com.kap.mechanics_api.adapter.persistence.repository;

import com.kap.mechanics_api.adapter.persistence.entity.UsuarioJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<UsuarioJpaEntity, Integer> {

    Optional<UsuarioJpaEntity> findByLogin(String login);

    boolean existsByLogin(String login);
}
