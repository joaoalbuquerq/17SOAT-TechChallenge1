package com.kap.mechanics_api.adapter.web.dto.veiculo;

public record ListagemVeiculoResponseDTO(
        Integer id,
        String placa,
        String marca,
        String modelo,
        int ano,
        Integer cliente
) {
}
