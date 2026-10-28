package com.kap.mechanics_api.adapter.persistence.repository;
import com.kap.mechanics_api.adapter.persistence.entity.OrcamentoItemJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface OrcamentoItemRepository extends JpaRepository<OrcamentoItemJpaEntity, Integer> { List<OrcamentoItemJpaEntity> findByOrcamento_Id(Integer orcamentoId); }
