package com.kap.mechanics_api.itemestoque;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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

import com.kap.mechanics_api.adapter.web.ItemEstoqueWebMapperImpl;
import com.kap.mechanics_api.adapter.web.ItemEstoqueController;
import com.kap.mechanics_api.core.itemestoque.domain.ItemEstoque;
import com.kap.mechanics_api.core.itemestoque.domain.TipoItemEstoque;
import com.kap.mechanics_api.core.itemestoque.usecase.AtualizarItemEstoqueUseCase;
import com.kap.mechanics_api.core.itemestoque.usecase.BuscarItemEstoquePorIdUseCase;
import com.kap.mechanics_api.core.itemestoque.usecase.CadastrarItemEstoqueUseCase;
import com.kap.mechanics_api.core.itemestoque.usecase.ExcluirItemEstoqueUseCase;
import com.kap.mechanics_api.core.itemestoque.usecase.ItemEstoqueNaoEncontradoException;
import com.kap.mechanics_api.core.itemestoque.usecase.ListarItemEstoqueUseCase;

@WebMvcTest(ItemEstoqueController.class)
@Import(ItemEstoqueWebMapperImpl.class)
@WithMockUser(roles = "ESTOQUISTA")
class ItemEstoqueControllerTest {

    private static final String ENDPOINT = "/api/item-estoque";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CadastrarItemEstoqueUseCase cadastrarItemEstoqueUseCase;
    @MockitoBean
    private ListarItemEstoqueUseCase listarItemEstoqueUseCase;
    @MockitoBean
    private BuscarItemEstoquePorIdUseCase buscarItemEstoquePorIdUseCase;
    @MockitoBean
    private AtualizarItemEstoqueUseCase atualizarItemEstoqueUseCase;
    @MockitoBean
    private ExcluirItemEstoqueUseCase excluirItemEstoqueUseCase;

    private ItemEstoque filtro(Integer id) {
        return ItemEstoque.reconstruir(id, "Filtro", "Filtro de óleo", TipoItemEstoque.PECA,
                new BigDecimal("25.00"), 10, 2, true);
    }

    @Test
    void deveCadastrarItemERetornar201ComLocation() throws Exception {
        when(cadastrarItemEstoqueUseCase.executar("Filtro", "Filtro de óleo", TipoItemEstoque.PECA,
                new BigDecimal("25.00"), 2, true)).thenReturn(filtro(7));

        mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome":"Filtro","descricao":"Filtro de óleo","tipoItemEstoque":"PECA",
                         "valorUnitario":25.00,"quantidadeMinima":2,"ativo":true}"""))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/item-estoque/7"))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.tipoItemEstoque").value("PECA"))
                .andExpect(jsonPath("$.quantidadeAtual").value(10))
                .andExpect(jsonPath("$.quantidadeMinima").value(2))
                .andExpect(jsonPath("$.ativo").value(true));
    }

    @Test
    void naoDeveCadastrarQuandoRequisicaoInvalida() throws Exception {
        mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome":"","descricao":"d","tipoItemEstoque":"PECA","valorUnitario":1,
                         "quantidadeMinima":2,"ativo":true}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.nome").value("Informe o nome do item"));

        verifyNoInteractions(cadastrarItemEstoqueUseCase);
    }

    @Test
    void deveListarItens() throws Exception {
        when(listarItemEstoqueUseCase.executar()).thenReturn(List.of(filtro(1), filtro(2)));

        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void deveBuscarItemPorId() throws Exception {
        when(buscarItemEstoquePorIdUseCase.executar(3)).thenReturn(filtro(3));

        mockMvc.perform(get(ENDPOINT + "/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3));
    }

    @Test
    void deveRetornar404QuandoItemNaoExiste() throws Exception {
        when(buscarItemEstoquePorIdUseCase.executar(9)).thenThrow(new ItemEstoqueNaoEncontradoException(9));

        mockMvc.perform(get(ENDPOINT + "/9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Item de estoque não encontrado"));
    }

    @Test
    void deveAtualizarItem() throws Exception {
        when(atualizarItemEstoqueUseCase.executar(4, "Novo nome", null, null, null, null, null))
                .thenReturn(filtro(4));

        mockMvc.perform(patch(ENDPOINT + "/4").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Novo nome\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4));
    }

    @Test
    void naoDeveAtualizarSemNenhumCampo() throws Exception {
        mockMvc.perform(patch(ENDPOINT + "/4").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Nenhum campo informado"));

        verifyNoInteractions(atualizarItemEstoqueUseCase);
    }

    @Test
    void deveExcluirItemERetornar204() throws Exception {
        mockMvc.perform(delete(ENDPOINT + "/5"))
                .andExpect(status().isNoContent());

        verify(excluirItemEstoqueUseCase).executar(5);
    }
}
