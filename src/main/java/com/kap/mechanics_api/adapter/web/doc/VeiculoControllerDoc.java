package com.kap.mechanics_api.adapter.web.doc;

import com.kap.mechanics_api.adapter.web.dto.veiculo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface VeiculoControllerDoc {

    @Operation(summary = "Cadastrar um novo veículo")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Veículo cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    ResponseEntity<CriacaoVeiculoResponseDTO> cadastrar(
            @Valid @RequestBody CriacaoVeiculoRequestDTO dto
    );

    @Operation(summary = "Listar todos os veículos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de veículos retornada com sucesso")
    })
    ResponseEntity<List<ListagemVeiculoResponseDTO>> listar();

    @Operation(summary = "Buscar veículo por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Veículo encontrado"),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado")
    })
    ResponseEntity<ListagemVeiculoResponseDTO> pesquisarPorId(
            @PathVariable Integer id
    );

    @Operation(summary = "Atualizar um veículo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Veículo atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado")
    })
    ResponseEntity<AtualizacaoVeiculoResponseDTO> atualizar(
            @Valid @RequestBody AtualizacaoVeiculoRequestDTO dto,
            @PathVariable Integer id
    );

    @Operation(summary = "Excluir um veículo")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Veículo removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado")
    })
    ResponseEntity<Void> deletar(
            @PathVariable Integer id
    );

}
