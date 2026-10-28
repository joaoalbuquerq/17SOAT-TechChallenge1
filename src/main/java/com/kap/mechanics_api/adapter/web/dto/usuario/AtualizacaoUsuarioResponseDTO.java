package com.kap.mechanics_api.adapter.web.dto.usuario;

import com.kap.mechanics_api.core.usuario.domain.TipoUsuario;

public record AtualizacaoUsuarioResponseDTO(
        Integer id,
        String nome,
        String login,
        String senha,
        TipoUsuario tipo
) {
}
