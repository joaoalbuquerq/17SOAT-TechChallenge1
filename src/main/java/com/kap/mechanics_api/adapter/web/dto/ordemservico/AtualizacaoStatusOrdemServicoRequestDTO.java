package com.kap.mechanics_api.adapter.web.dto.ordemservico;

import com.kap.mechanics_api.core.ordemservico.domain.StatusOrdemServico;
import jakarta.validation.constraints.NotNull;

public record AtualizacaoStatusOrdemServicoRequestDTO(
        @NotNull(message = "Informe o novo status da ordem de servico")
        StatusOrdemServico status
) {
}
