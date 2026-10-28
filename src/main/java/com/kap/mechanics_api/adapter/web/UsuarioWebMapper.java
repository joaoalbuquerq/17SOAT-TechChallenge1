package com.kap.mechanics_api.adapter.web;

import com.kap.mechanics_api.core.usuario.domain.Usuario;
import com.kap.mechanics_api.adapter.web.dto.usuario.AtualizacaoUsuarioResponseDTO;
import com.kap.mechanics_api.adapter.web.dto.usuario.CriacaoUsuarioResponseDTO;
import com.kap.mechanics_api.adapter.web.dto.usuario.ListagemUsuarioDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UsuarioWebMapper {

    CriacaoUsuarioResponseDTO paraCriacaoDto(Usuario usuario);

    @Mapping(target = "senha", ignore = true)
    ListagemUsuarioDTO paraListagemDto(Usuario usuario);

    List<ListagemUsuarioDTO> paraListagemDtoLista(List<Usuario> usuarios);

    @Mapping(target = "senha", ignore = true)
    AtualizacaoUsuarioResponseDTO paraAtualizacaoDto(Usuario usuario);
}