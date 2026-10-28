package com.kap.mechanics_api.infra.security;

import com.kap.mechanics_api.adapter.persistence.entity.UsuarioJpaEntity;
import com.kap.mechanics_api.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {

        UsuarioJpaEntity usuario = usuarioRepository.findByLogin(login)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Usuário não encontrado")
                );

        return User.builder()
                .username(usuario.getLogin())
                .password(usuario.getSenhaHash())
                .roles(usuario.getTipo().name())
                .build();
    }
}
