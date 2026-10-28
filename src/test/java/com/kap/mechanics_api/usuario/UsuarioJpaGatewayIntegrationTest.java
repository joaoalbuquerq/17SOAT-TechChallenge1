package com.kap.mechanics_api.usuario;

import com.kap.mechanics_api.core.usuario.domain.TipoUsuario;
import com.kap.mechanics_api.core.usuario.domain.Usuario;
import com.kap.mechanics_api.core.usuario.gateway.UsuarioGateway;
import com.kap.mechanics_api.adapter.persistence.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UsuarioJpaGatewayIntegrationTest {
    private static final LocalDateTime DATA = LocalDateTime.of(2026, 1, 1, 10, 0);
    @Autowired private UsuarioGateway gateway;
    @Autowired private UsuarioRepository repository;
    @Autowired private EntityManager entityManager;

    private Usuario novo() {
        var usuario = new Usuario(null, "Usuario Gateway", "usuario-gateway", "hash-original", TipoUsuario.ADMIN);
        usuario.setDataCriacao(DATA);
        return usuario;
    }

    private void sincronizar() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void devePreservarTodosOsCamposNaIdaEVoltaPeloBanco() {
        var salvo = gateway.salvar(novo());
        assertNotNull(salvo.getId());
        sincronizar();

        var lido = gateway.buscarPorId(salvo.getId()).orElseThrow();

        assertEquals(salvo.getId(), lido.getId());
        assertEquals("Usuario Gateway", lido.getNome());
        assertEquals("usuario-gateway", lido.getLogin());
        assertEquals("hash-original", lido.getSenhaHash());
        assertEquals(TipoUsuario.ADMIN, lido.getTipo());
        assertEquals(DATA, lido.getDataCriacao());
    }

    @Test
    void deveAtualizarSemInserirNovoRegistroOuAlterarData() {
        var salvo = gateway.salvar(novo());
        sincronizar();
        long total = repository.count();
        var existente = gateway.buscarPorId(salvo.getId()).orElseThrow();
        existente.setNome("Usuario Atualizado");
        gateway.salvar(existente);
        sincronizar();

        var atualizado = gateway.buscarPorId(salvo.getId()).orElseThrow();
        assertEquals(total, repository.count());
        assertEquals("Usuario Atualizado", atualizado.getNome());
        assertEquals("hash-original", atualizado.getSenhaHash());
        assertEquals(DATA, atualizado.getDataCriacao());
    }

    @Test
    void deveListarUsuariosDoDominio() {
        var salvo = gateway.salvar(novo());
        sincronizar();

        assertTrue(gateway.listarTodos().stream().anyMatch(u ->
                u.getId().equals(salvo.getId()) && u.getLogin().equals("usuario-gateway")));
    }

    @Test
    void deveExcluirRegistroPersistido() {
        var salvo = gateway.salvar(novo());
        sincronizar();

        gateway.excluir(salvo.getId());
        sincronizar();

        assertTrue(gateway.buscarPorId(salvo.getId()).isEmpty());
        assertFalse(repository.existsById(salvo.getId()));
    }

    @Test
    void deveRetornarVazioQuandoIdNaoExiste() {
        assertTrue(gateway.buscarPorId(-1).isEmpty());
    }
}
