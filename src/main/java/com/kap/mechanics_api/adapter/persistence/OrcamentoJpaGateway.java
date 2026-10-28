package com.kap.mechanics_api.adapter.persistence;

import com.kap.mechanics_api.adapter.persistence.entity.OrcamentoItemJpaEntity;
import com.kap.mechanics_api.adapter.persistence.entity.OrcamentoJpaEntity;
import com.kap.mechanics_api.adapter.persistence.entity.OrcamentoServicoJpaEntity;
import com.kap.mechanics_api.adapter.persistence.entity.OrcamentoServicoJpaId;
import com.kap.mechanics_api.core.orcamento.domain.ItemOrcamento;
import com.kap.mechanics_api.core.orcamento.domain.Orcamento;
import com.kap.mechanics_api.core.orcamento.domain.ServicoOrcado;
import com.kap.mechanics_api.core.orcamento.gateway.OrcamentoGateway;
import com.kap.mechanics_api.adapter.persistence.repository.ClienteRepository;
import com.kap.mechanics_api.adapter.persistence.repository.ItemEstoqueRepository;
import com.kap.mechanics_api.adapter.persistence.repository.OrcamentoItemRepository;
import com.kap.mechanics_api.adapter.persistence.repository.OrcamentoRepository;
import com.kap.mechanics_api.adapter.persistence.repository.OrcamentoServicoRepository;
import com.kap.mechanics_api.adapter.persistence.repository.ServicoRepository;
import com.kap.mechanics_api.adapter.persistence.repository.VeiculoRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OrcamentoJpaGateway implements OrcamentoGateway {

    private final OrcamentoJpaMapper mapper;
    private final OrcamentoRepository orcamentoRepository;
    private final OrcamentoItemRepository itemRepository;
    private final OrcamentoServicoRepository servicoOrcadoRepository;
    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;
    private final ServicoRepository servicoRepository;
    private final ItemEstoqueRepository itemEstoqueRepository;

    public OrcamentoJpaGateway(OrcamentoJpaMapper mapper, OrcamentoRepository orcamentoRepository,
                               OrcamentoItemRepository itemRepository,
                               OrcamentoServicoRepository servicoOrcadoRepository,
                               ClienteRepository clienteRepository, VeiculoRepository veiculoRepository,
                               ServicoRepository servicoRepository, ItemEstoqueRepository itemEstoqueRepository) {
        this.mapper = mapper;
        this.orcamentoRepository = orcamentoRepository;
        this.itemRepository = itemRepository;
        this.servicoOrcadoRepository = servicoOrcadoRepository;
        this.clienteRepository = clienteRepository;
        this.veiculoRepository = veiculoRepository;
        this.servicoRepository = servicoRepository;
        this.itemEstoqueRepository = itemEstoqueRepository;
    }

    @Override
    public Orcamento salvar(Orcamento orcamento) {
        OrcamentoJpaEntity entity = orcamento.getId() == null
                ? novaEntity(orcamento)
                : orcamentoRepository.findById(orcamento.getId()).orElseThrow(
                        () -> new IllegalStateException("Orçamento inexistente: " + orcamento.getId()));
        entity.setValorTotal(orcamento.getValorTotal());
        entity.setStatusOrcamento(orcamento.getStatus());
        entity.setDataResposta(orcamento.getDataResposta());
        OrcamentoJpaEntity salvo = orcamentoRepository.save(entity);

        // linhas e serviços orçados só são acrescentados, nunca alterados
        for (ItemOrcamento item : orcamento.getItens()) {
            if (item.getId() == null) {
                itemRepository.save(novaLinha(salvo, item));
            }
        }
        for (ServicoOrcado servicoOrcado : orcamento.getServicosOrcados()) {
            var chave = new OrcamentoServicoJpaId(salvo.getId(), servicoOrcado.servicoId());
            if (!servicoOrcadoRepository.existsById(chave)) {
                servicoOrcadoRepository.save(new OrcamentoServicoJpaEntity(salvo,
                        servicoRepository.getReferenceById(servicoOrcado.servicoId()), servicoOrcado.valorCobrado()));
            }
        }
        return carregar(salvo);
    }

    @Override
    public Optional<Orcamento> pesquisarPorId(Integer id) {
        return orcamentoRepository.findById(id).map(this::carregar);
    }

    private OrcamentoJpaEntity novaEntity(Orcamento orcamento) {
        var entity = new OrcamentoJpaEntity();
        entity.setCliente(clienteRepository.getReferenceById(orcamento.getClienteId()));
        entity.setVeiculo(veiculoRepository.getReferenceById(orcamento.getVeiculoId()));
        entity.setDataCriacao(orcamento.getDataCriacao());
        return entity;
    }

    private OrcamentoItemJpaEntity novaLinha(OrcamentoJpaEntity orcamento, ItemOrcamento item) {
        var linha = new OrcamentoItemJpaEntity();
        linha.setOrcamento(orcamento);
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

    private Orcamento carregar(OrcamentoJpaEntity entity) {
        return mapper.paraDominio(entity,
                itemRepository.findByOrcamento_Id(entity.getId()),
                servicoOrcadoRepository.findByOrcamento_Id(entity.getId()));
    }
}
