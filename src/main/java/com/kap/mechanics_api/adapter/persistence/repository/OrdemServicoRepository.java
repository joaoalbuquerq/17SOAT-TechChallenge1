package com.kap.mechanics_api.adapter.persistence.repository;

import com.kap.mechanics_api.adapter.persistence.entity.OrdemServicoJpaEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrdemServicoRepository extends JpaRepository<OrdemServicoJpaEntity, Integer> {

    boolean existsByOrcamentoId(Integer orcamentoId);

    Optional<OrdemServicoJpaEntity> findByOrcamento_Id(Integer orcamentoId);

    @EntityGraph(attributePaths = {"orcamento", "statusOrdemServico"})
    List<OrdemServicoJpaEntity> findByOrcamento_Cliente_IdOrderByDataAberturaDesc(Integer clienteId);
}
