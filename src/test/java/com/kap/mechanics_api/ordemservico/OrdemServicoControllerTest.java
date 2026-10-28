package com.kap.mechanics_api.ordemservico;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.kap.mechanics_api.adapter.web.OrdemServicoWebMapper;
import com.kap.mechanics_api.adapter.web.HistoricoStatusController;
import com.kap.mechanics_api.adapter.web.OrdemServicoController;
import com.kap.mechanics_api.core.cliente.usecase.ClienteNaoEncontradoException;
import com.kap.mechanics_api.core.ordemservico.domain.HistoricoDeStatus;
import com.kap.mechanics_api.core.ordemservico.domain.OrdemServico;
import com.kap.mechanics_api.core.ordemservico.domain.OrdemServicoResumo;
import com.kap.mechanics_api.core.ordemservico.domain.StatusOrdemServico;
import com.kap.mechanics_api.core.ordemservico.domain.TransicaoStatusInvalidaException;
import com.kap.mechanics_api.core.ordemservico.usecase.ConsultaDeItensDaOrdemServico;
import com.kap.mechanics_api.core.ordemservico.usecase.ConsultarHistoricoDeStatusUseCase;
import com.kap.mechanics_api.core.ordemservico.usecase.ConsultarItensDaOrdemServicoUseCase;
import com.kap.mechanics_api.core.ordemservico.usecase.GerarOrdemServicoUseCase;
import com.kap.mechanics_api.core.ordemservico.usecase.IncluirItemNaOrdemServicoUseCase;
import com.kap.mechanics_api.core.ordemservico.usecase.ListarOrdensServicoPorClienteUseCase;
import com.kap.mechanics_api.core.ordemservico.usecase.ConsultarOrdemServicoUseCase;
import com.kap.mechanics_api.core.ordemservico.usecase.ListarOrdensServicoUseCase;
import com.kap.mechanics_api.core.ordemservico.usecase.OrdemServicoNaoEncontradaException;
import com.kap.mechanics_api.core.ordemservico.usecase.TransicionarStatusOrdemServicoUseCase;

@WebMvcTest({OrdemServicoController.class, HistoricoStatusController.class})
@Import(OrdemServicoWebMapper.class)
class OrdemServicoControllerTest {

    private static final String ENDPOINT = "/api/ordem-servico";

    @Autowired private MockMvc mockMvc;

