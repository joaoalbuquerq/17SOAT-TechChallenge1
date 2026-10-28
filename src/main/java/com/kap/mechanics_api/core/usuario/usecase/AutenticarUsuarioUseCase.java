package com.kap.mechanics_api.core.usuario.usecase;

import com.kap.mechanics_api.core.usuario.gateway.AutenticacaoGateway;

public class AutenticarUsuarioUseCase {

    private final AutenticacaoGateway autenticacaoGateway;

    public AutenticarUsuarioUseCase(AutenticacaoGateway autenticacaoGateway) {
        this.autenticacaoGateway = autenticacaoGateway;
    }

    public String executar(String login, String senha) {
        return autenticacaoGateway.autenticar(login, senha);
    }
}
