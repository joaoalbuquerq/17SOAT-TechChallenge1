package com.kap.mechanics_api.infra;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.kap.mechanics_api.core.cliente.gateway.ClienteGateway;
import com.kap.mechanics_api.core.itemestoque.gateway.ItemEstoqueGateway;
import com.kap.mechanics_api.core.movimentacaoestoque.usecase.BaixarItemParaOrdemServicoUseCase;
import com.kap.mechanics_api.core.movimentacaoestoque.usecase.BaixarItensDaOrdemServicoUseCase;
import com.kap.mechanics_api.core.orcamento.gateway.OrcamentoGateway;
import com.kap.mechanics_api.core.ordemservico.gateway.AtendenteGateway;
import com.kap.mechanics_api.core.ordemservico.gateway.OrdemServicoGateway;
import com.kap.mechanics_api.core.ordemservico.usecase.AbridorDeOrdemServico;
import com.kap.mechanics_api.core.ordemservico.usecase.ConsultarHistoricoDeStatusUseCase;
import com.kap.mechanics_api.core.ordemservico.usecase.ConsultarItensDaOrdemServicoUseCase;
import com.kap.mechanics_api.core.ordemservico.usecase.CriarOrdemServicoParaOrcamentoPendenteUseCase;
import com.kap.mechanics_api.core.ordemservico.usecase.FinalizarOrdemServicoPorOrcamentoUseCase;
import com.kap.mechanics_api.core.ordemservico.usecase.GerarOrdemServicoUseCase;
import com.kap.mechanics_api.core.ordemservico.usecase.IncluirItemNaOrdemServicoUseCase;
import com.kap.mechanics_api.core.ordemservico.usecase.IniciarDiagnosticoPorOrcamentoUseCase;
import com.kap.mechanics_api.core.ordemservico.usecase.ListarOrdensServicoPorClienteUseCase;
import com.kap.mechanics_api.core.ordemservico.usecase.ConsultarOrdemServicoUseCase;
import com.kap.mechanics_api.core.ordemservico.usecase.ListarOrdensServicoUseCase;
import com.kap.mechanics_api.core.ordemservico.usecase.TransicionarStatusOrdemServicoUseCase;
import com.kap.mechanics_api.core.servico.gateway.ComponentesDeServicoGateway;
import com.kap.mechanics_api.core.servico.gateway.ServicoGateway;

@Configuration
public class OrdemServicoUseCaseConfig {

    @Bean
    AbridorDeOrdemServico abridorDeOrdemServico(OrdemServicoGateway ordemServicoGateway,
            AtendenteGateway atendenteGateway, Clock clock) {
        return new AbridorDeOrdemServico(ordemServicoGateway, atendenteGateway, clock);
    }

    @Bean
    CriarOrdemServicoParaOrcamentoPendenteUseCase criarOrdemServicoParaOrcamentoPendenteUseCase(
            OrcamentoGateway orcamentoGateway, AbridorDeOrdemServico abridor) {
        return new CriarOrdemServicoParaOrcamentoPendenteUseCase(orcamentoGateway, abridor);
    }

    @Bean
    GerarOrdemServicoUseCase gerarOrdemServicoUseCase(OrcamentoGateway orcamentoGateway,
            AbridorDeOrdemServico abridor) {
        return new GerarOrdemServicoUseCase(orcamentoGateway, abridor);
    }

    @Bean
    TransicionarStatusOrdemServicoUseCase transicionarStatusOrdemServicoUseCase(
            OrdemServicoGateway ordemServicoGateway, OrcamentoGateway orcamentoGateway,
            BaixarItensDaOrdemServicoUseCase baixarItens, Clock clock) {
        return new TransicionarStatusOrdemServicoUseCase(ordemServicoGateway, orcamentoGateway, baixarItens, clock);
    }

    @Bean
    IniciarDiagnosticoPorOrcamentoUseCase iniciarDiagnosticoPorOrcamentoUseCase(
            OrdemServicoGateway ordemServicoGateway, TransicionarStatusOrdemServicoUseCase transicionar) {
        return new IniciarDiagnosticoPorOrcamentoUseCase(ordemServicoGateway, transicionar);
    }

    @Bean
    FinalizarOrdemServicoPorOrcamentoUseCase finalizarOrdemServicoPorOrcamentoUseCase(
            OrdemServicoGateway ordemServicoGateway, Clock clock) {
        return new FinalizarOrdemServicoPorOrcamentoUseCase(ordemServicoGateway, clock);
    }

    @Bean
    IncluirItemNaOrdemServicoUseCase incluirItemNaOrdemServicoUseCase(OrdemServicoGateway ordemServicoGateway,
            ServicoGateway servicoGateway, ItemEstoqueGateway itemEstoqueGateway,
            ComponentesDeServicoGateway componentesGateway, BaixarItemParaOrdemServicoUseCase baixarItem) {
        return new IncluirItemNaOrdemServicoUseCase(ordemServicoGateway, servicoGateway, itemEstoqueGateway,
                componentesGateway, baixarItem);
    }

    @Bean
    ConsultarItensDaOrdemServicoUseCase consultarItensDaOrdemServicoUseCase(
            OrdemServicoGateway ordemServicoGateway, ServicoGateway servicoGateway,
            ItemEstoqueGateway itemEstoqueGateway) {
        return new ConsultarItensDaOrdemServicoUseCase(ordemServicoGateway, servicoGateway, itemEstoqueGateway);
    }

    @Bean
    ConsultarOrdemServicoUseCase consultarOrdemServicoUseCase(OrdemServicoGateway ordemServicoGateway) {
        return new ConsultarOrdemServicoUseCase(ordemServicoGateway);
    }

    @Bean
    ListarOrdensServicoUseCase listarOrdensServicoUseCase(OrdemServicoGateway ordemServicoGateway) {
        return new ListarOrdensServicoUseCase(ordemServicoGateway);
    }

    @Bean
    ListarOrdensServicoPorClienteUseCase listarOrdensServicoPorClienteUseCase(
            OrdemServicoGateway ordemServicoGateway, ClienteGateway clienteGateway) {
        return new ListarOrdensServicoPorClienteUseCase(ordemServicoGateway, clienteGateway);
    }

    @Bean
    ConsultarHistoricoDeStatusUseCase consultarHistoricoDeStatusUseCase(OrdemServicoGateway ordemServicoGateway) {
        return new ConsultarHistoricoDeStatusUseCase(ordemServicoGateway);
    }
}
