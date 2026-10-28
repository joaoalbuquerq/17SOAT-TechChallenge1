package com.kap.mechanics_api.infra.security;

import com.kap.mechanics_api.core.usuario.gateway.AutenticacaoGateway;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class SpringSecurityAutenticacaoGateway implements AutenticacaoGateway {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public SpringSecurityAutenticacaoGateway(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    public String autenticar(String login, String senha) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(login, senha));
        return jwtService.gerarToken(authentication);
    }
}
