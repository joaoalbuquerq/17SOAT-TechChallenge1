package com.kap.mechanics_api.adapter.web.doc;

import com.kap.mechanics_api.adapter.web.dto.orcamento.AtualizacaoStatusOrcamentoRequestDTO;
import com.kap.mechanics_api.adapter.web.dto.orcamento.GeracaoOrcamentoRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

public interface OrcamentoControllerDoc {

    @Operation(summary = "Gerar um novo orçamento")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orçamento gerado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Cliente, veículo, serviço ou usuário não encontrado")
    })
    ResponseEntity<String> gerarOrcamento(
            @Valid @RequestBody GeracaoOrcamentoRequestDTO dto,
            Authentication authentication
    );

    @Operation(summary = "Atualizar o status de um orçamento")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status do orçamento atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Status inválido"),
            @ApiResponse(responseCode = "404", description = "Orçamento não encontrado")
    })
    ResponseEntity<String> atualizarStatus(
            @PathVariable Integer id,
            @Valid @RequestBody AtualizacaoStatusOrcamentoRequestDTO dto
    );
}
