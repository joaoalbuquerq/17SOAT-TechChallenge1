package com.kap.mechanics_api.adapter.web.dto.servico;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CriacaoServicoRequestDTO(

        @NotBlank(message = "Informe o nome do serviço")
        String nome,

        @NotBlank(message = "Informe a descrição do serviço")
        String descricao,

        @NotNull(message = "Informe o valor da mão de obra")
        @DecimalMin(value = "0.0", inclusive = true, message = "O valor da mão de obra não pode ser negativo")
        BigDecimal valorMaoDeObra,

        @NotNull(message = "Informe o tempo estimado")
        @PositiveOrZero(message = "O tempo estimado não pode ser negativo")
        Integer tempoEstimadoMin,

        @NotNull(message = "Informe se o serviço está ativo")
        Boolean ativo
) {
}
