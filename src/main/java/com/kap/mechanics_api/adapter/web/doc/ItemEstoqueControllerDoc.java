package com.kap.mechanics_api.adapter.web.doc;

import com.kap.mechanics_api.adapter.web.dto.itemestoque.AtualizacaoItemEstoqueRequestDTO;
import com.kap.mechanics_api.adapter.web.dto.itemestoque.CriacaoItemEstoqueRequestDTO;
import com.kap.mechanics_api.adapter.web.dto.itemestoque.ItemEstoqueResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface ItemEstoqueControllerDoc {

    @Operation(summary = "Cadastrar um novo item de estoque")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item de estoque cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    ResponseEntity<ItemEstoqueResponseDTO> cadastrar(
            @Valid @RequestBody CriacaoItemEstoqueRequestDTO dto
    );

    @Operation(summary = "Listar todos os itens de estoque")
    @ApiResponse(responseCode = "200", description = "Lista de itens retornada com sucesso")
    ResponseEntity<List<ItemEstoqueResponseDTO>> listar();

    @Operation(summary = "Buscar item de estoque por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item de estoque encontrado"),
            @ApiResponse(responseCode = "404", description = "Item de estoque não encontrado")
    })
    ResponseEntity<ItemEstoqueResponseDTO> buscarPorId(
            @PathVariable Integer id
    );

    @Operation(summary = "Atualizar um item de estoque")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item de estoque atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou nenhum campo informado"),
            @ApiResponse(responseCode = "404", description = "Item de estoque não encontrado")
    })
    ResponseEntity<ItemEstoqueResponseDTO> atualizar(
            @PathVariable Integer id,
            @Valid @RequestBody AtualizacaoItemEstoqueRequestDTO dto
    );

    @Operation(summary = "Excluir um item de estoque")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Item de estoque removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Item de estoque não encontrado")
    })
    ResponseEntity<Void> deletar(
            @PathVariable Integer id
    );
}
