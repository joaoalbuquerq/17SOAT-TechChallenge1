package com.kap.mechanics_api.adapter.web.dto.movimentacaoestoque;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RegistroEntradaMovimentacaoEstoqueRequestDTO(
        @NotNull(message = "Informe o item de estoque")
        Integer itemEstoqueId,

        @NotNull(message = "Informe a quantidade")
        @Positive(message = "A quantidade deve ser maior que zero")
        Integer quantidade
) {
}
