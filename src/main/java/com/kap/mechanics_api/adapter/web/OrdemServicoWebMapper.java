package com.kap.mechanics_api.adapter.web;

import com.kap.mechanics_api.core.ordemservico.domain.HistoricoDeStatus;
import com.kap.mechanics_api.core.ordemservico.domain.OrdemServicoResumo;
import com.kap.mechanics_api.core.ordemservico.usecase.ConsultaDeItensDaOrdemServico;
import com.kap.mechanics_api.adapter.web.dto.ordemservico.ConsultaOrdemServicoItensResponseDTO;
import com.kap.mechanics_api.adapter.web.dto.ordemservico.HistoricoStatusOsResponseDTO;
import com.kap.mechanics_api.adapter.web.dto.ordemservico.ItemOrdemServicoResponseDTO;
import com.kap.mechanics_api.adapter.web.dto.ordemservico.ListagemOrdemServicoResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrdemServicoWebMapper {

    public ListagemOrdemServicoResponseDTO paraDto(OrdemServicoResumo resumo) {
        return new ListagemOrdemServicoResponseDTO(resumo.id(), resumo.orcamentoId(), resumo.status().name(),
                resumo.dataAbertura(), resumo.dataEntrega());
    }

    public List<ListagemOrdemServicoResponseDTO> paraDtoLista(List<OrdemServicoResumo> resumos) {
        return resumos.stream().map(this::paraDto).toList();
    }

    public ConsultaOrdemServicoItensResponseDTO paraDto(ConsultaDeItensDaOrdemServico consulta) {
        return new ConsultaOrdemServicoItensResponseDTO(consulta.ordemServicoId(), consulta.orcamentoId(),
                consulta.status().name(), consulta.valorTotal(),
                consulta.linhas().stream()
                        .map(l -> new ItemOrdemServicoResponseDTO(l.id(), l.orcamentoItemId(), l.tipo().name(),
                                l.referenciaId(), l.nome(), l.quantidade(), l.valorUnitario(), l.valorTotal()))
                        .toList());
    }

    public List<HistoricoStatusOsResponseDTO> paraDtoHistorico(List<HistoricoDeStatus> historico) {
        return historico.stream()
                .map(h -> new HistoricoStatusOsResponseDTO(h.getStatus().name(), h.getInicio(), h.getFim()))
                .toList();
    }
}
