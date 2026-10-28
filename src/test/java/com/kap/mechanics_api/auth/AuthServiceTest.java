package com.kap.mechanics_api.auth;

import com.kap.mechanics_api.dto.auth.LoginRequestDTO;
import com.kap.mechanics_api.dto.auth.LoginResponseDTO;
import com.kap.mechanics_api.infra.security.JwtService;
import com.kap.mechanics_api.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;

    @InjectMocks private AuthService service;

    @Test
    void deveAutenticarEGerarToken() {
        LoginRequestDTO request = new LoginRequestDTO("joao", "senha123");
        Authentication autenticado = new TestingAuthenticationToken("joao", "senha123");
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(autenticado);
        when(jwtService.gerarToken(autenticado)).thenReturn("jwt-token");

        LoginResponseDTO response = service.login(request);

        assertEquals("jwt-token", response.token());

        ArgumentCaptor<Authentication> captor = ArgumentCaptor.forClass(Authentication.class);
        org.mockito.Mockito.verify(authenticationManager).authenticate(captor.capture());
        assertEquals("joao", captor.getValue().getPrincipal());
        assertEquals("senha123", captor.getValue().getCredentials());
    }
}
