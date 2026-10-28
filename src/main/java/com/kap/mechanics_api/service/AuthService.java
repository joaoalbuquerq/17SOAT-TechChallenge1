package com.kap.mechanics_api.service;

import com.kap.mechanics_api.dto.auth.LoginRequestDTO;
import com.kap.mechanics_api.dto.auth.LoginResponseDTO;
import com.kap.mechanics_api.infra.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService) {

        this.authenticationManager =
                authenticationManager;

        this.jwtService = jwtService;
    }

    public LoginResponseDTO login(
            LoginRequestDTO request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.login(),
                                request.senha()
                        )
                );

        String token =
                jwtService.gerarToken(authentication);

        return new LoginResponseDTO(token);
    }
}
