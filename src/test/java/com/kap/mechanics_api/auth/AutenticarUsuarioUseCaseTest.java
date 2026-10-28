package com.kap.mechanics_api.auth;

import com.kap.mechanics_api.core.usuario.gateway.AutenticacaoGateway;
import com.kap.mechanics_api.core.usuario.usecase.AutenticarUsuarioUseCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AutenticarUsuarioUseCaseTest {

    @Test
    void deveDevolverTokenDoGateway() {
        AutenticacaoGateway gateway = mock(AutenticacaoGateway.class);
        when(gateway.autenticar("joao", "senha123")).thenReturn("jwt-token");

        assertEquals("jwt-token", new AutenticarUsuarioUseCase(gateway).executar("joao", "senha123"));
    }
}
