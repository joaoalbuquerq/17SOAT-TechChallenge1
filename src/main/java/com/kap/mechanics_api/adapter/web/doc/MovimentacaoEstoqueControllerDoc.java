package com.kap.mechanics_api.adapter.web.doc;

import com.kap.mechanics_api.adapter.web.dto.movimentacaoestoque.MovimentacaoEstoqueResponseDTO;
import com.kap.mechanics_api.adapter.web.dto.movimentacaoestoque.RegistroEntradaMovimentacaoEstoqueRequestDTO;
import com.kap.mechanics_api.adapter.web.dto.movimentacaoestoque.RegistroSaidaMovimentacaoEstoqueRequestDTO;
import com.kap.mechanics_api.core.movimentacaoestoque.domain.TipoMovimentacaoEstoque;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

public interface MovimentacaoEstoqueControllerDoc {

    @Operation(summary = "Registrar entrada no estoque")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Entrada registrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos"),
            @ApiResponse(responseCode = "404", description = "Item ou usuario nao encontrado")
    })
    ResponseEntity<MovimentacaoEstoqueResponseDTO> registrarEntrada(
            @Valid @RequestBody RegistroEntradaMovimentacaoEstoqueRequestDTO dto,
            Authentication authentication
    );

    @Operation(summary = "Registrar saida do estoque")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Saida registrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos ou estoque insuficiente"),
            @ApiResponse(responseCode = "404", description = "Item, usuario ou ordem de servico nao encontrado")
    })
    ResponseEntity<MovimentacaoEstoqueResponseDTO> registrarSaida(
            @Valid @RequestBody RegistroSaidaMovimentacaoEstoqueRequestDTO dto,
            Authentication authentication
    );

    @Operation(summary = "Listar movimentacoes de estoque")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    ResponseEntity<List<MovimentacaoEstoqueResponseDTO>> listar();

    @Operation(summary = "Listar movimentacoes de estoque por item")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    ResponseEntity<List<MovimentacaoEstoqueResponseDTO>> listarPorItem(
            @PathVariable Integer itemEstoqueId
    );

    @Operation(summary = "Listar movimentacoes de estoque por ordem de servico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    ResponseEntity<List<MovimentacaoEstoqueResponseDTO>> listarPorOrdemServico(
            @PathVariable Integer ordemServicoId
    );

    @Operation(summary = "Listar movimentacoes de estoque por tipo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    ResponseEntity<List<MovimentacaoEstoqueResponseDTO>> listarPorTipo(
            @PathVariable TipoMovimentacaoEstoque tipo
    );

    @Operation(summary = "Listar movimentacoes de estoque por periodo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    ResponseEntity<List<MovimentacaoEstoqueResponseDTO>> listarPorPeriodo(
            @RequestParam LocalDateTime inicio,
            @RequestParam LocalDateTime fim
    );

    @Operation(summary = "Buscar movimentacao por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movimentacao encontrada"),
            @ApiResponse(responseCode = "404", description = "Movimentacao nao encontrada")
    })
    ResponseEntity<MovimentacaoEstoqueResponseDTO> buscarPorId(
            @PathVariable Integer id
    );
}
