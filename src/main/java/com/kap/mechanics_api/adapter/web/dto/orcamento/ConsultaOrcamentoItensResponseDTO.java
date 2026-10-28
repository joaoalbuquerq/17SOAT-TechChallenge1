package com.kap.mechanics_api.adapter.web.dto.orcamento;

import com.kap.mechanics_api.core.orcamento.domain.StatusOrcamento;
import java.math.BigDecimal;
import java.util.List;

public record ConsultaOrcamentoItensResponseDTO(
        Integer orcamentoId,
        StatusOrcamento status,
        BigDecimal valorTotal,
        List<ItemOrcamentoResponseDTO> itens
) {}
