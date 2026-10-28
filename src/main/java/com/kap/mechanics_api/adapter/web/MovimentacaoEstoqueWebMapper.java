package com.kap.mechanics_api.adapter.web;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.kap.mechanics_api.core.movimentacaoestoque.domain.MovimentacaoEstoqueConsulta;
import com.kap.mechanics_api.adapter.web.dto.movimentacaoestoque.MovimentacaoEstoqueResponseDTO;

@Mapper(componentModel = "spring")
public interface MovimentacaoEstoqueWebMapper {

    @Mapping(target = "id", source = "movimentacao.id")
    @Mapping(target = "itemEstoqueId", source = "movimentacao.itemEstoqueId")
    @Mapping(target = "tipo", source = "movimentacao.tipo")
    @Mapping(target = "quantidade", source = "movimentacao.quantidade")
    @Mapping(target = "dataHora", source = "movimentacao.dataHora")
    @Mapping(target = "usuarioId", source = "movimentacao.usuarioId")
    @Mapping(target = "ordemServicoId", source = "movimentacao.ordemServicoId")
    MovimentacaoEstoqueResponseDTO paraDto(MovimentacaoEstoqueConsulta consulta);

    List<MovimentacaoEstoqueResponseDTO> paraDtoLista(List<MovimentacaoEstoqueConsulta> consultas);
}
