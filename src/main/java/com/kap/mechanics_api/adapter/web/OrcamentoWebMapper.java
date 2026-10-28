package com.kap.mechanics_api.adapter.web;

import com.kap.mechanics_api.core.orcamento.usecase.ConsultaDeItensDoOrcamento;
import com.kap.mechanics_api.adapter.web.dto.orcamento.ConsultaOrcamentoItensResponseDTO;
import com.kap.mechanics_api.adapter.web.dto.orcamento.ItemOrcamentoResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class OrcamentoWebMapper {

    public ConsultaOrcamentoItensResponseDTO paraDto(ConsultaDeItensDoOrcamento consulta) {
        return new ConsultaOrcamentoItensResponseDTO(consulta.orcamentoId(), consulta.status(), consulta.valorTotal(),
                consulta.linhas().stream()
                        .map(l -> new ItemOrcamentoResponseDTO(l.id(), l.tipo().name(), l.referenciaId(), l.nome(),
                                l.quantidade(), l.valorUnitario(), l.valorTotal()))
                        .toList());
    }
}
