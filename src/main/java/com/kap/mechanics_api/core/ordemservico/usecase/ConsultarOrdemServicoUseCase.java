package com.kap.mechanics_api.core.ordemservico.usecase;

import com.kap.mechanics_api.core.ordemservico.domain.OrdemServico;
import com.kap.mechanics_api.core.ordemservico.domain.OrdemServicoResumo;
import com.kap.mechanics_api.core.ordemservico.gateway.OrdemServicoGateway;

public class ConsultarOrdemServicoUseCase {

    private final OrdemServicoGateway gateway;

    public ConsultarOrdemServicoUseCase(OrdemServicoGateway gateway) {
        this.gateway = gateway;
    }

    public OrdemServicoResumo executar(Integer id) {
        OrdemServico os = gateway.pesquisarPorId(id).orElseThrow(() -> new OrdemServicoNaoEncontradaException(id));
        return new OrdemServicoResumo(os.getId(), os.getOrcamentoId(), os.getStatus(), os.getDataAbertura(),
                os.getDataEntrega());
    }
}
