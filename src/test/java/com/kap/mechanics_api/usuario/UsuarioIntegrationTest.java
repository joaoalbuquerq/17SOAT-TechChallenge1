package com.kap.mechanics_api.usuario;

import com.kap.mechanics_api.core.usuario.domain.TipoUsuario;
import com.kap.mechanics_api.core.usuario.usecase.CadastrarUsuarioUseCase;
import com.kap.mechanics_api.adapter.persistence.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UsuarioIntegrationTest {
    @Autowired private MockMvc mvc;
    @Autowired private CadastrarUsuarioUseCase cadastrar;
    @Autowired private UsuarioRepository repository;
    @Autowired private PasswordEncoder encoder;
    @Autowired private JsonMapper json;
    @Autowired private EntityManager entityManager;

    private String login(String usuario, String senha) throws Exception {
        var body = json.createObjectNode().put("login", usuario).put("senha", senha);
        var resposta = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(resposta).get("token").asText();
    }

    private String tokenAdmin() throws Exception {
        cadastrar.executar("Admin Integracao", "admin-integracao", "senha-admin", TipoUsuario.ADMIN);
        return login("admin-integracao", "senha-admin");
    }

    private void sincronizar() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void deveCadastrarAtualizarAutenticarEExcluirPelaApi() throws Exception {
        String admin = tokenAdmin();
        mvc.perform(post("/api/usuario").header(HttpHeaders.AUTHORIZATION, "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome":"Usuario Integracao","login":"usuario-integracao","senha":"segredo","tipo":"ATENDENTE"}
                        """))
                .andExpect(status().isCreated());
        sincronizar();
        var usuario = repository.findByLogin("usuario-integracao").orElseThrow();
        Integer id = usuario.getId();
        var data = usuario.getDataCriacao();
        String hash = usuario.getSenhaHash();
        assertTrue(encoder.matches("segredo", hash));
        login("usuario-integracao", "segredo");

        mvc.perform(get("/api/usuario/{id}", id).header(HttpHeaders.AUTHORIZATION, "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("usuario-integracao"))
                .andExpect(jsonPath("$.senha").value(nullValue()))
                .andExpect(jsonPath("$.senhaHash").doesNotExist());
        mvc.perform(put("/api/usuario/{id}", id).header(HttpHeaders.AUTHORIZATION, "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome":"Usuario Atualizado"}
                        """))
                .andExpect(status().isOk());
        sincronizar();
        usuario = repository.findById(id).orElseThrow();
        assertEquals(hash, usuario.getSenhaHash());
        assertEquals(data, usuario.getDataCriacao());

        mvc.perform(put("/api/usuario/{id}", id).header(HttpHeaders.AUTHORIZATION, "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                        {"senha":"novo-segredo","tipo":"ADMIN"}
                        """))
                .andExpect(status().isOk());
        sincronizar();
        assertFalse(encoder.matches("segredo", repository.findById(id).orElseThrow().getSenhaHash()));
        String novoToken = login("usuario-integracao", "novo-segredo");
        // O novo token deve refletir o papel ADMIN persistido na atualizacao.
        mvc.perform(get("/api/usuario").header(HttpHeaders.AUTHORIZATION, "Bearer " + novoToken))
                .andExpect(status().isOk());

        mvc.perform(delete("/api/usuario/{id}", id).header(HttpHeaders.AUTHORIZATION, "Bearer " + admin))
                .andExpect(status().isNoContent());
        sincronizar();
        assertFalse(repository.existsById(id));
        mvc.perform(get("/api/usuario/{id}", id).header(HttpHeaders.AUTHORIZATION, "Bearer " + admin))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveBloquearTodosOsEndpointsParaAtendenteAutenticado() throws Exception {
        cadastrar.executar("Atendente Integracao", "atendente-integracao", "segredo", TipoUsuario.ATENDENTE);
        String token = login("atendente-integracao", "segredo");
        var requisicoes = java.util.List.of(
                get("/api/usuario"), get("/api/usuario/1"), delete("/api/usuario/1"),
                post("/api/usuario").contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome":"Novo","login":"novo","senha":"segredo","tipo":"ADMIN"}
                        """),
                put("/api/usuario/1").contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome":"Alterado"}
                        """));
        for (var requisicao : requisicoes) {
            mvc.perform(requisicao.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void deveExigirAutenticacao() throws Exception {
        mvc.perform(get("/api/usuario")).andExpect(status().isUnauthorized());
    }

    @Test
    void deveRejeitarSenhaIncorreta() throws Exception {
        cadastrar.executar("Login Integracao", "login-integracao", "correta", TipoUsuario.ADMIN);
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                        {"login":"login-integracao","senha":"incorreta"}
                        """))
                .andExpect(status().isUnauthorized());
    }
}
