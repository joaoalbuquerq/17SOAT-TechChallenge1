package com.kap.mechanics_api.adapter.persistence.repository;

import com.kap.mechanics_api.adapter.persistence.entity.OrcamentoServicoJpaEntity;
import com.kap.mechanics_api.adapter.persistence.entity.OrcamentoServicoJpaId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrcamentoServicoRepository extends JpaRepository<OrcamentoServicoJpaEntity, OrcamentoServicoJpaId> {

    List<OrcamentoServicoJpaEntity> findByOrcamento_Id(Integer orcamentoId);
}
