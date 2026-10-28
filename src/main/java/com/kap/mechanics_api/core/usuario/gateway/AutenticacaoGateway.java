package com.kap.mechanics_api.core.usuario.gateway;

public interface AutenticacaoGateway {

    /** Valida as credenciais e devolve o token de acesso. */
    String autenticar(String login, String senha);
}
