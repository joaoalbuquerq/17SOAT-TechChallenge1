package com.kap.mechanics_api.adapter.web.dto.servico;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record AtualizacaoServicoRequestDTO(

        String nome,

        String descricao,

        @PositiveOrZero(
                message = "O valor da mão de obra não pode ser negativo"
        )
        BigDecimal valorMaoDeObra,

        @PositiveOrZero(
                message = "O tempo estimado não pode ser negativo"
        )
        Integer tempoEstimadoMin,

        Boolean ativo

) {

    public boolean temAoMenosUmCampoPreenchido() {
        return nome != null
                || descricao != null
                || valorMaoDeObra != null
                || tempoEstimadoMin != null
                || ativo != null;
    }
}