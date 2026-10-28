package com.kap.mechanics_api.core.ordemservico.gateway;

import java.util.Optional;

/**
 * Porta da ordem de serviço para o contexto Usuário: só precisa resolver o login
 * de quem está logado no id do atendente.
 */
public interface AtendenteGateway {

    Optional<Integer> buscarIdPorLogin(String login);

}
