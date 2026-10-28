package com.kap.mechanics_api.adapter.web.dto.auth;

public record LoginRequestDTO(
        String login,
        String senha
) { }
