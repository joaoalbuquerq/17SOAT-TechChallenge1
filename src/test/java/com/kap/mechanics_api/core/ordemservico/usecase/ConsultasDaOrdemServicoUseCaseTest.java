package com.kap.mechanics_api.core.ordemservico.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kap.mechanics_api.core.cliente.domain.Cliente;
import com.kap.mechanics_api.core.cliente.domain.CpfCnpj;
import com.kap.mechanics_api.core.cliente.gateway.ClienteGateway;
import com.kap.mechanics_api.core.cliente.usecase.ClienteNaoEncontradoException;
import com.kap.mechanics_api.core.itemestoque.domain.ItemEstoque;
import com.kap.mechanics_api.core.itemestoque.domain.TipoItemEstoque;
import com.kap.mechanics_api.core.itemestoque.gateway.ItemEstoqueGateway;
import com.kap.mechanics_api.core.ordemservico.domain.HistoricoDeStatus;
import com.kap.mechanics_api.core.ordemservico.domain.ItemOrdemServico;
import com.kap.mechanics_api.core.ordemservico.domain.OrdemServico;
import com.kap.mechanics_api.core.ordemservico.domain.OrdemServicoResumo;
import com.kap.mechanics_api.core.ordemservico.domain.StatusOrdemServico;
import com.kap.mechanics_api.core.ordemservico.gateway.OrdemServicoGateway;
import com.kap.mechanics_api.core.servico.domain.Servico;
import com.kap.mechanics_api.core.servico.gateway.ServicoGateway;

