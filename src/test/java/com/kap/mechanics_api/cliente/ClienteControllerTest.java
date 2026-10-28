package com.kap.mechanics_api.cliente;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kap.mechanics_api.Utilities;
import com.kap.mechanics_api.adapter.web.ClienteController;
import com.kap.mechanics_api.core.cliente.domain.Cliente;
import com.kap.mechanics_api.core.cliente.domain.CpfCnpj;
import com.kap.mechanics_api.core.cliente.usecase.AtualizarClienteUseCase;
import com.kap.mechanics_api.core.cliente.usecase.BuscarClientePorDocumentoUseCase;
import com.kap.mechanics_api.core.cliente.usecase.BuscarClientePorIdUseCase;
import com.kap.mechanics_api.core.cliente.usecase.CadastrarClienteUseCase;
import com.kap.mechanics_api.core.cliente.usecase.ClienteNaoEncontradoException;
import com.kap.mechanics_api.core.cliente.usecase.ExcluirClienteUseCase;
import com.kap.mechanics_api.core.cliente.usecase.ListarClienteUseCase;
import com.kap.mechanics_api.adapter.web.ClienteWebMapperImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClienteController.class)
@Import(ClienteWebMapperImpl.class)
public class ClienteControllerTest {

    private static final LocalDateTime DATA = LocalDateTime.of(2026, 10, 7, 10, 30);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CadastrarClienteUseCase cadastrarClienteUseCase;

    @MockitoBean
    private ListarClienteUseCase listarClienteUseCase;

    @MockitoBean
    private BuscarClientePorIdUseCase buscarClientePorIdUseCase;

    @MockitoBean
    private BuscarClientePorDocumentoUseCase buscarClientePorDocumentoUseCase;

    @MockitoBean
    private AtualizarClienteUseCase atualizarClienteUseCase;

    @MockitoBean
    private ExcluirClienteUseCase excluirClienteUseCase;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String endPoint = "/api/cliente";

    private Cliente joao() {
        return Cliente.reconstruir(1, "João Silva", new CpfCnpj("12345678900"), "51999999999", "joao@email.com", DATA);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveCriarClienteERetornar201() throws Exception {

        when(cadastrarClienteUseCase.executar("João Silva", new CpfCnpj("12345678900"), "51999999999", "joao@email.com"))
                .thenReturn(joao());

        mockMvc.perform(post(endPoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Utilities.produzirClienteRequestDto())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("João Silva"))
                .andExpect(jsonPath("$.cpfCnpj").value("12345678900"))
                .andExpect(jsonPath("$.telefone").value("51999999999"))
                .andExpect(jsonPath("$.email").value("joao@email.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar400QuandoDocumentoInvalidoNoCadastro() throws Exception {

        String json = "{\"nome\":\"João\",\"cpfCnpj\":\"123\",\"telefone\":\"51999999999\",\"email\":\"joao@email.com\"}";

        mockMvc.perform(post(endPoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(cadastrarClienteUseCase);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar200QuandoBuscarClientePorIdExistente() throws Exception {

        when(buscarClientePorIdUseCase.executar(1)).thenReturn(joao());

        mockMvc.perform(get(endPoint + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("João Silva"))
                .andExpect(jsonPath("$.cpfCnpj").value("12345678900"))
                .andExpect(jsonPath("$.telefone").value("51999999999"))
                .andExpect(jsonPath("$.email").value("joao@email.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar200QuandoListarTodosOsClientes() throws Exception {

        Cliente maria = Cliente.reconstruir(2, "Maria Souza", new CpfCnpj("98765432100"), "51988888888", "maria@email.com", DATA);
        when(listarClienteUseCase.executar()).thenReturn(List.of(joao(), maria));

        mockMvc.perform(get(endPoint))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].cpfCnpj").value("98765432100"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar404QuandoNaoEncontradoClientePorId() throws Exception{

        when(buscarClientePorIdUseCase.executar(99999)).thenThrow(new ClienteNaoEncontradoException(99999));

        mockMvc.perform(get(endPoint + "/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar400QuandoNaoPassadoNenhumParametroParaAtualizacao() throws Exception{

        mockMvc.perform(put(endPoint + "/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Utilities.produzirAtualizacaoClienteDtoInvalido())))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(atualizarClienteUseCase);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar204QuandoDeletadoComSucesso() throws Exception{
        doNothing().when(excluirClienteUseCase).executar(999);

        mockMvc.perform(delete(endPoint + "/999"))
                .andExpect(status().isNoContent());

        verify(excluirClienteUseCase).executar(999);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar404QuandoDeletarClienteInexistente() throws Exception{
        org.mockito.Mockito.doThrow(new ClienteNaoEncontradoException(999))
                .when(excluirClienteUseCase).executar(999);

        mockMvc.perform(delete(endPoint + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar200QuandoAtualizadoComSucesso() throws Exception {

        Cliente atualizado = Cliente.reconstruir(99, "João Silva Junior", new CpfCnpj("12345678900"),
                "51988887777", "joaojr@email.com", DATA);
        when(atualizarClienteUseCase.executar(eq(99), eq("João Silva Junior"), eq(new CpfCnpj("12345678900")),
                eq("51988887777"), eq("joaojr@email.com"))).thenReturn(atualizado);

        mockMvc.perform(put(endPoint + "/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Utilities.produzirAtualizacaoClienteDto())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.nome").value("João Silva Junior"))
                .andExpect(jsonPath("$.cpfCnpj").value("12345678900"))
                .andExpect(jsonPath("$.telefone").value("51988887777"))
                .andExpect(jsonPath("$.email").value("joaojr@email.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar200QuandoBuscarClientePorCpf() throws Exception {
        String cpf = "12345678900";

        when(buscarClientePorDocumentoUseCase.executar(new CpfCnpj(cpf))).thenReturn(joao());

        mockMvc.perform(get(endPoint + "/documento/" + cpf))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("João Silva"))
                .andExpect(jsonPath("$.cpfCnpj").value(cpf));

        verify(buscarClientePorDocumentoUseCase).executar(new CpfCnpj(cpf));
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void deveRetornar200QuandoBuscarClientePorCnpj() throws Exception {
        String cnpj = "12345678000190";
        Cliente oficina = Cliente.reconstruir(2, "Oficina Exemplo", new CpfCnpj(cnpj),
                "51988888888", "contato@oficina.com", DATA);

        when(buscarClientePorDocumentoUseCase.executar(new CpfCnpj(cnpj))).thenReturn(oficina);

        mockMvc.perform(get(endPoint + "/documento/" + cnpj))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.nome").value("Oficina Exemplo"))
                .andExpect(jsonPath("$.cpfCnpj").value(cnpj));

        verify(buscarClientePorDocumentoUseCase).executar(new CpfCnpj(cnpj));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar404QuandoClienteNaoForEncontradoPorDocumento() throws Exception {
        String documento = "12345678900";

        when(buscarClientePorDocumentoUseCase.executar(new CpfCnpj(documento)))
                .thenThrow(new ClienteNaoEncontradoException(documento));

        mockMvc.perform(get(endPoint + "/documento/" + documento))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Cliente não encontrado"));

        verify(buscarClientePorDocumentoUseCase).executar(new CpfCnpj(documento));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar400QuandoDocumentoDaBuscaForInvalido() throws Exception {

        mockMvc.perform(get(endPoint + "/documento/123"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(buscarClientePorDocumentoUseCase);
    }
}
