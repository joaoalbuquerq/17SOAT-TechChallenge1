package com.kap.mechanics_api.core.ordemservico.usecase;

import com.kap.mechanics_api.core.ordemservico.domain.OrdemServicoResumo;
import com.kap.mechanics_api.core.ordemservico.domain.StatusOrdemServico;
import com.kap.mechanics_api.core.ordemservico.gateway.OrdemServicoGateway;

import java.util.Comparator;
import java.util.List;

import static com.kap.mechanics_api.core.ordemservico.domain.StatusOrdemServico.AGUARDANDO_APROVACAO;
import static com.kap.mechanics_api.core.ordemservico.domain.StatusOrdemServico.EM_DIAGNOSTICO;
import static com.kap.mechanics_api.core.ordemservico.domain.StatusOrdemServico.EM_EXECUCAO;
import static com.kap.mechanics_api.core.ordemservico.domain.StatusOrdemServico.RECEBIDA;

/**
 * Fila de trabalho da oficina: só as ordens em andamento, por prioridade de status e, dentro do mesmo status,
 * as mais antigas primeiro. Finalizadas e entregues ficam de fora da listagem (exclusão lógica, nada é apagado).
 */
public class ListarOrdensServicoUseCase {

    private static final List<StatusOrdemServico> PRIORIDADE =
            List.of(EM_EXECUCAO, AGUARDANDO_APROVACAO, EM_DIAGNOSTICO, RECEBIDA);

    private final OrdemServicoGateway gateway;

    public ListarOrdensServicoUseCase(OrdemServicoGateway gateway) {
        this.gateway = gateway;
    }

    public List<OrdemServicoResumo> executar() {
        return gateway.listarResumos().stream()
                .filter(os -> PRIORIDADE.contains(os.status()))
                .sorted(Comparator.comparingInt((OrdemServicoResumo os) -> PRIORIDADE.indexOf(os.status()))
                        .thenComparing(OrdemServicoResumo::dataAbertura))
                .toList();
    }
}
