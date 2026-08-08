package com.kap.mechanics_api.mapper;

import java.util.List;

import org.mapstruct.Mapping;

import com.kap.mechanics_api.domain.Clientes;
import com.kap.mechanics_api.dto.cliente.AtualizacaoClienteResponseDTO;
import com.kap.mechanics_api.dto.cliente.CriacaoClienteRequestDTO;
import com.kap.mechanics_api.dto.cliente.CriacaoClienteResponseDTO;
import com.kap.mechanics_api.dto.cliente.ListagemClienteResponseDTO;

public interface ClientesMapper {
	
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    Clientes dtoToEntity(CriacaoClienteRequestDTO dto);
    CriacaoClienteResponseDTO entityToDto(Clientes clientes);
    
    List<ListagemClienteResponseDTO> listEntityToListDto (List<Clientes> clientes);
    ListagemClienteResponseDTO entityToListagemDto(Clientes clientes);
    AtualizacaoClienteResponseDTO entityToAtualizacaoDto(Clientes cliente);
}
