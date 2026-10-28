package com.kap.mechanics_api.core.orcamento.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kap.mechanics_api.core.cliente.domain.Cliente;
import com.kap.mechanics_api.core.cliente.gateway.ClienteGateway;
import com.kap.mechanics_api.core.cliente.usecase.ClienteNaoEncontradoException;
import com.kap.mechanics_api.core.itemestoque.domain.ItemEstoque;
import com.kap.mechanics_api.core.itemestoque.domain.TipoItemEstoque;
import com.kap.mechanics_api.core.itemestoque.gateway.ItemEstoqueGateway;
import com.kap.mechanics_api.core.orcamento.domain.Orcamento;
import com.kap.mechanics_api.core.orcamento.domain.StatusOrcamento;
import com.kap.mechanics_api.core.orcamento.gateway.OrcamentoGateway;
import com.kap.mechanics_api.core.ordemservico.domain.OrdemServico;
import com.kap.mechanics_api.core.orcamento.usecase.OrcamentoGerado;
import com.kap.mechanics_api.core.ordemservico.usecase.CriarOrdemServicoParaOrcamentoPendenteUseCase;
import com.kap.mechanics_api.core.servico.domain.Servico;
import com.kap.mechanics_api.core.servico.gateway.ComponenteDeServico;
import com.kap.mechanics_api.core.servico.gateway.ComponentesDeServicoGateway;
import com.kap.mechanics_api.core.servico.gateway.ServicoGateway;
import com.kap.mechanics_api.core.servico.usecase.ServicoNaoEncontradoException;
import com.kap.mechanics_api.core.veiculo.domain.Veiculo;
import com.kap.mechanics_api.core.veiculo.gateway.VeiculoGateway;
import com.kap.mechanics_api.core.veiculo.usecase.VeiculoNaoEncontradoException;

@ExtendWith(MockitoExtension.class)
class GerarOrcamentoUseCaseTest {

    private static final Clock RELOGIO = Clock.fixed(Instant.parse("2026-10-16T12:00:00Z"), ZoneOffset.UTC);

    @Mock private OrcamentoGateway orcamentoGateway;
    @Mock private ClienteGateway clienteGateway;
    @Mock private VeiculoGateway veiculoGateway;
    @Mock private ServicoGateway servicoGateway;
    @Mock private ItemEstoqueGateway itemEstoqueGateway;
    @Mock private ComponentesDeServicoGateway componentesGateway;
    @Mock private CriarOrdemServicoParaOrcamentoPendenteUseCase criarOrdemServico;

    private GerarOrcamentoUseCase useCase;

    @BeforeEach
    void montar() {
        useCase = new GerarOrcamentoUseCase(orcamentoGateway, clienteGateway, veiculoGateway, servicoGateway,
                itemEstoqueGateway, componentesGateway, criarOrdemServico, RELOGIO);
    }

    private void clienteEVeiculoExistem() {
        when(clienteGateway.pesquisarPorId(1)).thenReturn(Optional.of(mock(Cliente.class)));
        when(veiculoGateway.pesquisarPorId(2)).thenReturn(Optional.of(mock(Veiculo.class)));
    }

    private Servico servico(int id, String maoDeObra) {
        return Servico.reconstruir(id, "Serviço " + id, "Descrição", new BigDecimal(maoDeObra), 60, true);
    }

    @Test
    void deveGerarOrcamentoSomandoMaoDeObraEPecasEAbrirAOrdemDeServico() {
        clienteEVeiculoExistem();
        when(servicoGateway.pesquisarPorId(10)).thenReturn(Optional.of(servico(10, "100.00")));
        when(componentesGateway.listarPorServico(10)).thenReturn(List.of(new ComponenteDeServico(7, 2)));
        when(itemEstoqueGateway.pesquisarPorId(7)).thenReturn(Optional.of(ItemEstoque.reconstruir(
                7, "Filtro", "Filtro de óleo", TipoItemEstoque.PECA, new BigDecimal("30.00"), 10, 1, true)));
        when(orcamentoGateway.salvar(any(Orcamento.class))).thenAnswer(inv -> {
            Orcamento o = inv.getArgument(0);
            return Orcamento.reconstruir(55, o.getClienteId(), o.getVeiculoId(), o.getValorTotal(), o.getStatus(),
                    o.getDataCriacao(), null, o.getItens(), o.getServicosOrcados());
        });

        OrdemServico ordemServico = mock(OrdemServico.class);
        when(ordemServico.getId()).thenReturn(77);
        when(criarOrdemServico.executar(55, "atendente")).thenReturn(ordemServico);

        OrcamentoGerado resultado = useCase.executar(1, 2, List.of(10), "atendente");

        ArgumentCaptor<Orcamento> captor = ArgumentCaptor.forClass(Orcamento.class);
        verify(orcamentoGateway).salvar(captor.capture());
        Orcamento gravado = captor.getValue();
        assertEquals(StatusOrcamento.PENDENTE, gravado.getStatus());
        assertEquals(0, new BigDecimal("160.00").compareTo(gravado.getValorTotal()));
        assertEquals(LocalDateTime.of(2026, 10, 16, 12, 0), gravado.getDataCriacao());
        assertEquals(55, resultado.orcamento().getId());
        assertEquals(77, resultado.ordemServico().getId());
        verify(criarOrdemServico).executar(55, "atendente");
    }

    @Test
    void deveGerarOrcamentoSemPecasQuandoOServicoNaoTemComponentes() {
        clienteEVeiculoExistem();
        when(servicoGateway.pesquisarPorId(10)).thenReturn(Optional.of(servico(10, "100.00")));
        when(componentesGateway.listarPorServico(10)).thenReturn(List.of());
        when(orcamentoGateway.salvar(any(Orcamento.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.executar(1, 2, List.of(10), "atendente");

        ArgumentCaptor<Orcamento> captor = ArgumentCaptor.forClass(Orcamento.class);
        verify(orcamentoGateway).salvar(captor.capture());
        assertEquals(0, new BigDecimal("100.00").compareTo(captor.getValue().getValorTotal()));
        assertEquals(1, captor.getValue().getItens().size());
    }

    @Test
    void naoDeveGravarQuandoClienteNaoExiste() {
        when(clienteGateway.pesquisarPorId(1)).thenReturn(Optional.empty());

        assertThrows(ClienteNaoEncontradoException.class, () -> useCase.executar(1, 2, List.of(10), "atendente"));

        verifyNoInteractions(orcamentoGateway, criarOrdemServico);
    }

    @Test
    void naoDeveGravarQuandoVeiculoNaoExiste() {
        when(clienteGateway.pesquisarPorId(1)).thenReturn(Optional.of(mock(Cliente.class)));
        when(veiculoGateway.pesquisarPorId(2)).thenReturn(Optional.empty());

        assertThrows(VeiculoNaoEncontradoException.class, () -> useCase.executar(1, 2, List.of(10), "atendente"));

        verifyNoInteractions(orcamentoGateway, criarOrdemServico);
    }

    @Test
    void naoDeveGravarQuandoServicoNaoExiste() {
        clienteEVeiculoExistem();
        when(servicoGateway.pesquisarPorId(10)).thenReturn(Optional.empty());

        assertThrows(ServicoNaoEncontradoException.class, () -> useCase.executar(1, 2, List.of(10), "atendente"));

        verify(orcamentoGateway, never()).salvar(any());
        verifyNoInteractions(criarOrdemServico);
    }
}
