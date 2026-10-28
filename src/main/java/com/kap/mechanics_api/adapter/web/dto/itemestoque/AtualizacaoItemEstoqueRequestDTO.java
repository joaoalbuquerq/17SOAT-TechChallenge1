package com.kap.mechanics_api.adapter.web.dto.itemestoque;

import com.kap.mechanics_api.core.itemestoque.domain.TipoItemEstoque;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

public record AtualizacaoItemEstoqueRequestDTO(
        @Pattern(regexp = ".*\\S.*", message = "O nome não pode estar em branco")
        String nome,

        @Pattern(regexp = ".*\\S.*", message = "A descrição não pode estar em branco")
        String descricao,

        TipoItemEstoque tipoItemEstoque,

        @DecimalMin(value = "0.0", inclusive = true, message = "O valor unitário não pode ser negativo")
        BigDecimal valorUnitario,

        @PositiveOrZero(message = "A quantidade mínima não pode ser negativa")
        Integer quantidadeMinima,

        Boolean ativo
) {
    public boolean temAoMenosUmCampoPreenchido() {
        return StringUtils.hasText(nome) || StringUtils.hasText(descricao)
                || tipoItemEstoque != null || valorUnitario != null
//                || quantidadeAtual != null
                || quantidadeMinima != null
                || ativo != null;
    }
}
