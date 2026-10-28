package com.kap.mechanics_api.movimentacaoestoque;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.kap.mechanics_api.adapter.web.MovimentacaoEstoqueWebMapperImpl;
import com.kap.mechanics_api.adapter.web.MovimentacaoEstoqueController;
import com.kap.mechanics_api.core.itemestoque.domain.EstoqueInsuficienteException;
import com.kap.mechanics_api.core.movimentacaoestoque.domain.MovimentacaoEstoque;
import com.kap.mechanics_api.core.movimentacaoestoque.domain.MovimentacaoEstoqueConsulta;
import com.kap.mechanics_api.core.movimentacaoestoque.domain.TipoMovimentacaoEstoque;
import com.kap.mechanics_api.core.movimentacaoestoque.usecase.BuscarMovimentacaoPorIdUseCase;
import com.kap.mechanics_api.core.movimentacaoestoque.usecase.ListarMovimentacoesPorItemUseCase;
import com.kap.mechanics_api.core.movimentacaoestoque.usecase.ListarMovimentacoesPorOrdemServicoUseCase;
import com.kap.mechanics_api.core.movimentacaoestoque.usecase.ListarMovimentacoesPorPeriodoUseCase;
import com.kap.mechanics_api.core.movimentacaoestoque.usecase.ListarMovimentacoesPorTipoUseCase;
import com.kap.mechanics_api.core.movimentacaoestoque.usecase.ListarMovimentacoesUseCase;
import com.kap.mechanics_api.core.movimentacaoestoque.usecase.MovimentacaoEstoqueNaoEncontradaException;
import com.kap.mechanics_api.core.movimentacaoestoque.usecase.PeriodoMovimentacaoInvalidoException;
import com.kap.mechanics_api.core.movimentacaoestoque.usecase.RegistrarEntradaEstoqueUseCase;
import com.kap.mechanics_api.core.movimentacaoestoque.usecase.RegistrarSaidaEstoqueUseCase;
import com.kap.mechanics_api.core.ordemservico.domain.OrdemServicoNaoEstaEmExecucaoException;

@WebMvcTest(MovimentacaoEstoqueController.class)
@Import(MovimentacaoEstoqueWebMapperImpl.class)
@WithMockUser(username = "estoque", roles = "ESTOQUISTA")
class MovimentacaoEstoqueControllerTest {

    private static final String ENDPOINT = "/api/movimentacao-estoque";
    private static final LocalDateTime DATA = LocalDateTime.of(2026, 8, 22, 10, 0);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegistrarEntradaEstoqueUseCase registrarEntradaEstoqueUseCase;
    @MockitoBean
    private RegistrarSaidaEstoqueUseCase registrarSaidaEstoqueUseCase;
    @MockitoBean
    private ListarMovimentacoesUseCase listarMovimentacoesUseCase;
    @MockitoBean
    private ListarMovimentacoesPorItemUseCase listarMovimentacoesPorItemUseCase;
    @MockitoBean
    private ListarMovimentacoesPorOrdemServicoUseCase listarMovimentacoesPorOrdemServicoUseCase;
    @MockitoBean
    private ListarMovimentacoesPorTipoUseCase listarMovimentacoesPorTipoUseCase;
    @MockitoBean
    private ListarMovimentacoesPorPeriodoUseCase listarMovimentacoesPorPeriodoUseCase;
    @MockitoBean
    private BuscarMovimentacaoPorIdUseCase buscarMovimentacaoPorIdUseCase;

    private TestingAuthenticationToken estoquista() {
        return new TestingAuthenticationToken("estoque", null);
    }

    private MovimentacaoEstoqueConsulta entrada(Integer id, int saldo) {
        return new MovimentacaoEstoqueConsulta(MovimentacaoEstoque.reconstruir(
                id, 1, TipoMovimentacaoEstoque.ENTRADA, 10, DATA, 2, null), "Filtro", saldo);
    }

    private MovimentacaoEstoqueConsulta saida(Integer id, int saldo) {
        return new MovimentacaoEstoqueConsulta(MovimentacaoEstoque.reconstruir(
                id, 1, TipoMovimentacaoEstoque.SAIDA, 4, DATA, 2, 99), "Filtro", saldo);
    }

    @Test
    void deveRegistrarEntradaUsandoOLoginDoUsuarioAutenticado() throws Exception {
        when(registrarEntradaEstoqueUseCase.executar(1, 10, "estoque")).thenReturn(entrada(1, 20));

        mockMvc.perform(post(ENDPOINT + "/entrada").principal(estoquista()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemEstoqueId\":1,\"quantidade\":10}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/movimentacao-estoque/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.itemEstoqueId").value(1))
                .andExpect(jsonPath("$.itemEstoqueNome").value("Filtro"))
                .andExpect(jsonPath("$.tipo").value("ENTRADA"))
                .andExpect(jsonPath("$.quantidade").value(10))
                .andExpect(jsonPath("$.usuarioId").value(2))
                .andExpect(jsonPath("$.saldoItemEstoque").value(20));
    }

