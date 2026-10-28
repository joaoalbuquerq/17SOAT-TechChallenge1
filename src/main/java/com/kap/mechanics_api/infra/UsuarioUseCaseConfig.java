package com.kap.mechanics_api.infra;

import com.kap.mechanics_api.core.usuario.gateway.AutenticacaoGateway;
import com.kap.mechanics_api.core.usuario.gateway.CriptografiaSenhaGateway;
import com.kap.mechanics_api.core.usuario.gateway.UsuarioGateway;
import com.kap.mechanics_api.core.usuario.usecase.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UsuarioUseCaseConfig {

    @Bean
    BuscarPorIdUseCase buscarUsuarioPorIdUseCase(
            UsuarioGateway gateway) {
        return new BuscarPorIdUseCase(gateway);
    }

    @Bean
    ListarUsuarioUseCase listarUsuarioUseCase(UsuarioGateway gateway) {
        return new ListarUsuarioUseCase(gateway);
    }

    @Bean
    ExcluirUsuarioUseCase excluirUsuarioUseCase(UsuarioGateway gateway) {
        return new ExcluirUsuarioUseCase(gateway);
    }

    @Bean
    CadastrarUsuarioUseCase cadastrarUsuarioUseCase(
            UsuarioGateway gateway,
            CriptografiaSenhaGateway criptografia) {
        return new CadastrarUsuarioUseCase(gateway, criptografia);
    }

    @Bean
    AtualizarUsuarioUseCase atualizarUsuarioUseCase(
            UsuarioGateway gateway,
            CriptografiaSenhaGateway criptografia) {
        return new AtualizarUsuarioUseCase(gateway, criptografia);
    }

    @Bean
    AutenticarUsuarioUseCase autenticarUsuarioUseCase(AutenticacaoGateway gateway) {
        return new AutenticarUsuarioUseCase(gateway);
    }
}
