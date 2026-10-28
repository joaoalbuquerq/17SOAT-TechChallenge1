package com.kap.mechanics_api.adapter.persistence.repository;

import com.kap.mechanics_api.adapter.persistence.entity.MovimentacaoEstoqueJpaEntity;
import com.kap.mechanics_api.core.movimentacaoestoque.domain.TipoMovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoqueJpaEntity, Integer> {

    List<MovimentacaoEstoqueJpaEntity> findAllByOrderByDataHoraDesc();

    List<MovimentacaoEstoqueJpaEntity> findByItemEstoque_IdOrderByDataHoraDesc(Integer itemEstoqueId);

    List<MovimentacaoEstoqueJpaEntity> findByOrdemServico_IdOrderByDataHoraDesc(Integer ordemServicoId);

    List<MovimentacaoEstoqueJpaEntity> findByTipoOrderByDataHoraDesc(TipoMovimentacaoEstoque tipo);

    List<MovimentacaoEstoqueJpaEntity> findByDataHoraBetweenOrderByDataHoraDesc(LocalDateTime inicio, LocalDateTime fim);

    List<MovimentacaoEstoqueJpaEntity> findByItemEstoque_IdOrderByDataHoraAscIdAsc(Integer itemEstoqueId);
}
