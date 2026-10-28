package com.kap.mechanics_api.adapter.web;

import com.kap.mechanics_api.adapter.web.doc.AuthControllerDoc;
import com.kap.mechanics_api.adapter.web.dto.auth.LoginRequestDTO;
import com.kap.mechanics_api.adapter.web.dto.auth.LoginResponseDTO;
import com.kap.mechanics_api.core.usuario.usecase.AutenticarUsuarioUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController implements AuthControllerDoc {

    private final AutenticarUsuarioUseCase autenticarUsuarioUseCase;

    public AuthController(AutenticarUsuarioUseCase autenticarUsuarioUseCase) {
        this.autenticarUsuarioUseCase = autenticarUsuarioUseCase;
    }

    @PostMapping("/login")
    @Override
    public ResponseEntity<LoginResponseDTO> login(
            @RequestBody LoginRequestDTO dto) {

        return ResponseEntity.ok(new LoginResponseDTO(autenticarUsuarioUseCase.executar(dto.login(), dto.senha())));
    }
}
