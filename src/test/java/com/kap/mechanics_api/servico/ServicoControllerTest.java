package com.kap.mechanics_api.servico;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.kap.mechanics_api.adapter.web.ServicoWebMapperImpl;
import com.kap.mechanics_api.adapter.web.ServicoController;
import com.kap.mechanics_api.core.servico.domain.Servico;
import com.kap.mechanics_api.core.servico.usecase.AtualizarServicoUseCase;
import com.kap.mechanics_api.core.servico.usecase.BuscarServicoPorIdUseCase;
import com.kap.mechanics_api.core.servico.usecase.CadastrarServicoUseCase;
import com.kap.mechanics_api.core.servico.usecase.ExcluirServicoUseCase;
import com.kap.mechanics_api.core.servico.usecase.ListarServicoUseCase;
import com.kap.mechanics_api.core.servico.usecase.ServicoNaoEncontradoException;

@WebMvcTest(ServicoController.class)
@Import(ServicoWebMapperImpl.class)
@WithMockUser(roles = "ADMIN")
class ServicoControllerTest {

    private static final String ENDPOINT = "/api/servico";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CadastrarServicoUseCase cadastrarServicoUseCase;
    @MockitoBean
    private ListarServicoUseCase listarServicoUseCase;
    @MockitoBean
    private BuscarServicoPorIdUseCase buscarServicoPorIdUseCase;
    @MockitoBean
    private AtualizarServicoUseCase atualizarServicoUseCase;
    @MockitoBean
    private ExcluirServicoUseCase excluirServicoUseCase;

    private Servico alinhamento() {
        return Servico.reconstruir(1, "Alinhamento", "Alinhamento 3D", new BigDecimal("150.00"), 60, true);
    }

    @Test
    void deveCadastrarServicoERetornar201() throws Exception {
        when(cadastrarServicoUseCase.executar("Alinhamento", "Alinhamento 3D", new BigDecimal("150.00"), 60, true))
                .thenReturn(alinhamento());

        mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome":"Alinhamento","descricao":"Alinhamento 3D","valorMaoDeObra":150.00,
                         "tempoEstimadoMin":60,"ativo":true}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Alinhamento"))
                .andExpect(jsonPath("$.descricao").value("Alinhamento 3D"))
                .andExpect(jsonPath("$.valorMaoDeObra").value(150.00))
                .andExpect(jsonPath("$.tempoEstimadoMin").value(60))
                .andExpect(jsonPath("$.ativo").value(true));
    }

    @Test
    void naoDeveCadastrarQuandoRequisicaoInvalida() throws Exception {
        mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome":"","descricao":"d","valorMaoDeObra":-1,"tempoEstimadoMin":60,"ativo":true}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.nome").value("Informe o nome do serviço"));

        verifyNoInteractions(cadastrarServicoUseCase);
    }

    @Test
    void deveListarServicos() throws Exception {
        when(listarServicoUseCase.executar()).thenReturn(List.of(alinhamento(), alinhamento()));

        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nome").value("Alinhamento"));
    }

    @Test
    void deveBuscarServicoPorId() throws Exception {
        when(buscarServicoPorIdUseCase.executar(1)).thenReturn(alinhamento());

        mockMvc.perform(get(ENDPOINT + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tempoEstimadoMin").value(60));
    }

    @Test
    void deveRetornar404QuandoServicoNaoExiste() throws Exception {
        when(buscarServicoPorIdUseCase.executar(9)).thenThrow(new ServicoNaoEncontradoException(9));

        mockMvc.perform(get(ENDPOINT + "/9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Servico não encontrado"))
                .andExpect(jsonPath("$.detail").value("Servico nao encontrado com o id 9"));
    }

    @Test
    void deveAtualizarServico() throws Exception {
        when(atualizarServicoUseCase.executar(1, "Alinhamento premium", null, null, null, null))
                .thenReturn(Servico.reconstruir(1, "Alinhamento premium", "d", BigDecimal.TEN, 60, true));

        mockMvc.perform(patch(ENDPOINT + "/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Alinhamento premium\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Alinhamento premium"));
    }

    @Test
    void naoDeveAtualizarSemNenhumCampo() throws Exception {
        mockMvc.perform(patch(ENDPOINT + "/1").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Nenhum campo informado"));

        verifyNoInteractions(atualizarServicoUseCase);
    }

    @Test
    void deveRetornar400QuandoDominioRejeitaOsDados() throws Exception {
        when(atualizarServicoUseCase.executar(any(), any(), any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("O valor da mão de obra não pode ser negativo"));

        mockMvc.perform(patch(ENDPOINT + "/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Dados inválidos"));
    }

    @Test
    void deveExcluirServicoERetornar204() throws Exception {
        mockMvc.perform(delete(ENDPOINT + "/1"))
                .andExpect(status().isNoContent());

        verify(excluirServicoUseCase).executar(1);
    }
}
