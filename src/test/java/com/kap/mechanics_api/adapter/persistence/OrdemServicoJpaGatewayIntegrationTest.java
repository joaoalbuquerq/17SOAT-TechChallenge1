package com.kap.mechanics_api.adapter.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
import com.kap.mechanics_api.adapter.persistence.entity.StatusOrdemServicoJpaEntity;
import com.kap.mechanics_api.adapter.persistence.entity.VeiculoJpaEntity;
import com.kap.mechanics_api.core.itemestoque.domain.TipoItemEstoque;
import com.kap.mechanics_api.core.orcamento.domain.Orcamento;
import com.kap.mechanics_api.core.ordemservico.domain.OrdemServico;
import com.kap.mechanics_api.core.ordemservico.domain.OrdemServicoResumo;
import com.kap.mechanics_api.core.ordemservico.domain.StatusOrdemServico;
import com.kap.mechanics_api.adapter.persistence.entity.UsuarioJpaEntity;
import com.kap.mechanics_api.core.usuario.domain.TipoUsuario;
import com.kap.mechanics_api.adapter.persistence.repository.ClienteRepository;
import com.kap.mechanics_api.adapter.persistence.repository.HistoricoStatusOsRepository;
import com.kap.mechanics_api.adapter.persistence.repository.ItemEstoqueRepository;
import com.kap.mechanics_api.adapter.persistence.repository.ServicoRepository;
import com.kap.mechanics_api.adapter.persistence.repository.StatusOrdemServicoRepository;
import com.kap.mechanics_api.repository.UsuarioRepository;
import com.kap.mechanics_api.adapter.persistence.repository.VeiculoRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrdemServicoJpaGatewayIntegrationTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 10, 16, 9, 0);

    @Autowired private OrdemServicoJpaGateway gateway;
    @Autowired private OrcamentoJpaGateway orcamentoGateway;
    @Autowired private EntityManager entityManager;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private VeiculoRepository veiculoRepository;
    @Autowired private ServicoRepository servicoRepository;
    @Autowired private ItemEstoqueRepository itemEstoqueRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private StatusOrdemServicoRepository statusRepository;
    @Autowired private HistoricoStatusOsRepository historicoRepository;

    private Integer clienteId;
    private Integer orcamentoId;
    private Integer atendenteId;
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
        Integer veiculoId = veiculoRepository.save(veiculo).getId();

        var usuario = new UsuarioJpaEntity();
        usuario.setNome("atendente-" + sufixo);
        usuario.setLogin("atendente-" + sufixo);
        usuario.setSenhaHash("hash");
        usuario.setTipo(TipoUsuario.ATENDENTE);
        usuario.setDataCriacao(LocalDateTime.now());
        atendenteId = usuarioRepository.save(usuario).getId();

        servicoId = servicoRepository.save(
                new ServicoJpaEntity("Revisão", "Completa", new BigDecimal("100.00"), 60, true)).getId();
        itemId = itemEstoqueRepository.save(new ItemEstoqueJpaEntity("Filtro", "Óleo", TipoItemEstoque.PECA,
                new BigDecimal("30.00"), 10, 1, true)).getId();

        for (StatusOrdemServico status : StatusOrdemServico.values()) {
            if (statusRepository.findByNome(status.name()).isEmpty()) {
                var entity = new StatusOrdemServicoJpaEntity();
                entity.setNome(status.name());
                statusRepository.save(entity);
            }
        }

        orcamentoId = orcamentoGateway.salvar(Orcamento.novo(clienteId, veiculoId, T0)).getId();
    }

    private void sincronizarComBanco() {
        entityManager.flush();
        entityManager.clear();
    }

    private Integer abrirOrdem() {
        return gateway.salvar(OrdemServico.abrir(orcamentoId, atendenteId, T0)).getId();
    }

    @Test
    void deveSalvarOrdemAbertaComHistoricoInicial() {
        Integer id = abrirOrdem();
        sincronizarComBanco();

        OrdemServico lida = gateway.pesquisarPorId(id).orElseThrow();

        assertThat(id).isNotNull();
        assertThat(lida.getOrcamentoId()).isEqualTo(orcamentoId);
        assertThat(lida.getAtendenteId()).isEqualTo(atendenteId);
        assertThat(lida.getStatus()).isEqualTo(StatusOrdemServico.AGUARDANDO_APROVACAO);
        assertThat(lida.getDataAbertura()).isEqualTo(T0);
        assertThat(lida.getHistorico()).hasSize(1);
        assertThat(lida.getHistorico().getFirst().getId()).isNotNull();
        assertThat(lida.getHistorico().getFirst().getFim()).isNull();
    }

    @Test
    void transicaoDeveEncerrarOPeriodoAnteriorEAbrirOProximoSemDuplicar() {
        Integer id = abrirOrdem();
        sincronizarComBanco();

        OrdemServico carregada = gateway.pesquisarPorId(id).orElseThrow();
        carregada.transicionarPara(StatusOrdemServico.EM_DIAGNOSTICO, T0.plusHours(1));
        gateway.salvar(carregada);
        sincronizarComBanco();

        OrdemServico lida = gateway.pesquisarPorId(id).orElseThrow();
        assertThat(lida.getStatus()).isEqualTo(StatusOrdemServico.EM_DIAGNOSTICO);
        assertThat(lida.getHistorico()).hasSize(2);
        assertThat(lida.getHistorico().get(0).getFim()).isEqualTo(T0.plusHours(1));
        assertThat(lida.getHistorico().get(1).getStatus()).isEqualTo(StatusOrdemServico.EM_DIAGNOSTICO);
        assertThat(lida.getHistorico().get(1).getFim()).isNull();
        assertThat(historicoRepository.findByOrdemServico_IdOrderByDataHoraInicioAsc(id)).hasSize(2);
    }

    @Test
    void deveGravarLinhasNovasUmaUnicaVez() {
        Integer id = abrirOrdem();
        sincronizarComBanco();

        OrdemServico carregada = gateway.pesquisarPorId(id).orElseThrow();
        carregada.incluirServico(servicoId, 1, new BigDecimal("100.00"));
        carregada.incluirItemEstoque(itemId, 2, new BigDecimal("30.00"));
        gateway.salvar(carregada);
        sincronizarComBanco();

        OrdemServico relida = gateway.pesquisarPorId(id).orElseThrow();
        assertThat(relida.getItens()).hasSize(2);
        gateway.salvar(relida);
        sincronizarComBanco();

        OrdemServico finalLida = gateway.pesquisarPorId(id).orElseThrow();
        assertThat(finalLida.getItens()).hasSize(2);
        assertThat(finalLida.getItens()).extracting("itemEstoqueId").contains(itemId);
    }

    @Test
    void deveBuscarPorOrcamentoEInformarSeExiste() {
        Integer id = abrirOrdem();
        sincronizarComBanco();

        assertThat(gateway.pesquisarPorOrcamentoId(orcamentoId)).get().extracting(OrdemServico::getId).isEqualTo(id);
        assertThat(gateway.existePorOrcamentoId(orcamentoId)).isTrue();
        assertThat(gateway.existePorOrcamentoId(-1)).isFalse();
        assertThat(gateway.pesquisarPorId(-1)).isEmpty();
    }

    @Test
    void deveListarResumosGeraisEPorCliente() {
        Integer id = abrirOrdem();
        sincronizarComBanco();

        assertThat(gateway.listarResumos()).extracting(OrdemServicoResumo::id).contains(id);
        assertThat(gateway.listarResumosPorCliente(clienteId))
                .singleElement()
                .satisfies(resumo -> {
                    assertThat(resumo.id()).isEqualTo(id);
                    assertThat(resumo.orcamentoId()).isEqualTo(orcamentoId);
                    assertThat(resumo.status()).isEqualTo(StatusOrdemServico.AGUARDANDO_APROVACAO);
                    assertThat(resumo.dataAbertura()).isEqualTo(T0);
                    assertThat(resumo.dataEntrega()).isNull();
                });
        assertThat(gateway.listarResumosPorCliente(-1)).isEmpty();
    }
}
