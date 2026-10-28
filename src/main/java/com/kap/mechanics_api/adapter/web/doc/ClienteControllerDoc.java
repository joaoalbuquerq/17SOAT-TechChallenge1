package com.kap.mechanics_api.adapter.web.doc;

import com.kap.mechanics_api.adapter.web.dto.cliente.AtualizacaoClienteRequestDTO;
import com.kap.mechanics_api.adapter.web.dto.cliente.AtualizacaoClienteResponseDTO;
import com.kap.mechanics_api.adapter.web.dto.cliente.CriacaoClienteRequestDTO;
import com.kap.mechanics_api.adapter.web.dto.cliente.CriacaoClienteResponseDTO;
import com.kap.mechanics_api.adapter.web.dto.cliente.ListagemClienteResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface ClienteControllerDoc {

    @Operation(summary = "Cadastrar um novo cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    ResponseEntity<CriacaoClienteResponseDTO> cadastrar(
            @Valid @RequestBody CriacaoClienteRequestDTO clienteDTO
    );

    @Operation(summary = "Listar todos os clientes")
    @ApiResponse(responseCode = "200", description = "Lista de clientes retornada com sucesso")
    ResponseEntity<List<ListagemClienteResponseDTO>> listar();

    @Operation(summary = "Buscar cliente por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    ResponseEntity<ListagemClienteResponseDTO> pesquisarPorId(
            @PathVariable Integer id
    );

    @Operation(summary = "Excluir um cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cliente removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    ResponseEntity<Void> deletar(
            @PathVariable Integer id
    );

    @Operation(summary = "Atualizar um cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    ResponseEntity<AtualizacaoClienteResponseDTO> atualizar(
            @Valid @RequestBody AtualizacaoClienteRequestDTO dto,
            @PathVariable Integer id
    );

    @Operation(summary = "Buscar cliente por CPF ou CNPJ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "400", description = "Documento inválido"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    ResponseEntity<ListagemClienteResponseDTO> pesquisarPorDocumento(
            @PathVariable String documento
    );
}
