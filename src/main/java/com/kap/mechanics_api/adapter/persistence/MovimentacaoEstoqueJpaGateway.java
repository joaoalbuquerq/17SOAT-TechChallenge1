package com.kap.mechanics_api.adapter.persistence;

import com.kap.mechanics_api.adapter.persistence.entity.MovimentacaoEstoqueJpaEntity;
import com.kap.mechanics_api.core.movimentacaoestoque.domain.MovimentacaoEstoque;
import com.kap.mechanics_api.core.movimentacaoestoque.domain.TipoMovimentacaoEstoque;
import com.kap.mechanics_api.core.movimentacaoestoque.gateway.MovimentacaoEstoqueGateway;
import com.kap.mechanics_api.adapter.persistence.repository.ItemEstoqueRepository;
import com.kap.mechanics_api.adapter.persistence.repository.MovimentacaoEstoqueRepository;
import com.kap.mechanics_api.adapter.persistence.repository.OrdemServicoRepository;
import com.kap.mechanics_api.adapter.persistence.repository.UsuarioRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class MovimentacaoEstoqueJpaGateway implements MovimentacaoEstoqueGateway {

    private final MovimentacaoEstoqueJpaMapper mapper;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;
    private final ItemEstoqueRepository itemEstoqueRepository;
    private final UsuarioRepository usuarioRepository;
    private final OrdemServicoRepository ordemServicoRepository;

    public MovimentacaoEstoqueJpaGateway(MovimentacaoEstoqueJpaMapper mapper,
                                         MovimentacaoEstoqueRepository movimentacaoRepository,
                                         ItemEstoqueRepository itemEstoqueRepository,
                                         UsuarioRepository usuarioRepository,
                                         OrdemServicoRepository ordemServicoRepository) {
        this.mapper = mapper;
        this.movimentacaoRepository = movimentacaoRepository;
        this.itemEstoqueRepository = itemEstoqueRepository;
        this.usuarioRepository = usuarioRepository;
        this.ordemServicoRepository = ordemServicoRepository;
    }

    @Override
    public MovimentacaoEstoque salvar(MovimentacaoEstoque movimentacao) {
        var entity = mapper.paraEntity(
                movimentacao,
                itemEstoqueRepository.getReferenceById(movimentacao.getItemEstoqueId()),
                usuarioRepository.getReferenceById(movimentacao.getUsuarioId()),
                movimentacao.getOrdemServicoId() == null
                        ? null : ordemServicoRepository.getReferenceById(movimentacao.getOrdemServicoId()));
        var salva = movimentacaoRepository.save(entity);

        // reconstrói a partir do que o domínio já sabe, sem tocar nas associações (proxies)
        return MovimentacaoEstoque.reconstruir(salva.getId(), movimentacao.getItemEstoqueId(), movimentacao.getTipo(),
                movimentacao.getQuantidade(), movimentacao.getDataHora(), movimentacao.getUsuarioId(),
                movimentacao.getOrdemServicoId());
    }

    @Override
    public Optional<MovimentacaoEstoque> pesquisarPorId(Integer id) {
        return movimentacaoRepository.findById(id).map(mapper::paraDominio);
    }

    @Override
    public List<MovimentacaoEstoque> listarTodas() {
        return paraDominio(movimentacaoRepository.findAllByOrderByDataHoraDesc());
    }

    @Override
    public List<MovimentacaoEstoque> listarPorItem(Integer itemEstoqueId) {
        return paraDominio(movimentacaoRepository.findByItemEstoque_IdOrderByDataHoraDesc(itemEstoqueId));
    }

    @Override
    public List<MovimentacaoEstoque> listarPorOrdemServico(Integer ordemServicoId) {
        return paraDominio(movimentacaoRepository.findByOrdemServico_IdOrderByDataHoraDesc(ordemServicoId));
    }

    @Override
    public List<MovimentacaoEstoque> listarPorTipo(TipoMovimentacaoEstoque tipo) {
        return paraDominio(movimentacaoRepository.findByTipoOrderByDataHoraDesc(tipo));
    }

    @Override
    public List<MovimentacaoEstoque> listarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return paraDominio(movimentacaoRepository.findByDataHoraBetweenOrderByDataHoraDesc(inicio, fim));
    }

    @Override
    public List<MovimentacaoEstoque> listarHistoricoDoItem(Integer itemEstoqueId) {
        return paraDominio(movimentacaoRepository.findByItemEstoque_IdOrderByDataHoraAscIdAsc(itemEstoqueId));
    }

    private List<MovimentacaoEstoque> paraDominio(List<MovimentacaoEstoqueJpaEntity> entities) {
        return entities.stream().map(mapper::paraDominio).toList();
    }
}
