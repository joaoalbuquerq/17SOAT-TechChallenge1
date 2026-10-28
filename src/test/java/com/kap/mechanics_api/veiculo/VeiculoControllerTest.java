package com.kap.mechanics_api.veiculo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kap.mechanics_api.Utilities;
import com.kap.mechanics_api.adapter.web.VeiculoWebMapperImpl;
import com.kap.mechanics_api.adapter.web.VeiculoController;
import com.kap.mechanics_api.core.cliente.usecase.ClienteNaoEncontradoException;
import com.kap.mechanics_api.core.veiculo.domain.Veiculo;
import com.kap.mechanics_api.core.veiculo.usecase.AtualizarVeiculoUseCase;
import com.kap.mechanics_api.core.veiculo.usecase.BuscarVeiculoPorIdUseCase;
import com.kap.mechanics_api.core.veiculo.usecase.CadastrarVeiculoUseCase;
import com.kap.mechanics_api.core.veiculo.usecase.ExcluirVeiculoUseCase;
import com.kap.mechanics_api.core.veiculo.usecase.ListarVeiculoUseCase;
import com.kap.mechanics_api.core.veiculo.usecase.VeiculoNaoEncontradoException;
import com.kap.mechanics_api.adapter.web.dto.veiculo.CriacaoVeiculoRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import java.time.LocalDateTime;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VeiculoController.class)
@Import(VeiculoWebMapperImpl.class)
@WithMockUser(roles = "ADMIN")
public class VeiculoControllerTest {

    private static final LocalDateTime DATA = LocalDateTime.of(2026, 10, 7, 10, 30);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CadastrarVeiculoUseCase cadastrarVeiculoUseCase;

    @MockitoBean
    private ListarVeiculoUseCase listarVeiculoUseCase;

    @MockitoBean
    private BuscarVeiculoPorIdUseCase buscarVeiculoPorIdUseCase;

    @MockitoBean
    private AtualizarVeiculoUseCase atualizarVeiculoUseCase;

    @MockitoBean
    private ExcluirVeiculoUseCase excluirVeiculoUseCase;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String endPoint = "/api/veiculo";

    private Veiculo civic() {
        return Veiculo.reconstruir(1, "ABC1234", "Honda", "Civic", 2020, DATA);
    }

    @Test
    void deveCriarVeiculoERetornar201() throws Exception {

        when(cadastrarVeiculoUseCase.executar("ABC1234", "Honda", "Civic", 2020, 1)).thenReturn(civic());

        mockMvc.perform(post(endPoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Utilities.produzirVeiculoRequestDto())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/veiculo/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.placa").value("ABC1234"))
                .andExpect(jsonPath("$.marca").value("Honda"))
                .andExpect(jsonPath("$.modelo").value("Civic"))
                .andExpect(jsonPath("$.ano").value(2020));
    }

    @Test
    void deveRetornar400QuandoPlacaInvalidaNaCriacao() throws Exception {
        var dto = new CriacaoVeiculoRequestDTO("123", "Honda", "Civic", 2020, 1);

        mockMvc.perform(post(endPoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(cadastrarVeiculoUseCase);
    }

    @Test
    void deveRetornar404QuandoClienteDaCriacaoNaoExiste() throws Exception {
        when(cadastrarVeiculoUseCase.executar(any(), any(), any(), any(), eq(1)))
                .thenThrow(new ClienteNaoEncontradoException(1));

        mockMvc.perform(post(endPoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Utilities.produzirVeiculoRequestDto())))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar200QuandoBuscarVeiculoPorIdExistente() throws Exception {

        when(buscarVeiculoPorIdUseCase.executar(1)).thenReturn(civic());

        mockMvc.perform(get(endPoint + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.placa").value("ABC1234"))
                .andExpect(jsonPath("$.marca").value("Honda"))
                .andExpect(jsonPath("$.modelo").value("Civic"))
                .andExpect(jsonPath("$.ano").value(2020));
    }

    @Test
    void deveRetornar200QuandoListarTodosOsVeiculos() throws Exception {

        when(listarVeiculoUseCase.executar()).thenReturn(List.of(
                civic(), Veiculo.reconstruir(2, "ADD1234", "Honda", "CRV", 2022, DATA)));

        mockMvc.perform(get(endPoint))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].modelo").value("CRV"));
    }

    @Test
    void deveRetornar404QuandoNaoEncontradoVeiculoPorId() throws Exception {

        when(buscarVeiculoPorIdUseCase.executar(99999)).thenThrow(new VeiculoNaoEncontradoException(99999));

        mockMvc.perform(get(endPoint + "/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar400QuandoNaoPassadoNenhumParametroParaAtualizacao() throws Exception {

        mockMvc.perform(put(endPoint + "/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Utilities.produzirAtualizacaoVeiculoDtoInvalido())))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(atualizarVeiculoUseCase);
    }

    @Test
    void deveRetornar204QuandoDeletadoComSucesso() throws Exception {
        doNothing().when(excluirVeiculoUseCase).executar(999);

        mockMvc.perform(delete(endPoint + "/999"))
                .andExpect(status().isNoContent());

        verify(excluirVeiculoUseCase).executar(999);
    }

    @Test
    void deveRetornar200QuandoAtualizadoComSucesso() throws Exception {

        when(atualizarVeiculoUseCase.executar(99, "ABC1234", "Honda2", "Civic2", 2020, 1))
                .thenReturn(Veiculo.reconstruir(99, "ABC1234", "Honda2", "Civic2", 2020, DATA));

        mockMvc.perform(put(endPoint + "/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Utilities.produzirAtualizacaoVeiculoDto())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.placa").value("ABC1234"))
                .andExpect(jsonPath("$.marca").value("Honda2"))
                .andExpect(jsonPath("$.modelo").value("Civic2"))
                .andExpect(jsonPath("$.ano").value(2020));
    }
}
