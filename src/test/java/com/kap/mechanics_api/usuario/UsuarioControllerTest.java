package com.kap.mechanics_api.usuario;

import com.kap.mechanics_api.adapter.web.UsuarioWebMapperImpl;
import com.kap.mechanics_api.adapter.web.UsuarioController;
import com.kap.mechanics_api.core.usuario.domain.TipoUsuario;
import com.kap.mechanics_api.core.usuario.domain.Usuario;
import com.kap.mechanics_api.core.usuario.usecase.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
@Import(UsuarioWebMapperImpl.class)
@WithMockUser(roles = "ADMIN")
class UsuarioControllerTest {
    private static final String ENDPOINT = "/api/usuario";

    @Autowired private MockMvc mockMvc;
    @MockitoBean private BuscarPorIdUseCase buscar;
    @MockitoBean private ListarUsuarioUseCase listar;
    @MockitoBean private CadastrarUsuarioUseCase cadastrar;
    @MockitoBean private AtualizarUsuarioUseCase atualizar;
    @MockitoBean private ExcluirUsuarioUseCase excluir;

    private Usuario usuario() {
        return new Usuario(1, "Pedro", "pedro", "hash-secreto", TipoUsuario.ATENDENTE);
    }

    @Test
    void deveCadastrarERetornar201ComLocation() throws Exception {
        when(cadastrar.executar("Pedro", "pedro", "segredo", TipoUsuario.ATENDENTE)).thenReturn(usuario());

        mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content("""
                {"nome":"Pedro","login":"pedro","senha":"segredo","tipo":"ATENDENTE"}
                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", ENDPOINT + "/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Pedro"))
                .andExpect(jsonPath("$.login").value("pedro"))
                .andExpect(jsonPath("$.tipo").value("ATENDENTE"))
                .andExpect(jsonPath("$.senhaHash").doesNotExist());
    }

    @Test
    void deveRejeitarCadastroSemCamposObrigatorios() throws Exception {
        mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(cadastrar);
    }

    @Test
    void deveRejeitarTipoInvalidoNoCadastro() throws Exception {
        mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content("""
                {"nome":"Pedro","login":"pedro","senha":"segredo","tipo":"INVALIDO"}
                """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(cadastrar);
    }

    @Test
    void deveBuscarUsuarioSemExporHash() throws Exception {
        when(buscar.executar(1)).thenReturn(usuario());

        mockMvc.perform(get(ENDPOINT + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("pedro"))
                .andExpect(jsonPath("$.senha").value(nullValue()))
                .andExpect(jsonPath("$.senhaHash").doesNotExist());
    }

    @Test
    void deveRetornar404QuandoUsuarioNaoExiste() throws Exception {
        when(buscar.executar(99)).thenThrow(new UsuarioNaoEncontradoException(99));

        mockMvc.perform(get(ENDPOINT + "/99")).andExpect(status().isNotFound());
    }

    @Test
    void deveListarUsuariosSemExporHash() throws Exception {
        when(listar.executar()).thenReturn(List.of(usuario()));

        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Pedro"))
                .andExpect(jsonPath("$[0].senha").value(nullValue()))
                .andExpect(jsonPath("$[0].senhaHash").doesNotExist());
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaUsuarios() throws Exception {
        when(listar.executar()).thenReturn(List.of());

        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void deveAtualizarTodosOsCamposConvertendoTipo() throws Exception {
        var atualizado = new Usuario(1, "Maria", "maria", "hash-novo", TipoUsuario.ADMIN);
        when(atualizar.executar(1, "Maria", "maria", "nova", TipoUsuario.ADMIN)).thenReturn(atualizado);

        mockMvc.perform(put(ENDPOINT + "/1").contentType(MediaType.APPLICATION_JSON).content("""
                {"nome":"Maria","login":"maria","senha":"nova","tipo":"ADMIN"}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Maria"))
                .andExpect(jsonPath("$.tipo").value("ADMIN"))
                .andExpect(jsonPath("$.senha").value(nullValue()))
                .andExpect(jsonPath("$.senhaHash").doesNotExist());
        verify(atualizar).executar(1, "Maria", "maria", "nova", TipoUsuario.ADMIN);
    }

    @Test
    void devePermitirAtualizacaoParcial() throws Exception {
        when(atualizar.executar(1, "Pedro", null, null, null)).thenReturn(usuario());

        mockMvc.perform(put(ENDPOINT + "/1").contentType(MediaType.APPLICATION_JSON).content("""
                {"nome":"Pedro"}
                """))
                .andExpect(status().isOk());
        verify(atualizar).executar(1, "Pedro", null, null, null);
    }

    @Test
    void deveRejeitarAtualizacaoSemCampos() throws Exception {
        mockMvc.perform(put(ENDPOINT + "/1").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(atualizar);
    }

    @Test
    void deveRejeitarTipoInvalidoNaAtualizacao() throws Exception {
        mockMvc.perform(put(ENDPOINT + "/1").contentType(MediaType.APPLICATION_JSON).content("""
                {"tipo":"INVALIDO"}
                """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(atualizar);
    }

    @Test
    void deveRetornar404AoAtualizarUsuarioInexistente() throws Exception {
        when(atualizar.executar(99, "Pedro", null, null, null))
                .thenThrow(new UsuarioNaoEncontradoException(99));

        mockMvc.perform(put(ENDPOINT + "/99").contentType(MediaType.APPLICATION_JSON).content("""
                {"nome":"Pedro"}
                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveExcluirERetornar204() throws Exception {
        mockMvc.perform(delete(ENDPOINT + "/1")).andExpect(status().isNoContent());
        verify(excluir).executar(1);
    }

    @Test
    void deveRetornar404AoExcluirUsuarioInexistente() throws Exception {
        doThrow(new UsuarioNaoEncontradoException(99)).when(excluir).executar(99);

        mockMvc.perform(delete(ENDPOINT + "/99")).andExpect(status().isNotFound());
    }
}
