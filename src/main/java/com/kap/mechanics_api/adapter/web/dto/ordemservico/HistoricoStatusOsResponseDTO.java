package com.kap.mechanics_api.adapter.web.dto.ordemservico;

import java.time.LocalDateTime;

public record HistoricoStatusOsResponseDTO(
        String status,
        LocalDateTime dataHoraInicio,
        LocalDateTime dataHoraFim
) {
}
