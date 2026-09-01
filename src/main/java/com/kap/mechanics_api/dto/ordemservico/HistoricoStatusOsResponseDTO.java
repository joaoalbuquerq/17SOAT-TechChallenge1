package com.kap.mechanics_api.dto.ordemservico;

import java.time.LocalDateTime;

public record HistoricoStatusOsResponseDTO(
        String status,
        LocalDateTime dataHoraInicio,
        LocalDateTime dataHoraFim
) {
}
