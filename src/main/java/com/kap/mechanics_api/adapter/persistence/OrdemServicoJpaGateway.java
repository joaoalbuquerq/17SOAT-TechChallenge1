package com.kap.mechanics_api.adapter.persistence;

import com.kap.mechanics_api.adapter.persistence.entity.HistoricoStatusOsJpaEntity;
import com.kap.mechanics_api.adapter.persistence.entity.OrdemServicoItemJpaEntity;
import com.kap.mechanics_api.adapter.persistence.entity.OrdemServicoJpaEntity;
import com.kap.mechanics_api.adapter.persistence.entity.StatusOrdemServicoJpaEntity;
import com.kap.mechanics_api.core.ordemservico.domain.HistoricoDeStatus;
import com.kap.mechanics_api.core.ordemservico.domain.ItemOrdemServico;
import com.kap.mechanics_api.core.ordemservico.domain.OrdemServico;
import com.kap.mechanics_api.core.ordemservico.domain.OrdemServicoResumo;
import com.kap.mechanics_api.core.ordemservico.domain.StatusOrdemServico;
import com.kap.mechanics_api.core.ordemservico.gateway.OrdemServicoGateway;
import com.kap.mechanics_api.adapter.persistence.repository.HistoricoStatusOsRepository;
import com.kap.mechanics_api.adapter.persistence.repository.ItemEstoqueRepository;
import com.kap.mechanics_api.adapter.persistence.repository.OrcamentoItemRepository;
import com.kap.mechanics_api.adapter.persistence.repository.OrcamentoRepository;
import com.kap.mechanics_api.adapter.persistence.repository.OrdemServicoItemRepository;
import com.kap.mechanics_api.adapter.persistence.repository.OrdemServicoRepository;
import com.kap.mechanics_api.adapter.persistence.repository.ServicoRepository;
import com.kap.mechanics_api.adapter.persistence.repository.StatusOrdemServicoRepository;
import com.kap.mechanics_api.repository.UsuarioRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class OrdemServicoJpaGateway implements OrdemServicoGateway {

    private final OrdemServicoJpaMapper mapper;
    private final OrdemServicoRepository ordemServicoRepository;
    private final StatusOrdemServicoRepository statusRepository;
    private final HistoricoStatusOsRepository historicoRepository;
    private final OrdemServicoItemRepository itemRepository;
    private final OrcamentoRepository orcamentoRepository;
    private final OrcamentoItemRepository orcamentoItemRepository;
    private final UsuarioRepository usuarioRepository;
    private final ServicoRepository servicoRepository;
    private final ItemEstoqueRepository itemEstoqueRepository;

    public OrdemServicoJpaGateway(OrdemServicoJpaMapper mapper, OrdemServicoRepository ordemServicoRepository,
                                  StatusOrdemServicoRepository statusRepository,
                                  HistoricoStatusOsRepository historicoRepository,
                                  OrdemServicoItemRepository itemRepository, OrcamentoRepository orcamentoRepository,
                                  OrcamentoItemRepository orcamentoItemRepository,
                                  UsuarioRepository usuarioRepository, ServicoRepository servicoRepository,
                                  ItemEstoqueRepository itemEstoqueRepository) {
        this.mapper = mapper;
        this.ordemServicoRepository = ordemServicoRepository;
        this.statusRepository = statusRepository;
        this.historicoRepository = historicoRepository;
        this.itemRepository = itemRepository;
        this.orcamentoRepository = orcamentoRepository;
        this.orcamentoItemRepository = orcamentoItemRepository;
        this.usuarioRepository = usuarioRepository;
        this.servicoRepository = servicoRepository;
        this.itemEstoqueRepository = itemEstoqueRepository;
    }

    @Override
    public OrdemServico salvar(OrdemServico ordemServico) {
        OrdemServicoJpaEntity entity = ordemServico.getId() == null
                ? novaEntity(ordemServico)
                : ordemServicoRepository.findById(ordemServico.getId()).orElseThrow(
                        () -> new IllegalStateException("Ordem de serviço inexistente: " + ordemServico.getId()));
        entity.setStatusOrdemServico(buscarStatus(ordemServico.getStatus()));
        entity.setDataEntrega(ordemServico.getDataEntrega());
        OrdemServicoJpaEntity salva = ordemServicoRepository.saveAndFlush(entity);

        sincronizarHistorico(salva, ordemServico.getHistorico());
        for (ItemOrdemServico item : ordemServico.getItens()) {
            if (item.getId() == null) {
                itemRepository.save(novaLinha(salva, item));
            }
        }
        itemRepository.flush();
        return carregar(salva);
    }

    @Override
    public Optional<OrdemServico> pesquisarPorId(Integer id) {
        return ordemServicoRepository.findById(id).map(this::carregar);
    }

    @Override
    public Optional<OrdemServico> pesquisarPorOrcamentoId(Integer orcamentoId) {
        return ordemServicoRepository.findByOrcamento_Id(orcamentoId).map(this::carregar);
    }

    @Override
    public boolean existePorOrcamentoId(Integer orcamentoId) {
        return ordemServicoRepository.existsByOrcamentoId(orcamentoId);
    }

    @Override
    public List<OrdemServicoResumo> listarResumos() {
        return ordemServicoRepository.findAll().stream().map(mapper::paraResumo).toList();
    }

    @Override
    public List<OrdemServicoResumo> listarResumosPorCliente(Integer clienteId) {
        return ordemServicoRepository.findByOrcamento_Cliente_IdOrderByDataAberturaDesc(clienteId).stream()
                .map(mapper::paraResumo)
                .toList();
    }

    private OrdemServicoJpaEntity novaEntity(OrdemServico ordemServico) {
        var entity = new OrdemServicoJpaEntity();
        entity.setOrcamento(orcamentoRepository.getReferenceById(ordemServico.getOrcamentoId()));
        entity.setUsuarioAtendente(usuarioRepository.getReferenceById(ordemServico.getAtendenteId()));
        entity.setDataAbertura(ordemServico.getDataAbertura());
        return entity;
    }

    // Períodos já gravados só podem ser encerrados; os novos (sem id) são inseridos depois, para o
    // encerramento do anterior ir ao banco antes da abertura do seguinte.
    private void sincronizarHistorico(OrdemServicoJpaEntity ordem, List<HistoricoDeStatus> historico) {
        for (HistoricoDeStatus periodo : historico) {
            if (periodo.getId() != null && periodo.getFim() != null) {
                historicoRepository.findById(periodo.getId()).ifPresent(gravado -> {
                    if (gravado.getDataHoraFim() == null) {
                        gravado.setDataHoraFim(periodo.getFim());
                        historicoRepository.saveAndFlush(gravado);
                    }
                });
            }
        }
        for (HistoricoDeStatus periodo : historico) {
            if (periodo.getId() == null) {
                var novo = new HistoricoStatusOsJpaEntity(ordem, buscarStatus(periodo.getStatus()),
                        periodo.getInicio());
                novo.setDataHoraFim(periodo.getFim());
                historicoRepository.save(novo);
            }
        }
    }

    private OrdemServicoItemJpaEntity novaLinha(OrdemServicoJpaEntity ordem, ItemOrdemServico item) {
        var linha = new OrdemServicoItemJpaEntity();
        linha.setOrdemServico(ordem);
        if (item.getOrcamentoItemId() != null) {
            linha.setOrcamentoItem(orcamentoItemRepository.getReferenceById(item.getOrcamentoItemId()));
        }
        if (item.getServicoId() != null) {
            linha.setServico(servicoRepository.getReferenceById(item.getServicoId()));
        }
        if (item.getItemEstoqueId() != null) {
            linha.setItemEstoque(itemEstoqueRepository.getReferenceById(item.getItemEstoqueId()));
        }
        linha.setQuantidade(item.getQuantidade());
        linha.setValorUnitarioCobrado(item.getValorUnitario());
        return linha;
    }

    private StatusOrdemServicoJpaEntity buscarStatus(StatusOrdemServico status) {
        return statusRepository.findByNome(status.name())
                .orElseThrow(() -> new IllegalStateException("Status " + status + " não cadastrado"));
    }

    private OrdemServico carregar(OrdemServicoJpaEntity entity) {
        return mapper.paraDominio(entity,
                historicoRepository.findByOrdemServico_IdOrderByDataHoraInicioAsc(entity.getId()),
                itemRepository.findByOrdemServico_Id(entity.getId()));
    }
}
