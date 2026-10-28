package com.kap.mechanics_api.adapter.web;

import java.util.List;

import org.mapstruct.Mapper;

import com.kap.mechanics_api.core.itemestoque.domain.ItemEstoque;
import com.kap.mechanics_api.adapter.web.dto.itemestoque.ItemEstoqueResponseDTO;

@Mapper(componentModel = "spring")
public interface ItemEstoqueWebMapper {

    ItemEstoqueResponseDTO paraDto(ItemEstoque itemEstoque);

    List<ItemEstoqueResponseDTO> paraDtoLista(List<ItemEstoque> itens);
}
