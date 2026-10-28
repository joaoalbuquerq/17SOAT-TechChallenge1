package com.kap.mechanics_api.adapter.persistence.repository;

import com.kap.mechanics_api.adapter.persistence.entity.ServicoItemJpaEntity;
import com.kap.mechanics_api.adapter.persistence.entity.ServicoItemJpaId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServicoItemRepository extends JpaRepository<ServicoItemJpaEntity, ServicoItemJpaId> {

    List<ServicoItemJpaEntity> findByServico_Id(Integer servicoId);
}