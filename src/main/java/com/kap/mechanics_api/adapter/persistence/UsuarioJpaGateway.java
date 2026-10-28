package com.kap.mechanics_api.adapter.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.kap.mechanics_api.adapter.persistence.entity.UsuarioJpaEntity;
import com.kap.mechanics_api.core.movimentacaoestoque.gateway.ResponsavelMovimentacaoGateway;
import com.kap.mechanics_api.core.ordemservico.gateway.AtendenteGateway;
import com.kap.mechanics_api.core.usuario.domain.Usuario;
import com.kap.mechanics_api.core.usuario.gateway.UsuarioGateway;
import com.kap.mechanics_api.adapter.persistence.repository.UsuarioRepository;

// Também atende às portas estreitas de estoque e ordem de serviço, que só precisam resolver login -> id.
@Component
public class UsuarioJpaGateway implements UsuarioGateway, AtendenteGateway, ResponsavelMovimentacaoGateway {

    private final UsuarioJpaMapper usuarioJpaMapper;
    private final UsuarioRepository usuarioRepository;

    public UsuarioJpaGateway(UsuarioJpaMapper usuarioJpaMapper, UsuarioRepository usuarioRepository) {
        this.usuarioJpaMapper = usuarioJpaMapper;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        var entity = usuarioJpaMapper.paraEntity(usuario);
        return usuarioJpaMapper.paraDominio(usuarioRepository.save(entity));
    }

    @Override
    public Optional<Usuario> buscarPorId(Integer id) {
       return usuarioRepository.findById(id).map(usuarioJpaMapper::paraDominio);
    }

    @Override
    public List<Usuario> listarTodos() {
      return usuarioRepository.findAll().stream().map(usuarioJpaMapper::paraDominio).toList();
    }

    @Override
    public void excluir(Integer id) {
        usuarioRepository.deleteById(id);
    }

    @Override
    public Optional<Integer> buscarIdPorLogin(String login) {
        return usuarioRepository.findByLogin(login).map(UsuarioJpaEntity::getId);
    }
}
