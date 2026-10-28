package com.kap.mechanics_api.core.orcamento.usecase;

import com.kap.mechanics_api.core.cliente.gateway.ClienteGateway;
import com.kap.mechanics_api.core.cliente.usecase.ClienteNaoEncontradoException;
import com.kap.mechanics_api.core.itemestoque.gateway.ItemEstoqueGateway;
import com.kap.mechanics_api.core.itemestoque.usecase.ItemEstoqueNaoEncontradoException;
import com.kap.mechanics_api.core.orcamento.domain.ComponenteOrcado;
import com.kap.mechanics_api.core.orcamento.domain.Orcamento;
import com.kap.mechanics_api.core.orcamento.gateway.OrcamentoGateway;
import com.kap.mechanics_api.core.ordemservico.domain.OrdemServico;
import com.kap.mechanics_api.core.ordemservico.usecase.CriarOrdemServicoParaOrcamentoPendenteUseCase;
import com.kap.mechanics_api.core.servico.domain.Servico;
import com.kap.mechanics_api.core.servico.gateway.ComponentesDeServicoGateway;
import com.kap.mechanics_api.core.servico.gateway.ServicoGateway;
import com.kap.mechanics_api.core.servico.usecase.ServicoNaoEncontradoException;
import com.kap.mechanics_api.core.veiculo.gateway.VeiculoGateway;
import com.kap.mechanics_api.core.veiculo.usecase.VeiculoNaoEncontradoException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Gera um orçamento pendente para os serviços escolhidos e já abre a ordem de serviço inicial.
 * Quem chama deve manter tudo na mesma transação: se a ordem falhar, o orçamento não pode ficar gravado.
 */
public class GerarOrcamentoUseCase {

    private final OrcamentoGateway orcamentoGateway;
    private final ClienteGateway clienteGateway;
    private final VeiculoGateway veiculoGateway;
    private final ServicoGateway servicoGateway;
    private final ItemEstoqueGateway itemEstoqueGateway;
    private final ComponentesDeServicoGateway componentesGateway;
    private final CriarOrdemServicoParaOrcamentoPendenteUseCase criarOrdemServico;
    private final Clock clock;

    public GerarOrcamentoUseCase(OrcamentoGateway orcamentoGateway, ClienteGateway clienteGateway,
                                 VeiculoGateway veiculoGateway, ServicoGateway servicoGateway,
                                 ItemEstoqueGateway itemEstoqueGateway,
                                 ComponentesDeServicoGateway componentesGateway,
                                 CriarOrdemServicoParaOrcamentoPendenteUseCase criarOrdemServico, Clock clock) {
        this.orcamentoGateway = orcamentoGateway;
        this.clienteGateway = clienteGateway;
        this.veiculoGateway = veiculoGateway;
        this.servicoGateway = servicoGateway;
        this.itemEstoqueGateway = itemEstoqueGateway;
        this.componentesGateway = componentesGateway;
        this.criarOrdemServico = criarOrdemServico;
        this.clock = clock;
    }

    public OrcamentoGerado executar(Integer clienteId, Integer veiculoId, List<Integer> servicosIds,
                              String atendenteLogin) {
        clienteGateway.pesquisarPorId(clienteId).orElseThrow(() -> new ClienteNaoEncontradoException(clienteId));
        veiculoGateway.pesquisarPorId(veiculoId).orElseThrow(() -> new VeiculoNaoEncontradoException(veiculoId));

        Orcamento orcamento = Orcamento.novo(clienteId, veiculoId, LocalDateTime.now(clock));
        for (Integer servicoId : servicosIds) {
            Servico servico = servicoGateway.pesquisarPorId(servicoId)
                    .orElseThrow(() -> new ServicoNaoEncontradoException(servicoId));
            orcamento.orcarServico(servicoId, servico.getValorMaoDeObra(), componentesDe(servicoId));
        }

        Orcamento salvo = orcamentoGateway.salvar(orcamento);
        OrdemServico ordemServico = criarOrdemServico.executar(salvo.getId(), atendenteLogin);
        return new OrcamentoGerado(salvo, ordemServico);
    }

    private List<ComponenteOrcado> componentesDe(Integer servicoId) {
        return componentesGateway.listarPorServico(servicoId).stream()
                .map(componente -> new ComponenteOrcado(
                        componente.itemEstoqueId(),
                        componente.quantidadePadrao(),
                        itemEstoqueGateway.pesquisarPorId(componente.itemEstoqueId())
                                .orElseThrow(() -> new ItemEstoqueNaoEncontradoException(componente.itemEstoqueId()))
                                .getValorUnitario()))
                .toList();
    }
}
