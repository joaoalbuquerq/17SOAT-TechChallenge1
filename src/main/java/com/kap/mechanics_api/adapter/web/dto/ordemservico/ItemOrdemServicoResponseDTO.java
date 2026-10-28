package com.kap.mechanics_api.adapter.web.dto.ordemservico;

import java.math.BigDecimal;

public record ItemOrdemServicoResponseDTO(
        Integer id,
        Integer orcamentoItemId,
        String tipo,
        Integer referenciaId,
        String nome,
        Integer quantidade,
        BigDecimal valorUnitario,
        BigDecimal valorTotal
) {}
