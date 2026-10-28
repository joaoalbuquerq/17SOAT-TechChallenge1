package com.kap.mechanics_api.adapter.web.doc;

import com.kap.mechanics_api.adapter.web.dto.ordemservico.AtualizacaoStatusOrdemServicoRequestDTO;
import com.kap.mechanics_api.adapter.web.dto.ordemservico.ListagemOrdemServicoResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface OrdemServicoControllerDoc {

    @Operation(summary = "Gerar uma ordem de serviço a partir de um orçamento")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ordem de serviço gerada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Orçamento ou usuário não encontrado")
    })
    ResponseEntity<Void> gerarOrdemServico(
            @PathVariable Integer orcamentoId,
            Authentication authentication
    );

    @Operation(summary = "Atualizar o status de uma ordem de serviço")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Status da ordem de serviço atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Status ou transição inválida"),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada")
    })
    ResponseEntity<Void> transicionarStatus(
            @PathVariable Integer id,
            @Valid @RequestBody AtualizacaoStatusOrdemServicoRequestDTO dto
    );

    @Operation(summary = "Listar ordens de serviço e seus status por cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ordens de serviço retornadas com sucesso"),
            @ApiResponse(responseCode = "400", description = "Filtro de cliente inválido"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    ResponseEntity<List<ListagemOrdemServicoResponseDTO>> listarPorCliente(
            @RequestParam(required = false) Integer clienteId,
            @RequestParam(required = false) String cpfCnpj
    );
}
