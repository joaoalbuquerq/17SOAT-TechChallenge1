package com.kap.mechanics_api.adapter.persistence;

import com.kap.mechanics_api.core.servico.gateway.ComponenteDeServico;
import com.kap.mechanics_api.core.servico.gateway.ComponentesDeServicoGateway;
import com.kap.mechanics_api.adapter.persistence.repository.ServicoItemRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ComponentesDeServicoJpaGateway implements ComponentesDeServicoGateway {

    private final ServicoItemRepository servicoItemRepository;

    public ComponentesDeServicoJpaGateway(ServicoItemRepository servicoItemRepository) {
        this.servicoItemRepository = servicoItemRepository;
    }

    @Override
    public List<ComponenteDeServico> listarPorServico(Integer servicoId) {
        return servicoItemRepository.findByServico_Id(servicoId).stream()
                .map(componente -> new ComponenteDeServico(
                        componente.getItemEstoque().getId(), componente.getQuantidadePadrao()))
                .toList();
    }
}
