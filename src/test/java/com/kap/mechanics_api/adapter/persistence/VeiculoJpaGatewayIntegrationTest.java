package com.kap.mechanics_api.adapter.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.kap.mechanics_api.adapter.persistence.entity.ClienteJpaEntity;
import com.kap.mechanics_api.adapter.persistence.entity.ClienteVeiculoJpaId;
import com.kap.mechanics_api.core.veiculo.domain.Veiculo;
import com.kap.mechanics_api.adapter.persistence.repository.ClienteRepository;
import com.kap.mechanics_api.adapter.persistence.repository.ClienteVeiculoRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class VeiculoJpaGatewayIntegrationTest {

    private static final LocalDateTime DATA = LocalDateTime.of(2026, 10, 7, 10, 30);

    @Autowired
    private VeiculoJpaGateway gateway;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private ClienteVeiculoRepository clienteVeiculoRepository;
    @Autowired
    private EntityManager entityManager;

    private void sincronizarComBanco() {
        entityManager.flush();
        entityManager.clear();
    }

    private Veiculo novoVeiculo(String placa) {
        return Veiculo.novo(placa, "Honda", "Civic", 2020, DATA);
    }

    private Integer persistirCliente() {
        return clienteRepository.save(new ClienteJpaEntity("Maria", "12345678909", null, null, DATA)).getId();
    }

    @Test
    void deveSalvarNovoVeiculoGerandoIdEMantendoDataDoDominio() {
        Veiculo salvo = gateway.salvar(novoVeiculo("ABC1D23"));

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getDataCriacao()).isEqualTo(DATA);
    }

    @Test
    void devePreservarTodosOsCamposNaIdaEVoltaPeloBanco() {
        Integer id = gateway.salvar(novoVeiculo("ABC1D23")).getId();
        sincronizarComBanco();

        Veiculo lido = gateway.pesquisarPorId(id).orElseThrow();

        assertThat(lido.getPlaca()).isEqualTo("ABC1D23");
        assertThat(lido.getMarca()).isEqualTo("Honda");
        assertThat(lido.getModelo()).isEqualTo("Civic");
        assertThat(lido.getAno()).isEqualTo(2020);
        assertThat(lido.getDataCriacao()).isEqualTo(DATA);
    }

    @Test
    void deveAtualizarVeiculoExistenteSemCriarOutro() {
        Veiculo salvo = gateway.salvar(novoVeiculo("ABC1D23"));
        sincronizarComBanco();
        long antes = gateway.listarTodos().size();

        Veiculo carregado = gateway.pesquisarPorId(salvo.getId()).orElseThrow();
        carregado.atualizar("XYZ9K88", null, null, 2022);
        gateway.salvar(carregado);
        sincronizarComBanco();

        Veiculo lido = gateway.pesquisarPorId(salvo.getId()).orElseThrow();
        assertThat(lido.getPlaca()).isEqualTo("XYZ9K88");
        assertThat(lido.getAno()).isEqualTo(2022);
        assertThat(gateway.listarTodos()).hasSize((int) antes);
    }

    @Test
    void deveListarVeiculosSalvos() {
        gateway.salvar(novoVeiculo("ABC1D23"));
        gateway.salvar(novoVeiculo("DEF4G56"));
        sincronizarComBanco();

        assertThat(gateway.listarTodos()).extracting(Veiculo::getPlaca).contains("ABC1D23", "DEF4G56");
    }

    @Test
    void deveRetornarVazioQuandoIdNaoExiste() {
        assertThat(gateway.pesquisarPorId(999999)).isEqualTo(Optional.empty());
    }

    @Test
    void deveExcluirVeiculo() {
        Integer id = gateway.salvar(novoVeiculo("ABC1D23")).getId();
        sincronizarComBanco();

        gateway.excluir(id);
        sincronizarComBanco();

        assertThat(gateway.pesquisarPorId(id)).isEmpty();
    }

    @Test
    void deveVincularClienteAoVeiculoSemDuplicarOVinculo() {
        Integer veiculoId = gateway.salvar(novoVeiculo("ABC1D23")).getId();
        Integer clienteId = persistirCliente();
        sincronizarComBanco();

        gateway.vincularCliente(veiculoId, clienteId);
        sincronizarComBanco();
        gateway.vincularCliente(veiculoId, clienteId);
        sincronizarComBanco();

        assertThat(clienteVeiculoRepository.existsById(new ClienteVeiculoJpaId(clienteId, veiculoId))).isTrue();
        assertThat(clienteVeiculoRepository.count()).isEqualTo(1);
    }
}
