package com.kap.mechanics_api.adapter.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.kap.mechanics_api.core.cliente.domain.Cliente;
import com.kap.mechanics_api.core.cliente.domain.CpfCnpj;
import com.kap.mechanics_api.adapter.persistence.repository.ClienteRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ClienteJpaGatewayIntegrationTest {

    private static final LocalDateTime DATA = LocalDateTime.of(2026, 10, 7, 10, 30);

    @Autowired
    private ClienteJpaGateway gateway;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private EntityManager entityManager;

    private void sincronizarComBanco() {
        entityManager.flush();
        entityManager.clear();
    }

    private Cliente novoCliente(String documento) {
        return Cliente.novo("Maria", new CpfCnpj(documento), "11999990000", "maria@email.com", DATA);
    }

    @Test
    void deveSalvarNovoClienteGerandoIdEMantendoDataDoDominio() {

        Cliente salvo = gateway.salvar(novoCliente("12345678909"));

        assertThat(salvo.getId()).isNotNull();
        // prova que o @PrePersist saiu: a data é a que o domínio informou, não "agora"
        assertThat(salvo.getDataCriacao()).isEqualTo(DATA);
    }

    @Test
    void devePreservarTodosOsCamposNaIdaEVoltaPeloBanco() {

        Integer id = gateway.salvar(novoCliente("12345678909")).getId();
        sincronizarComBanco();

        Cliente lido = gateway.pesquisarPorId(id).orElseThrow();

        assertThat(lido.getId()).isEqualTo(id);
        assertThat(lido.getNome()).isEqualTo("Maria");
        assertThat(lido.getCpfCnpj()).isEqualTo(new CpfCnpj("12345678909"));
        assertThat(lido.getTelefone()).isEqualTo("11999990000");
        assertThat(lido.getEmail()).isEqualTo("maria@email.com");
        assertThat(lido.getDataCriacao()).isEqualTo(DATA);
    }

    @Test
    void deveAtualizarClienteExistenteSemCriarRegistroNovo() {

        Integer id = gateway.salvar(novoCliente("12345678909")).getId();
        sincronizarComBanco();
        long totalAntes = clienteRepository.count();

        Cliente existente = gateway.pesquisarPorId(id).orElseThrow();
        existente.atualizar("Maria Souza", null, null, "maria.souza@email.com");
        gateway.salvar(existente);
        sincronizarComBanco();

        // este assert é o que protege contra o bug do id perdido no mapper:
        // sem o id, o save faria INSERT e o total subiria
        assertThat(clienteRepository.count()).isEqualTo(totalAntes);

        Cliente atualizado = gateway.pesquisarPorId(id).orElseThrow();
        assertThat(atualizado.getNome()).isEqualTo("Maria Souza");
        assertThat(atualizado.getEmail()).isEqualTo("maria.souza@email.com");
        assertThat(atualizado.getTelefone()).isEqualTo("11999990000");
        assertThat(atualizado.getDataCriacao()).isEqualTo(DATA);
    }

    @Test
    void deveRetornarVazioQuandoIdNaoExiste() {

        Optional<Cliente> resultado = gateway.pesquisarPorId(-1);

        assertThat(resultado).isEmpty();
    }

    @Test
    void deveBuscarPorDocumentoIndependenteDaFormatacao() {

        Integer id = gateway.salvar(novoCliente("123.456.789-09")).getId();
        sincronizarComBanco();

        Optional<Cliente> comMascara = gateway.buscarPorDocumento(new CpfCnpj("123.456.789-09"));
        Optional<Cliente> semMascara = gateway.buscarPorDocumento(new CpfCnpj("12345678909"));

        assertThat(comMascara).isPresent();
        assertThat(comMascara.get().getId()).isEqualTo(id);
        assertThat(semMascara).isPresent();
        assertThat(semMascara.get().getId()).isEqualTo(id);
    }

    @Test
    void deveRetornarVazioQuandoDocumentoNaoExiste() {

        Optional<Cliente> resultado = gateway.buscarPorDocumento(new CpfCnpj("00000000000"));

        assertThat(resultado).isEmpty();
    }

    @Test
    void deveListarClientesSalvos() {

        Integer idMaria = gateway.salvar(novoCliente("12345678909")).getId();
        Integer idJoao = gateway.salvar(
                Cliente.novo("João", new CpfCnpj("76803621000175"), null, null, DATA)).getId();
        sincronizarComBanco();

        // outros testes podem ter deixado dados no banco compartilhado,
        // por isso verificamos que os nossos estão na lista e não o tamanho exato dela
        assertThat(gateway.listarTodos())
                .extracting(Cliente::getId)
                .contains(idMaria, idJoao);
    }

    @Test
    void deveExcluirCliente() {

        Integer id = gateway.salvar(novoCliente("12345678909")).getId();
        sincronizarComBanco();

        gateway.excluir(id);
        sincronizarComBanco();

        assertThat(gateway.pesquisarPorId(id)).isEmpty();
    }
}
