package com.kap.mechanics_api.adapter.web.dto.orcamento;

import jakarta.validation.constraints.NotBlank;

public record AtualizacaoStatusOrcamentoRequestDTO(
        @NotBlank
        String status
) {
}
