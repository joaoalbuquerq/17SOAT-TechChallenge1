package com.kap.mechanics_api.adapter.web.doc;

import com.kap.mechanics_api.adapter.web.dto.servico.AtualizacaoServicoRequestDTO;
import com.kap.mechanics_api.adapter.web.dto.servico.CriacaoServicoRequestDTO;
import com.kap.mechanics_api.adapter.web.dto.servico.ServicoResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface ServicoControllerDoc {

    @Operation(summary = "Cadastrar um novo serviço")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Serviço cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    ResponseEntity<ServicoResponseDTO> cadastrar(
            @Valid @RequestBody CriacaoServicoRequestDTO dto
    );

    @Operation(summary = "Listar todos os serviços")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de serviços retornada com sucesso")
    })
    ResponseEntity<List<ServicoResponseDTO>> listar();

    @Operation(summary = "Buscar serviço por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Serviço encontrado"),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    })
    ResponseEntity<ServicoResponseDTO> buscarPorId(
            @PathVariable Integer id
    );

    @Operation(summary = "Atualizar um serviço")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Serviço atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Nenhum campo informado"),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    })
    ResponseEntity<ServicoResponseDTO> atualizar(
            @Valid @RequestBody AtualizacaoServicoRequestDTO dto,
            @PathVariable Integer id
    );

    @Operation(summary = "Excluir um serviço")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Serviço removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    })
    ResponseEntity<Void> deletar(
            @PathVariable Integer id
    );
}