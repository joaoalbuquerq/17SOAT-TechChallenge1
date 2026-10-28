package com.kap.mechanics_api.adapter.web;

import com.kap.mechanics_api.adapter.web.OrdemServicoWebMapper;
import com.kap.mechanics_api.core.ordemservico.usecase.ConsultarHistoricoDeStatusUseCase;
import com.kap.mechanics_api.adapter.web.dto.ordemservico.HistoricoStatusOsResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ordem-servico")
@PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
public class HistoricoStatusController {

    private final ConsultarHistoricoDeStatusUseCase consultarHistoricoUseCase;
    private final OrdemServicoWebMapper ordemServicoMapper;

    public HistoricoStatusController(ConsultarHistoricoDeStatusUseCase consultarHistoricoUseCase,
                                     OrdemServicoWebMapper ordemServicoMapper) {
        this.consultarHistoricoUseCase = consultarHistoricoUseCase;
        this.ordemServicoMapper = ordemServicoMapper;
    }

    @GetMapping("/{ordemServicoId}/historico-status")
    @Operation(summary = "Consultar o histórico de status de uma ordem de serviço")
    public List<HistoricoStatusOsResponseDTO> buscarPorOrdemServico(
            @PathVariable Integer ordemServicoId) {
        return ordemServicoMapper.paraDtoHistorico(consultarHistoricoUseCase.executar(ordemServicoId));
    }
}
