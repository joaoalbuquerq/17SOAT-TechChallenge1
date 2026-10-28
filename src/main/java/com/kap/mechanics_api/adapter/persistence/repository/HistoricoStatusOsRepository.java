package com.kap.mechanics_api.adapter.persistence.repository;

import com.kap.mechanics_api.adapter.persistence.entity.HistoricoStatusOsJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HistoricoStatusOsRepository extends JpaRepository<HistoricoStatusOsJpaEntity, Long> {

    Optional<HistoricoStatusOsJpaEntity> findByOrdemServico_IdAndDataHoraFimIsNull(Integer ordemServicoId);

    List<HistoricoStatusOsJpaEntity> findByOrdemServico_IdOrderByDataHoraInicioAsc(Integer ordemServicoId);
}
