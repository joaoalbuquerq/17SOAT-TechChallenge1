package com.kap.mechanics_api.adapter.web.dto.veiculo;

public record AtualizacaoVeiculoResponseDTO(
        Integer id,
        String placa,
        String marca,
        String modelo,
        int ano,
        Integer clienteId
) {
}
