package com.kap.mechanics_api.adapter.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.kap.mechanics_api.adapter.persistence.entity.ClienteJpaEntity;
import com.kap.mechanics_api.adapter.persistence.entity.ItemEstoqueJpaEntity;
import com.kap.mechanics_api.adapter.persistence.entity.ServicoJpaEntity;
import com.kap.mechanics_api.adapter.persistence.entity.VeiculoJpaEntity;
import com.kap.mechanics_api.core.itemestoque.domain.TipoItemEstoque;
import com.kap.mechanics_api.core.orcamento.domain.ComponenteOrcado;
import com.kap.mechanics_api.core.orcamento.domain.Orcamento;
import com.kap.mechanics_api.core.orcamento.domain.StatusOrcamento;
import com.kap.mechanics_api.adapter.persistence.repository.ClienteRepository;
import com.kap.mechanics_api.adapter.persistence.repository.ItemEstoqueRepository;
import com.kap.mechanics_api.adapter.persistence.repository.OrcamentoServicoRepository;
import com.kap.mechanics_api.adapter.persistence.repository.ServicoRepository;
import com.kap.mechanics_api.adapter.persistence.repository.VeiculoRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrcamentoJpaGatewayIntegrationTest {

    @Autowired private OrcamentoJpaGateway gateway;
    @Autowired private EntityManager entityManager;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private VeiculoRepository veiculoRepository;
    @Autowired private ServicoRepository servicoRepository;
    @Autowired private ItemEstoqueRepository itemEstoqueRepository;
    @Autowired private OrcamentoServicoRepository orcamentoServicoRepository;

    private Integer clienteId;
    private Integer veiculoId;
    private Integer servicoId;
    private Integer itemId;

    @BeforeEach
    void prepararDados() {
        String sufixo = UUID.randomUUID().toString().replaceAll("\\D", "");
        clienteId = clienteRepository.save(new ClienteJpaEntity("João", (sufixo + "00000000000").substring(0, 11),
                "51999999999", "joao@email.com", LocalDateTime.now())).getId();
        var veiculo = new VeiculoJpaEntity();
        veiculo.setPlaca(("T" + sufixo + "000000").substring(0, 7));
        veiculo.setMarca("Honda");
        veiculo.setModelo("Civic");
        veiculo.setAno(2020);
        veiculo.setDataCriacao(LocalDateTime.now());
        veiculoId = veiculoRepository.save(veiculo).getId();
        servicoId = servicoRepository.save(
                new ServicoJpaEntity("Revisão", "Completa", new BigDecimal("100.00"), 60, true)).getId();
        itemId = itemEstoqueRepository.save(new ItemEstoqueJpaEntity("Filtro", "Óleo", TipoItemEstoque.PECA,
                new BigDecimal("30.00"), 10, 1, true)).getId();
    }

    private void sincronizarComBanco() {
        entityManager.flush();
        entityManager.clear();
    }

    private Orcamento novoComUmServico() {
        Orcamento orcamento = Orcamento.novo(clienteId, veiculoId, LocalDateTime.of(2026, 10, 16, 10, 0));
        orcamento.orcarServico(servicoId, new BigDecimal("100.00"),
                List.of(new ComponenteOrcado(itemId, 2, new BigDecimal("30.00"))));
        return orcamento;
    }

    @Test
    void deveSalvarOrcamentoComLinhasEServicosOrcadosEPreservarTudoNaIdaEVolta() {
        Integer id = gateway.salvar(novoComUmServico()).getId();
        sincronizarComBanco();

        Orcamento lido = gateway.pesquisarPorId(id).orElseThrow();

        assertThat(id).isNotNull();
        assertThat(lido.getClienteId()).isEqualTo(clienteId);
        assertThat(lido.getVeiculoId()).isEqualTo(veiculoId);
        assertThat(lido.getStatus()).isEqualTo(StatusOrcamento.PENDENTE);
        assertThat(lido.getValorTotal()).isEqualByComparingTo("160.00");
        assertThat(lido.getDataCriacao()).isEqualTo(LocalDateTime.of(2026, 10, 16, 10, 0));
        assertThat(lido.getItens()).hasSize(2);
        assertThat(lido.getItens()).allSatisfy(item -> assertThat(item.getId()).isNotNull());
        assertThat(lido.getServicosOrcados()).hasSize(1);
        assertThat(lido.getServicosOrcados().getFirst().valorCobrado()).isEqualByComparingTo("160.00");
    }

    @Test
    void deveAtualizarStatusSemDuplicarLinhasNemServicosOrcados() {
        Integer id = gateway.salvar(novoComUmServico()).getId();
        sincronizarComBanco();

        Orcamento carregado = gateway.pesquisarPorId(id).orElseThrow();
        carregado.alterarStatus(StatusOrcamento.APROVADO, LocalDateTime.of(2026, 10, 17, 8, 0));
        gateway.salvar(carregado);
        sincronizarComBanco();

        Orcamento lido = gateway.pesquisarPorId(id).orElseThrow();
        assertThat(lido.getStatus()).isEqualTo(StatusOrcamento.APROVADO);
        assertThat(lido.getDataResposta()).isEqualTo(LocalDateTime.of(2026, 10, 17, 8, 0));
        assertThat(lido.getItens()).hasSize(2);
        assertThat(orcamentoServicoRepository.findByOrcamento_Id(id)).hasSize(1);
    }

    @Test
    void deveAcrescentarSoAsLinhasNovasAoReSalvar() {
        Integer id = gateway.salvar(novoComUmServico()).getId();
        sincronizarComBanco();

        Orcamento carregado = gateway.pesquisarPorId(id).orElseThrow();
        carregado.incluirItemEstoque(itemId, new BigDecimal("30.00"), 1);
        gateway.salvar(carregado);
        sincronizarComBanco();

        Orcamento lido = gateway.pesquisarPorId(id).orElseThrow();
        assertThat(lido.getItens()).hasSize(3);
        assertThat(lido.getValorTotal()).isEqualByComparingTo("190.00");
    }

    @Test
    void deveRetornarVazioParaOrcamentoInexistente() {
        assertThat(gateway.pesquisarPorId(-1)).isEmpty();
    }
}