    @Test
    void deveRegistrarSaida() throws Exception {
        when(registrarSaidaEstoqueUseCase.executar(1, 4, 99, "estoque")).thenReturn(saida(2, 16));

        mockMvc.perform(post(ENDPOINT + "/saida").principal(estoquista()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemEstoqueId\":1,\"quantidade\":4,\"ordemServicoId\":99}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("SAIDA"))
                .andExpect(jsonPath("$.ordemServicoId").value(99))
                .andExpect(jsonPath("$.saldoItemEstoque").value(16));
    }

    @Test
    void naoDeveRegistrarEntradaComQuantidadeInvalida() throws Exception {
        mockMvc.perform(post(ENDPOINT + "/entrada").principal(estoquista()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemEstoqueId\":1,\"quantidade\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.quantidade").value("A quantidade deve ser maior que zero"));

        verifyNoInteractions(registrarEntradaEstoqueUseCase);
    }

    @Test
    void deveRetornar400QuandoEstoqueInsuficiente() throws Exception {
        when(registrarSaidaEstoqueUseCase.executar(1, 40, 99, "estoque"))
                .thenThrow(new EstoqueInsuficienteException(1, 10, 40));

        mockMvc.perform(post(ENDPOINT + "/saida").principal(estoquista()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemEstoqueId\":1,\"quantidade\":40,\"ordemServicoId\":99}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Estoque insuficiente"));
    }

    @Test
    void deveRetornar400QuandoOrdemNaoEstaEmExecucao() throws Exception {
        when(registrarSaidaEstoqueUseCase.executar(1, 4, 99, "estoque"))
                .thenThrow(new OrdemServicoNaoEstaEmExecucaoException(99));

        mockMvc.perform(post(ENDPOINT + "/saida").principal(estoquista()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemEstoqueId\":1,\"quantidade\":4,\"ordemServicoId\":99}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Movimentacao de estoque invalida"));
    }

    @Test
    void deveListarMovimentacoes() throws Exception {
        when(listarMovimentacoesUseCase.executar()).thenReturn(List.of(saida(2, 16), entrada(1, 20)));

        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].tipo").value("SAIDA"));
    }

    @Test
    void deveListarPorItemOrdemETipo() throws Exception {
        when(listarMovimentacoesPorItemUseCase.executar(1)).thenReturn(List.of(entrada(1, 20)));
        when(listarMovimentacoesPorOrdemServicoUseCase.executar(99)).thenReturn(List.of(saida(2, 16)));
        when(listarMovimentacoesPorTipoUseCase.executar(TipoMovimentacaoEstoque.ENTRADA))
                .thenReturn(List.of(entrada(1, 20)));

        mockMvc.perform(get(ENDPOINT + "/item/1")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
        mockMvc.perform(get(ENDPOINT + "/ordem-servico/99")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ordemServicoId").value(99));
        mockMvc.perform(get(ENDPOINT + "/tipo/ENTRADA")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("ENTRADA"));
    }

    @Test
    void deveListarPorPeriodo() throws Exception {
        LocalDateTime inicio = LocalDateTime.of(2026, 8, 1, 0, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 8, 31, 23, 59);
        when(listarMovimentacoesPorPeriodoUseCase.executar(inicio, fim)).thenReturn(List.of(entrada(1, 20)));

        mockMvc.perform(get(ENDPOINT + "/periodo")
                        .param("inicio", "2026-08-01T00:00:00").param("fim", "2026-08-31T23:59:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void deveRetornar400QuandoPeriodoInvertido() throws Exception {
        LocalDateTime inicio = LocalDateTime.of(2026, 9, 1, 0, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 8, 1, 0, 0);
        when(listarMovimentacoesPorPeriodoUseCase.executar(inicio, fim))
                .thenThrow(new PeriodoMovimentacaoInvalidoException(inicio, fim));

        mockMvc.perform(get(ENDPOINT + "/periodo")
                        .param("inicio", "2026-09-01T00:00:00").param("fim", "2026-08-01T00:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Periodo de movimentacao invalido"));
    }

    @Test
    void deveBuscarPorIdERetornar404QuandoNaoExiste() throws Exception {
        when(buscarMovimentacaoPorIdUseCase.executar(3)).thenReturn(entrada(3, 27));
        when(buscarMovimentacaoPorIdUseCase.executar(9)).thenThrow(new MovimentacaoEstoqueNaoEncontradaException(9));

        mockMvc.perform(get(ENDPOINT + "/3")).andExpect(status().isOk())
                .andExpect(jsonPath("$.saldoItemEstoque").value(27));
        mockMvc.perform(get(ENDPOINT + "/9")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Movimentacao de estoque nao encontrada"));
    }
}
