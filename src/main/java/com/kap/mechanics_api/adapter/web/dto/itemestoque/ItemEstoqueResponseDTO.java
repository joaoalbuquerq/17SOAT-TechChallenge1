package com.kap.mechanics_api.adapter.web.dto.itemestoque;

import com.kap.mechanics_api.core.itemestoque.domain.TipoItemEstoque;

import java.math.BigDecimal;

public record ItemEstoqueResponseDTO(
        Integer id,
        String nome,
        String descricao,
        TipoItemEstoque tipoItemEstoque,
        BigDecimal valorUnitario,
        Integer quantidadeAtual,
        Integer quantidadeMinima,
        boolean ativo
) {
}
