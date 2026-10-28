package com.kap.mechanics_api.adapter.web.dto.usuario;

import com.kap.mechanics_api.core.usuario.domain.TipoUsuario;

import java.time.LocalDateTime;

public record ListagemUsuarioDTO(

        Integer id,
        String nome,
        String senha,
        String login,
        LocalDateTime dataCriacao,
        TipoUsuario tipo
) {
}