@ExtendWith(MockitoExtension.class)
class ConsultasDaOrdemServicoUseCaseTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 10, 16, 9, 0);

    @Mock private OrdemServicoGateway ordemServicoGateway;
    @Mock private ClienteGateway clienteGateway;
    @Mock private ServicoGateway servicoGateway;
    @Mock private ItemEstoqueGateway itemEstoqueGateway;

    private final OrdemServicoResumo resumo =
            new OrdemServicoResumo(9, 5, StatusOrdemServico.EM_EXECUCAO, T0, null);

    // ---------- listagem ----------

    @Test
    void deveListarTodasAsOrdens() {
        when(ordemServicoGateway.listarResumos()).thenReturn(List.of(resumo));

        assertEquals(List.of(resumo), new ListarOrdensServicoUseCase(ordemServicoGateway).executar());
    }

    @Test
    void deveListarOrdensEmAndamentoPorPrioridadeDeStatusEMaisAntigasPrimeiro() {
        OrdemServicoResumo recebida = new OrdemServicoResumo(1, 1, StatusOrdemServico.RECEBIDA, T0, null);
        OrdemServicoResumo diagnostico = new OrdemServicoResumo(2, 2, StatusOrdemServico.EM_DIAGNOSTICO, T0, null);
        OrdemServicoResumo aguardando = new OrdemServicoResumo(3, 3, StatusOrdemServico.AGUARDANDO_APROVACAO, T0, null);
        OrdemServicoResumo execucaoNova = new OrdemServicoResumo(4, 4, StatusOrdemServico.EM_EXECUCAO, T0.plusDays(2), null);
        OrdemServicoResumo execucaoAntiga = new OrdemServicoResumo(5, 5, StatusOrdemServico.EM_EXECUCAO, T0.plusDays(1), null);
        OrdemServicoResumo finalizada = new OrdemServicoResumo(6, 6, StatusOrdemServico.FINALIZADA, T0, T0.plusDays(3));
        OrdemServicoResumo entregue = new OrdemServicoResumo(7, 7, StatusOrdemServico.ENTREGUE, T0, T0.plusDays(4));
        when(ordemServicoGateway.listarResumos()).thenReturn(List.of(
                recebida, finalizada, execucaoNova, diagnostico, entregue, aguardando, execucaoAntiga));

        assertEquals(List.of(execucaoAntiga, execucaoNova, aguardando, diagnostico, recebida),
                new ListarOrdensServicoUseCase(ordemServicoGateway).executar());
    }

    @Test
    void deveConsultarASituacaoAtualDaOrdem() {
        OrdemServico os = OrdemServico.abrir(5, 3, T0);
        when(ordemServicoGateway.pesquisarPorId(9)).thenReturn(Optional.of(os));

        OrdemServicoResumo resultado = new ConsultarOrdemServicoUseCase(ordemServicoGateway).executar(9);

        assertEquals(StatusOrdemServico.AGUARDANDO_APROVACAO, resultado.status());
        assertEquals(5, resultado.orcamentoId());
    }

    @Test
    void deveFalharAoConsultarOrdemInexistente() {
        when(ordemServicoGateway.pesquisarPorId(9)).thenReturn(Optional.empty());

        assertThrows(OrdemServicoNaoEncontradaException.class,
                () -> new ConsultarOrdemServicoUseCase(ordemServicoGateway).executar(9));
    }

    private ListarOrdensServicoPorClienteUseCase listarPorCliente() {
        return new ListarOrdensServicoPorClienteUseCase(ordemServicoGateway, clienteGateway);
    }

    private Cliente cliente(int id) {
        return Cliente.reconstruir(id, "Cliente " + id, new CpfCnpj("12345678900"), "51999999999", "c" + id + "@email.com", T0);
    }

    @Test
    void deveListarPorIdDoCliente() {
        when(clienteGateway.pesquisarPorId(7)).thenReturn(Optional.of(cliente(7)));
        when(ordemServicoGateway.listarResumosPorCliente(7)).thenReturn(List.of(resumo));

        assertEquals(List.of(resumo), listarPorCliente().executar(7, null));
    }

    @Test
    void deveListarPorCpfFormatadoEPorCnpj() {
        when(clienteGateway.buscarPorDocumento(new CpfCnpj("123.456.789-00"))).thenReturn(Optional.of(cliente(7)));
        when(clienteGateway.buscarPorDocumento(new CpfCnpj("12.345.678/0001-90"))).thenReturn(Optional.of(cliente(8)));
        when(ordemServicoGateway.listarResumosPorCliente(7)).thenReturn(List.of(resumo));
        when(ordemServicoGateway.listarResumosPorCliente(8)).thenReturn(List.of());

        assertEquals(List.of(resumo), listarPorCliente().executar(null, "123.456.789-00"));
        assertTrue(listarPorCliente().executar(null, "12.345.678/0001-90").isEmpty());
    }

    @Test
    void deveFalharQuandoOClienteNaoExiste() {
        when(clienteGateway.pesquisarPorId(7)).thenReturn(Optional.empty());
        when(clienteGateway.buscarPorDocumento(new CpfCnpj("12345678900"))).thenReturn(Optional.empty());

        assertThrows(ClienteNaoEncontradoException.class, () -> listarPorCliente().executar(7, null));
        assertThrows(ClienteNaoEncontradoException.class, () -> listarPorCliente().executar(null, "12345678900"));
        verifyNoInteractions(ordemServicoGateway);
    }

    @Test
    void deveExigirUmEApenasUmFiltro() {
        assertThrows(IllegalArgumentException.class, () -> listarPorCliente().executar(null, null));
        assertThrows(IllegalArgumentException.class, () -> listarPorCliente().executar(null, "  "));
        assertThrows(IllegalArgumentException.class, () -> listarPorCliente().executar(7, "12345678900"));
        verifyNoInteractions(ordemServicoGateway, clienteGateway);
    }

    @Test
    void deveRejeitarDocumentoInvalido() {
        assertThrows(IllegalArgumentException.class, () -> listarPorCliente().executar(null, "123abc"));
        assertThrows(IllegalArgumentException.class, () -> listarPorCliente().executar(null, "123"));
        verifyNoInteractions(ordemServicoGateway, clienteGateway);
    }

    // ---------- histórico ----------

    @Test
    void historicoDeveVirEmOrdemCronologica() {
        var tarde = HistoricoDeStatus.reconstruir(2L, StatusOrdemServico.EM_DIAGNOSTICO, T0.plusHours(1), null);
        var cedo = HistoricoDeStatus.reconstruir(1L, StatusOrdemServico.AGUARDANDO_APROVACAO, T0, T0.plusHours(1));
        when(ordemServicoGateway.pesquisarPorId(9)).thenReturn(Optional.of(OrdemServico.reconstruir(
                9, 5, 3, StatusOrdemServico.EM_DIAGNOSTICO, T0, null, List.of(tarde, cedo), List.of())));

        List<HistoricoDeStatus> historico = new ConsultarHistoricoDeStatusUseCase(ordemServicoGateway).executar(9);

        assertEquals(List.of(cedo, tarde), historico);
    }

    @Test
    void historicoDeOrdemInexistenteDeveFalhar() {
        when(ordemServicoGateway.pesquisarPorId(9)).thenReturn(Optional.empty());

        assertThrows(OrdemServicoNaoEncontradaException.class,
                () -> new ConsultarHistoricoDeStatusUseCase(ordemServicoGateway).executar(9));
    }

    // ---------- itens ----------

    @Test
    void itensDevemVirComNomeTipoETotalCalculado() {
        when(ordemServicoGateway.pesquisarPorId(9)).thenReturn(Optional.of(OrdemServico.reconstruir(
                9, 5, 3, StatusOrdemServico.EM_EXECUCAO, T0, null, List.of(),
                List.of(ItemOrdemServico.reconstruir(1, 21, 10, null, 1, new BigDecimal("100.00")),
                        ItemOrdemServico.reconstruir(2, null, null, 7, 3, new BigDecimal("10.00"))))));
        when(servicoGateway.pesquisarPorId(10)).thenReturn(Optional.of(
                Servico.reconstruir(10, "Revisão", "Completa", new BigDecimal("100.00"), 60, true)));
        when(itemEstoqueGateway.pesquisarPorId(7)).thenReturn(Optional.of(ItemEstoque.reconstruir(
                7, "Filtro", "Filtro de óleo", TipoItemEstoque.PECA, new BigDecimal("10.00"), 10, 1, true)));

        ConsultaDeItensDaOrdemServico consulta =
                new ConsultarItensDaOrdemServicoUseCase(ordemServicoGateway, servicoGateway, itemEstoqueGateway)
                        .executar(9);

        assertEquals(5, consulta.orcamentoId());
        assertEquals(StatusOrdemServico.EM_EXECUCAO, consulta.status());
        assertEquals(0, new BigDecimal("130.00").compareTo(consulta.valorTotal()));
        assertEquals(ConsultaDeItensDaOrdemServico.Tipo.SERVICO, consulta.linhas().get(0).tipo());
        assertEquals(21, consulta.linhas().get(0).orcamentoItemId());
        assertEquals("Revisão", consulta.linhas().get(0).nome());
        assertEquals(ConsultaDeItensDaOrdemServico.Tipo.ESTOQUE, consulta.linhas().get(1).tipo());
        assertEquals("Filtro", consulta.linhas().get(1).nome());
    }

    @Test
    void itensDeOrdemInexistenteDevemFalhar() {
        when(ordemServicoGateway.pesquisarPorId(9)).thenReturn(Optional.empty());

        assertThrows(OrdemServicoNaoEncontradaException.class,
                () -> new ConsultarItensDaOrdemServicoUseCase(ordemServicoGateway, servicoGateway,
                        itemEstoqueGateway).executar(9));
    }
}
