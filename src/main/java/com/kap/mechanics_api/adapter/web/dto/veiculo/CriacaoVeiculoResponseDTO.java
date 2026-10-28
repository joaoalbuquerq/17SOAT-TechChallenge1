package com.kap.mechanics_api.adapter.web.dto.veiculo;

public record CriacaoVeiculoResponseDTO(
        Integer id,
        String placa,
        String marca,
        String modelo,
        int ano
) {
}
