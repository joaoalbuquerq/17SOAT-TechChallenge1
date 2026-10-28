package com.kap.mechanics_api.adapter.web;

import java.util.List;

import org.mapstruct.Mapper;

import com.kap.mechanics_api.core.servico.domain.Servico;
import com.kap.mechanics_api.adapter.web.dto.servico.ServicoResponseDTO;

@Mapper(componentModel = "spring")
public interface ServicoWebMapper {

    ServicoResponseDTO paraDto(Servico servico);

    List<ServicoResponseDTO> paraDtoLista(List<Servico> servicos);
}
