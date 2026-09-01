package com.kap.mechanics_api.service;

import com.kap.mechanics_api.domain.*;
import com.kap.mechanics_api.dto.orcamento.InclusaoOrcamentoItemRequestDTO;
import com.kap.mechanics_api.enums.StatusOrcamento;
import com.kap.mechanics_api.exception.OrcamentoNaoEncontradoException;
import com.kap.mechanics_api.repository.OrcamentoItemRepository;
import com.kap.mechanics_api.repository.OrcamentoRepository;
import com.kap.mechanics_api.repository.ServicoItemRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import com.kap.mechanics_api.dto.orcamento.ConsultaOrcamentoItensResponseDTO;
import com.kap.mechanics_api.dto.orcamento.ItemOrcamentoResponseDTO;

@Service
public class OrcamentoItemService {
    private final OrcamentoRepository orcamentoRepository;
    private final OrcamentoItemRepository itemRepository;
    private final ServicoService servicoService;
    private final ItemEstoqueService itemEstoqueService;
    private final ServicoItemRepository servicoItemRepository;

    public OrcamentoItemService(OrcamentoRepository orcamentoRepository, OrcamentoItemRepository itemRepository,
                                ServicoService servicoService, ItemEstoqueService itemEstoqueService,
                                ServicoItemRepository servicoItemRepository) {
        this.orcamentoRepository = orcamentoRepository; this.itemRepository = itemRepository;
        this.servicoService = servicoService; this.itemEstoqueService = itemEstoqueService;
        this.servicoItemRepository = servicoItemRepository;
    }

    @Transactional
    public void incluir(Integer orcamentoId, InclusaoOrcamentoItemRequestDTO dto) {
        Orcamento orcamento = orcamentoRepository.findById(orcamentoId)
                .orElseThrow(() -> new OrcamentoNaoEncontradoException("Orçamento não encontrado"));
        if (orcamento.getStatusOrcamento() != StatusOrcamento.PENDENTE) throw new IllegalArgumentException("Só é possível incluir itens em orçamento pendente.");
        validarUmaReferencia(dto.servicoId(), dto.itemEstoqueId());
        if (dto.servicoId() != null) incluirServico(orcamento, servicoService.pesquisarPorId(dto.servicoId()), dto.quantidade());
        else incluirItemEstoque(orcamento, itemEstoqueService.pesquisarPorId(dto.itemEstoqueId()), dto.quantidade());
        orcamentoRepository.save(orcamento);
    }

    @Transactional
    public ConsultaOrcamentoItensResponseDTO consultar(Integer orcamentoId) {
        Orcamento orcamento = orcamentoRepository.findById(orcamentoId)
                .orElseThrow(() -> new OrcamentoNaoEncontradoException("Orçamento não encontrado"));
        List<ItemOrcamentoResponseDTO> itens = itemRepository.findByOrcamento_Id(orcamentoId).stream()
                .map(item -> new ItemOrcamentoResponseDTO(item.getId(), item.getServico() == null ? "ESTOQUE" : "SERVICO",
                        item.getServico() == null ? item.getItemEstoque().getId() : item.getServico().getId(),
                        item.getServico() == null ? item.getItemEstoque().getNome() : item.getServico().getNome(), item.getQuantidade(),
                        item.getValorUnitarioCobrado(), item.getValorUnitarioCobrado().multiply(BigDecimal.valueOf(item.getQuantidade()))))
                .toList();
        return new ConsultaOrcamentoItensResponseDTO(orcamentoId, orcamento.getStatusOrcamento(), orcamento.getValorTotal(), itens);
    }

	private void incluirServico(Orcamento orcamento, Servico servico, int quantidade) {
		salvar(orcamento, servico, null, quantidade, servico.getValorMaoDeObra());
		for (ServicoItem componente : servicoItemRepository.findByServico_Id(servico.getId()))
			incluirItemEstoque(orcamento, componente.getItemEstoque(),
					Math.multiplyExact(quantidade, componente.getQuantidadePadrao()));
	}

	private void incluirItemEstoque(Orcamento orcamento, ItemEstoque item, int quantidade) {
		salvar(orcamento, null, item, quantidade, item.getValorUnitario());
	}

	private void salvar(Orcamento orcamento, Servico servico, ItemEstoque item, int quantidade,
			BigDecimal valorUnitario) {
		OrcamentoItem linha = new OrcamentoItem();
		linha.setOrcamento(orcamento);
		linha.setServico(servico);
		linha.setItemEstoque(item);
		linha.setQuantidade(quantidade);
		linha.setValorUnitarioCobrado(valorUnitario);
		itemRepository.save(linha);
		orcamento.setValorTotal(orcamento.getValorTotal().add(valorUnitario.multiply(BigDecimal.valueOf(quantidade))));
	}

	private void validarUmaReferencia(Integer servicoId, Integer itemId) {
		if ((servicoId == null) == (itemId == null))
			throw new IllegalArgumentException("Informe exatamente um: servicoId ou itemEstoqueId.");
	}
}
