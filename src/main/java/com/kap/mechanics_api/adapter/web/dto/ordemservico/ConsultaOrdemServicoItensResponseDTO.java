package com.kap.mechanics_api.adapter.web.dto.ordemservico;

import java.math.BigDecimal;
import java.util.List;

public record ConsultaOrdemServicoItensResponseDTO(
        Integer ordemServicoId,
        Integer orcamentoId,
        String status,
        BigDecimal valorTotal,
        List<ItemOrdemServicoResponseDTO> itens
) {}
