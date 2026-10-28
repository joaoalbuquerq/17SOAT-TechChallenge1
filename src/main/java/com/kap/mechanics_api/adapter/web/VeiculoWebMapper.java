package com.kap.mechanics_api.adapter.web;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.kap.mechanics_api.core.veiculo.domain.Veiculo;
import com.kap.mechanics_api.adapter.web.dto.veiculo.AtualizacaoVeiculoResponseDTO;
import com.kap.mechanics_api.adapter.web.dto.veiculo.CriacaoVeiculoResponseDTO;
import com.kap.mechanics_api.adapter.web.dto.veiculo.ListagemVeiculoResponseDTO;

@Mapper(componentModel = "spring")
public interface VeiculoWebMapper {

    CriacaoVeiculoResponseDTO paraCriacaoDto(Veiculo veiculo);

    @Mapping(target = "cliente", ignore = true)
    ListagemVeiculoResponseDTO paraListagemDto(Veiculo veiculo);

    @Mapping(target = "clienteId", ignore = true)
    AtualizacaoVeiculoResponseDTO paraAtualizacaoDto(Veiculo veiculo);

    List<ListagemVeiculoResponseDTO> paraListagemDtoLista(List<Veiculo> veiculos);
}
