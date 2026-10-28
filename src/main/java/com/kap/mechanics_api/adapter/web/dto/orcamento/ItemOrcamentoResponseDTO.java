package com.kap.mechanics_api.adapter.web.dto.orcamento;

import java.math.BigDecimal;

public record ItemOrcamentoResponseDTO(
        Integer id,
        String tipo,
        Integer referenciaId,
        String nome,
        Integer quantidade,
        BigDecimal valorUnitario,
        BigDecimal valorTotal
) {}
