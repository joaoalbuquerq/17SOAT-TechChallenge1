package com.kap.mechanics_api.orcamento;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kap.mechanics_api.core.orcamento.domain.Orcamento;
import com.kap.mechanics_api.core.orcamento.usecase.OrcamentoGerado;
import com.kap.mechanics_api.core.ordemservico.domain.OrdemServico;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.kap.mechanics_api.adapter.web.OrcamentoWebMapper;
import com.kap.mechanics_api.adapter.web.OrcamentoController;
import com.kap.mechanics_api.core.orcamento.domain.StatusOrcamento;
import com.kap.mechanics_api.core.orcamento.domain.StatusOrcamentoInvalidoException;
import com.kap.mechanics_api.core.orcamento.usecase.AtualizarStatusDoOrcamentoUseCase;
import com.kap.mechanics_api.core.orcamento.usecase.ConsultaDeItensDoOrcamento;
import com.kap.mechanics_api.core.orcamento.usecase.ConsultarItensDoOrcamentoUseCase;
import com.kap.mechanics_api.core.orcamento.usecase.GerarOrcamentoUseCase;
import com.kap.mechanics_api.core.orcamento.usecase.IncluirItemNoOrcamentoUseCase;
import com.kap.mechanics_api.core.orcamento.usecase.OrcamentoNaoEncontradoException;

@WebMvcTest(OrcamentoController.class)
@Import(OrcamentoWebMapper.class)
@WithMockUser(username = "maria", roles = "ATENDENTE")
class OrcamentoControllerTest {

    private static final String ENDPOINT = "/api/orcamento";

    @Autowired private MockMvc mockMvc;

    @MockitoBean private GerarOrcamentoUseCase gerarOrcamentoUseCase;
    @MockitoBean private AtualizarStatusDoOrcamentoUseCase atualizarStatusUseCase;
    @MockitoBean private IncluirItemNoOrcamentoUseCase incluirItemUseCase;
    @MockitoBean private ConsultarItensDoOrcamentoUseCase consultarItensUseCase;

    @Test
    void deveGerarOrcamentoComOLoginDoUsuarioLogado() throws Exception {
        Orcamento orcamento = mock(Orcamento.class);
        when(orcamento.getId()).thenReturn(5);
        OrdemServico ordemServico = mock(OrdemServico.class);
        when(ordemServico.getId()).thenReturn(9);
        when(gerarOrcamentoUseCase.executar(1, 2, List.of(10, 11), "maria"))
                .thenReturn(new OrcamentoGerado(orcamento, ordemServico));

        mockMvc.perform(post(ENDPOINT + "/gerarOrcamento").principal(new TestingAuthenticationToken("maria", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\":1,\"veiculoId\":2,\"servicosIds\":[10,11]}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/ordem-servico/9"))
                .andExpect(jsonPath("$.orcamentoId").value(5))
                .andExpect(jsonPath("$.ordemServicoId").value(9));

        verify(gerarOrcamentoUseCase).executar(1, 2, List.of(10, 11), "maria");
    }

    @Test
    void deveRejeitarGeracaoSemCamposObrigatorios() throws Exception {
        mockMvc.perform(post(ENDPOINT + "/gerarOrcamento").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\":1}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(gerarOrcamentoUseCase);
    }

    @Test
    void deveAtualizarStatusDoOrcamento() throws Exception {
        mockMvc.perform(patch(ENDPOINT + "/5/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"APROVADO\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("status do orçamento atualizado com sucesso!"));

        verify(atualizarStatusUseCase).executar(5, "APROVADO");
    }

    @Test
    void deveRetornar404QuandoOrcamentoNaoExiste() throws Exception {
        doThrow(new OrcamentoNaoEncontradoException("Orçamento não encontrado"))
                .when(atualizarStatusUseCase).executar(5, "APROVADO");

        mockMvc.perform(patch(ENDPOINT + "/5/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"APROVADO\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Orçamento não encontrado"));
    }

    @Test
    void deveRetornar400QuandoStatusEInvalido() throws Exception {
        doThrow(new StatusOrcamentoInvalidoException("TALVEZ"))
                .when(atualizarStatusUseCase).executar(5, "TALVEZ");

        mockMvc.perform(patch(ENDPOINT + "/5/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"TALVEZ\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Status de orçamento inválido"));
    }

    @Test
    void deveIncluirItemERetornar204() throws Exception {
        mockMvc.perform(post(ENDPOINT + "/5/itens").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemEstoqueId\":7,\"quantidade\":3}"))
                .andExpect(status().isNoContent());

        verify(incluirItemUseCase).executar(5, null, 7, 3);
    }

    @Test
    void deveRejeitarQuantidadeMenorQueUm() throws Exception {
        mockMvc.perform(post(ENDPOINT + "/5/itens").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemEstoqueId\":7,\"quantidade\":0}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(incluirItemUseCase);
    }

    @Test
    void deveConsultarItensDoOrcamento() throws Exception {
        when(consultarItensUseCase.executar(5)).thenReturn(new ConsultaDeItensDoOrcamento(5,
                StatusOrcamento.PENDENTE, new BigDecimal("160.00"), List.of(
                new ConsultaDeItensDoOrcamento.Linha(1, ConsultaDeItensDoOrcamento.Tipo.SERVICO, 10, "Revisão", 1,
                        new BigDecimal("100.00"), new BigDecimal("100.00")),
                new ConsultaDeItensDoOrcamento.Linha(2, ConsultaDeItensDoOrcamento.Tipo.ESTOQUE, 7, "Filtro", 2,
                        new BigDecimal("30.00"), new BigDecimal("60.00")))));

        mockMvc.perform(get(ENDPOINT + "/5/itens"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orcamentoId").value(5))
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.valorTotal").value(160.00))
                .andExpect(jsonPath("$.itens.length()").value(2))
                .andExpect(jsonPath("$.itens[0].tipo").value("SERVICO"))
                .andExpect(jsonPath("$.itens[0].nome").value("Revisão"))
                .andExpect(jsonPath("$.itens[1].tipo").value("ESTOQUE"))
                .andExpect(jsonPath("$.itens[1].valorTotal").value(60.00));
    }
}
