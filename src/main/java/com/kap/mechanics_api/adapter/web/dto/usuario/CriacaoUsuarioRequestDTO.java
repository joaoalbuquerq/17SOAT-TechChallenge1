package com.kap.mechanics_api.adapter.web.dto.usuario;

import jakarta.validation.constraints.NotBlank;

public record CriacaoUsuarioRequestDTO(
        @NotBlank
        String nome,

        @NotBlank
        String login,

        @NotBlank
        String senha,

        @NotBlank
        String tipo
) {
}
