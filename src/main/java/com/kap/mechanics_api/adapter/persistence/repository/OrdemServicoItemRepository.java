package com.kap.mechanics_api.adapter.persistence.repository;
import com.kap.mechanics_api.adapter.persistence.entity.OrdemServicoItemJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface OrdemServicoItemRepository extends JpaRepository<OrdemServicoItemJpaEntity, Integer> { List<OrdemServicoItemJpaEntity> findByOrdemServico_Id(Integer ordemServicoId); }