    @MockitoBean private GerarOrdemServicoUseCase gerarOrdemServicoUseCase;
    @MockitoBean private TransicionarStatusOrdemServicoUseCase transicionarStatusUseCase;
    @MockitoBean private IncluirItemNaOrdemServicoUseCase incluirItemUseCase;
    @MockitoBean private ConsultarItensDaOrdemServicoUseCase consultarItensUseCase;
    @MockitoBean private ConsultarOrdemServicoUseCase consultarUseCase;
    @MockitoBean private ListarOrdensServicoUseCase listarUseCase;
    @MockitoBean private ListarOrdensServicoPorClienteUseCase listarPorClienteUseCase;
    @MockitoBean private ConsultarHistoricoDeStatusUseCase consultarHistoricoUseCase;

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 8, 28, 10, 0);

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornarOrdensServicoPorIdDoCliente() throws Exception {
        when(listarPorClienteUseCase.executar(7, null)).thenReturn(List.of(
                new OrdemServicoResumo(9, 11, StatusOrdemServico.EM_EXECUCAO, T0, null)));

        mockMvc.perform(get(ENDPOINT).param("clienteId", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(9))
                .andExpect(jsonPath("$[0].orcamentoId").value(11))
                .andExpect(jsonPath("$[0].status").value("EM_EXECUCAO"));

        verify(listarPorClienteUseCase).executar(7, null);
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void deveRetornarOrdensServicoPorCpfCnpj() throws Exception {
        when(listarPorClienteUseCase.executar(null, "12345678900")).thenReturn(List.of());

        mockMvc.perform(get(ENDPOINT).param("cpfCnpj", "12345678900"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar404QuandoClienteNaoForEncontrado() throws Exception {
        when(listarPorClienteUseCase.executar(99, null)).thenThrow(new ClienteNaoEncontradoException(99));

        mockMvc.perform(get(ENDPOINT).param("clienteId", "99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Cliente não encontrado"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar400QuandoFiltroForInvalido() throws Exception {
        when(listarPorClienteUseCase.executar(null, null))
                .thenThrow(new IllegalArgumentException("Informe o ID do cliente ou CPF/CNPJ"));

        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("deve retornar 200 com a situação atual da ordem ao chamar GET /{id}")
    void deveConsultarOrdemPorId() throws Exception {
        when(consultarUseCase.executar(3)).thenReturn(new OrdemServicoResumo(3, 10, StatusOrdemServico.EM_EXECUCAO,
                LocalDateTime.of(2026, 8, 20, 9, 0), null));

        mockMvc.perform(get(ENDPOINT + "/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.status").value("EM_EXECUCAO"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("deve retornar 404 ao consultar ordem inexistente")
    void deveRetornar404AoConsultarOrdemInexistente() throws Exception {
        when(consultarUseCase.executar(99)).thenThrow(new OrdemServicoNaoEncontradaException(99));

        mockMvc.perform(get(ENDPOINT + "/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("deve retornar 200 com todas as ordens de servico ao chamar GET /listar")
    void deveRetornarTodasAsOrdensServico() throws Exception {
        when(listarUseCase.executar()).thenReturn(List.of(
                new OrdemServicoResumo(1, 10, StatusOrdemServico.AGUARDANDO_APROVACAO,
                        LocalDateTime.of(2026, 8, 20, 9, 0), null),
                new OrdemServicoResumo(2, 20, StatusOrdemServico.FINALIZADA,
                        LocalDateTime.of(2026, 8, 21, 14, 0), LocalDateTime.of(2026, 8, 25, 17, 30))));

        mockMvc.perform(get(ENDPOINT + "/listar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status").value("AGUARDANDO_APROVACAO"))
                .andExpect(jsonPath("$[0].dataAbertura").value("2026-08-20T09:00:00"))
                .andExpect(jsonPath("$[0].dataEntrega").doesNotExist())
                .andExpect(jsonPath("$[1].status").value("FINALIZADA"))
                .andExpect(jsonPath("$[1].dataEntrega").value("2026-08-25T17:30:00"));
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    @DisplayName("deve retornar 200 com lista vazia quando nao houver ordens de servico")
    void deveRetornarListaVaziaQuandoNaoHouverOrdensServico() throws Exception {
        when(listarUseCase.executar()).thenReturn(List.of());

        mockMvc.perform(get(ENDPOINT + "/listar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void deveGerarOrdemServicoRetornando201ComLocation() throws Exception {
        when(gerarOrdemServicoUseCase.executar(5, "maria")).thenReturn(
                OrdemServico.reconstruir(33, 5, 3, StatusOrdemServico.AGUARDANDO_APROVACAO, T0, null, List.of(),
                        List.of()));

        mockMvc.perform(post(ENDPOINT + "/5").principal(new TestingAuthenticationToken("maria", null)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/ordem-servico/33"));
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void deveTransicionarStatus() throws Exception {
        mockMvc.perform(patch(ENDPOINT + "/9/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"EM_EXECUCAO\"}"))
                .andExpect(status().isNoContent());

        verify(transicionarStatusUseCase).executar(9, StatusOrdemServico.EM_EXECUCAO);
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void deveRetornar400QuandoATransicaoEInvalida() throws Exception {
        when(transicionarStatusUseCase.executar(9, StatusOrdemServico.ENTREGUE))
                .thenThrow(new TransicaoStatusInvalidaException("Não é possível ir de RECEBIDA para ENTREGUE"));

        mockMvc.perform(patch(ENDPOINT + "/9/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ENTREGUE\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Transicao de status invalida"));
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void deveRetornar400QuandoOStatusNaoForInformado() throws Exception {
        mockMvc.perform(patch(ENDPOINT + "/9/status").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void deveIncluirItemNaOrdem() throws Exception {
        mockMvc.perform(post(ENDPOINT + "/9/itens").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"servicoId\":10,\"quantidade\":2}"))
                .andExpect(status().isNoContent());

        verify(incluirItemUseCase).executar(9, 10, null, 2);
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void deveRetornar404AoIncluirItemEmOrdemInexistente() throws Exception {
        doThrow(new OrdemServicoNaoEncontradaException(9)).when(incluirItemUseCase).executar(9, null, 7, 1);

        mockMvc.perform(post(ENDPOINT + "/9/itens").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemEstoqueId\":7,\"quantidade\":1}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Ordem de serviço não encontrada"));
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void deveConsultarItensDaOrdem() throws Exception {
        when(consultarItensUseCase.executar(9)).thenReturn(new ConsultaDeItensDaOrdemServico(9, 5,
                StatusOrdemServico.EM_EXECUCAO, new BigDecimal("130.00"), List.of(
                new ConsultaDeItensDaOrdemServico.Linha(1, 21, ConsultaDeItensDaOrdemServico.Tipo.SERVICO, 10,
                        "Revisão", 1, new BigDecimal("100.00"), new BigDecimal("100.00")),
                new ConsultaDeItensDaOrdemServico.Linha(2, null, ConsultaDeItensDaOrdemServico.Tipo.ESTOQUE, 7,
                        "Filtro", 3, new BigDecimal("10.00"), new BigDecimal("30.00")))));

        mockMvc.perform(get(ENDPOINT + "/9/itens"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ordemServicoId").value(9))
                .andExpect(jsonPath("$.orcamentoId").value(5))
                .andExpect(jsonPath("$.status").value("EM_EXECUCAO"))
                .andExpect(jsonPath("$.valorTotal").value(130.00))
                .andExpect(jsonPath("$.itens[0].tipo").value("SERVICO"))
                .andExpect(jsonPath("$.itens[0].orcamentoItemId").value(21))
                .andExpect(jsonPath("$.itens[1].tipo").value("ESTOQUE"))
                .andExpect(jsonPath("$.itens[1].orcamentoItemId").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void deveConsultarOHistoricoDeStatus() throws Exception {
        when(consultarHistoricoUseCase.executar(9)).thenReturn(List.of(
                HistoricoDeStatus.reconstruir(1L, StatusOrdemServico.AGUARDANDO_APROVACAO, T0, T0.plusHours(1)),
                HistoricoDeStatus.reconstruir(2L, StatusOrdemServico.EM_DIAGNOSTICO, T0.plusHours(1), null)));

        mockMvc.perform(get(ENDPOINT + "/9/historico-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status").value("AGUARDANDO_APROVACAO"))
                .andExpect(jsonPath("$[0].dataHoraFim").value("2026-08-28T11:00:00"))
                .andExpect(jsonPath("$[1].status").value("EM_DIAGNOSTICO"))
                .andExpect(jsonPath("$[1].dataHoraFim").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void deveRetornar404AoConsultarHistoricoDeOrdemInexistente() throws Exception {
        when(consultarHistoricoUseCase.executar(9)).thenThrow(new OrdemServicoNaoEncontradaException(9));

        mockMvc.perform(get(ENDPOINT + "/9/historico-status"))
                .andExpect(status().isNotFound());
    }
}
