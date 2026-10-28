package com.kap.mechanics_api.adapter.web.dto.itemestoque;

import com.kap.mechanics_api.core.itemestoque.domain.TipoItemEstoque;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CriacaoItemEstoqueRequestDTO(
        @NotBlank(message = "Informe o nome do item")
        String nome,

        @NotBlank(message = "Informe a descrição do item")
        String descricao,

        @NotNull(message = "Informe o tipo do item")
        TipoItemEstoque tipoItemEstoque,

        @NotNull(message = "Informe o valor unitário")
        @DecimalMin(value = "0.0", inclusive = true, message = "O valor unitário não pode ser negativo")
        BigDecimal valorUnitario,

        @NotNull(message = "Informe a quantidade mínima")
        @PositiveOrZero(message = "A quantidade mínima não pode ser negativa")
        Integer quantidadeMinima,

        @NotNull(message = "Informe se o item está ativo")
        Boolean ativo
) {
}
