package com.kap.mechanics_api.adapter.web.dto.movimentacaoestoque;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RegistroSaidaMovimentacaoEstoqueRequestDTO(
        @NotNull(message = "Informe o item de estoque")
        Integer itemEstoqueId,

        @NotNull(message = "Informe a quantidade")
        @Positive(message = "A quantidade deve ser maior que zero")
        Integer quantidade,

        @NotNull(message = "Informe a ordem de servico")
        Integer ordemServicoId
) {
}
