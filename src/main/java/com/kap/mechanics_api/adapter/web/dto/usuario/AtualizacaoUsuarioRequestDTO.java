package com.kap.mechanics_api.adapter.web.dto.usuario;

import org.springframework.util.StringUtils;

public record AtualizacaoUsuarioRequestDTO(
        String nome,
        String login,
        String senha,
        String tipo
) {
    public boolean temAoMenosUmCampoPreenchido() {
        return StringUtils.hasText(nome) || StringUtils.hasText(login) || StringUtils.hasText(senha) || StringUtils.hasText(tipo);
    }
}
