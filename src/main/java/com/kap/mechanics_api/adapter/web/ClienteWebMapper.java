package com.kap.mechanics_api.adapter.web;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.kap.mechanics_api.core.cliente.domain.Cliente;
import com.kap.mechanics_api.adapter.web.dto.cliente.AtualizacaoClienteResponseDTO;
import com.kap.mechanics_api.adapter.web.dto.cliente.CriacaoClienteResponseDTO;
import com.kap.mechanics_api.adapter.web.dto.cliente.ListagemClienteResponseDTO;

@Mapper(componentModel = "spring")
public interface ClienteWebMapper {

    @Mapping(source = "cpfCnpj.documento", target = "cpfCnpj")
    CriacaoClienteResponseDTO paraCriacaoDto(Cliente cliente);

    @Mapping(source = "cpfCnpj.documento", target = "cpfCnpj")
    ListagemClienteResponseDTO paraListagemDto(Cliente cliente);

    @Mapping(source = "cpfCnpj.documento", target = "cpfCnpj")
    AtualizacaoClienteResponseDTO paraAtualizacaoDto(Cliente cliente);

    List<ListagemClienteResponseDTO> paraListagemDtoLista(List<Cliente> clientes);
}
