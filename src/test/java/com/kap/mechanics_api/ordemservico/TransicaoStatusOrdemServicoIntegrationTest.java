package com.kap.mechanics_api.ordemservico;

import com.kap.mechanics_api.adapter.persistence.entity.ClienteJpaEntity;
import com.kap.mechanics_api.adapter.persistence.entity.HistoricoStatusOsJpaEntity;
import com.kap.mechanics_api.adapter.persistence.entity.ItemEstoqueJpaEntity;
import com.kap.mechanics_api.adapter.persistence.entity.MovimentacaoEstoqueJpaEntity;
import com.kap.mechanics_api.adapter.persistence.entity.OrcamentoJpaEntity;
import com.kap.mechanics_api.adapter.persistence.entity.OrcamentoServicoJpaEntity;
import com.kap.mechanics_api.adapter.persistence.entity.OrdemServicoJpaEntity;
import com.kap.mechanics_api.adapter.persistence.entity.ServicoJpaEntity;
import com.kap.mechanics_api.adapter.persistence.entity.UsuarioJpaEntity;
import com.kap.mechanics_api.adapter.persistence.entity.ServicoItemJpaEntity;
import com.kap.mechanics_api.adapter.persistence.entity.ServicoItemJpaId;
import com.kap.mechanics_api.adapter.persistence.entity.StatusOrdemServicoJpaEntity;
import com.kap.mechanics_api.adapter.persistence.entity.VeiculoJpaEntity;
import com.kap.mechanics_api.core.orcamento.domain.StatusOrcamento;
import com.kap.mechanics_api.core.ordemservico.domain.StatusOrdemServico;
import com.kap.mechanics_api.core.itemestoque.domain.TipoItemEstoque;
import com.kap.mechanics_api.core.movimentacaoestoque.domain.TipoMovimentacaoEstoque;
import com.kap.mechanics_api.core.usuario.domain.TipoUsuario;
import com.kap.mechanics_api.core.itemestoque.domain.EstoqueInsuficienteException;
import com.kap.mechanics_api.core.ordemservico.domain.TransicaoStatusInvalidaException;
import com.kap.mechanics_api.adapter.persistence.repository.ClienteRepository;
import com.kap.mechanics_api.adapter.persistence.repository.HistoricoStatusOsRepository;
import com.kap.mechanics_api.adapter.persistence.repository.ItemEstoqueRepository;
import com.kap.mechanics_api.adapter.persistence.repository.MovimentacaoEstoqueRepository;
import com.kap.mechanics_api.adapter.persistence.repository.OrcamentoRepository;
import com.kap.mechanics_api.adapter.persistence.repository.OrcamentoServicoRepository;
import com.kap.mechanics_api.adapter.persistence.repository.OrdemServicoRepository;
import com.kap.mechanics_api.adapter.persistence.repository.ServicoItemRepository;
import com.kap.mechanics_api.adapter.persistence.repository.ServicoRepository;
import com.kap.mechanics_api.adapter.persistence.repository.StatusOrdemServicoRepository;
import com.kap.mechanics_api.repository.UsuarioRepository;
import com.kap.mechanics_api.adapter.persistence.repository.VeiculoRepository;
import com.kap.mechanics_api.core.ordemservico.usecase.TransicionarStatusOrdemServicoUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TransicaoStatusOrdemServicoIntegrationTest {

    @Autowired
    private TransicionarStatusOrdemServicoUseCase transicionarStatusUseCase;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private OrdemServicoRepository ordemServicoRepository;

    @Autowired
    private OrcamentoRepository orcamentoRepository;

    @Autowired
    private OrcamentoServicoRepository orcamentoServicoRepository;

    @Autowired
    private ServicoRepository servicoRepository;

    @Autowired
    private ServicoItemRepository servicoItemRepository;

    @Autowired
    private ItemEstoqueRepository itemEstoqueRepository;

    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    @Autowired
    private StatusOrdemServicoRepository statusOrdemServicoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private VeiculoRepository veiculoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private HistoricoStatusOsRepository historicoStatusOsRepository;

    @BeforeEach
    void limparDadosBaseAntesDoTeste() {
        movimentacaoEstoqueRepository.deleteAll();
        historicoStatusOsRepository.deleteAll();
        ordemServicoRepository.deleteAll();
        orcamentoServicoRepository.deleteAll();
        orcamentoRepository.deleteAll();
        servicoItemRepository.deleteAll();
        itemEstoqueRepository.deleteAll();
        servicoRepository.deleteAll();
        clienteRepository.deleteAll();
        veiculoRepository.deleteAll();
        statusOrdemServicoRepository.deleteAll();
        usuarioRepository.deleteAll();
        seedStatusOrdemServico();
    }

    private void seedStatusOrdemServico() {
        for (StatusOrdemServico valor : StatusOrdemServico.values()) {
            StatusOrdemServicoJpaEntity status = new StatusOrdemServicoJpaEntity();
            status.setNome(valor.name());
            statusOrdemServicoRepository.save(status);
        }
    }

    private StatusOrdemServicoJpaEntity status(StatusOrdemServico valor) {
        return statusOrdemServicoRepository.findByNome(valor.name()).orElseThrow();
    }

    private UsuarioJpaEntity persistirUsuario() {
        UsuarioJpaEntity usuario = new UsuarioJpaEntity();
        usuario.setNome("atendente-teste");
        usuario.setLogin("atendente-teste");
        usuario.setSenhaHash("hash");
        usuario.setTipo(TipoUsuario.ATENDENTE);
        usuario.setDataCriacao(LocalDateTime.now());
        return usuarioRepository.save(usuario);
    }

    private OrcamentoJpaEntity persistirOrcamento() {
        ClienteJpaEntity clienteJpaEntity = clienteRepository.save(
                new ClienteJpaEntity("João Silva", "12345678900", "51999999999", "joao@email.com", LocalDateTime.now()));

        VeiculoJpaEntity veiculo = new VeiculoJpaEntity();
        veiculo.setPlaca("ABC1234");
        veiculo.setMarca("Honda");
        veiculo.setModelo("Civic");
        veiculo.setAno(2020);
        veiculo.setDataCriacao(LocalDateTime.now());
        veiculo = veiculoRepository.save(veiculo);

        OrcamentoJpaEntity orcamento = new OrcamentoJpaEntity();
        orcamento.setCliente(clienteJpaEntity);
        orcamento.setVeiculo(veiculo);
        orcamento.setValorTotal(new BigDecimal("100.00"));
        orcamento.setStatusOrcamento(StatusOrcamento.APROVADO);
        orcamento.setDataCriacao(LocalDateTime.now());
        return orcamentoRepository.save(orcamento);
    }

    private OrdemServicoJpaEntity persistirOrdemServico(OrcamentoJpaEntity orcamento, StatusOrdemServico statusAtual) {
        OrdemServicoJpaEntity ordemServico = new OrdemServicoJpaEntity();
        ordemServico.setOrcamento(orcamento);
        ordemServico.setUsuarioAtendente(persistirUsuario());
        ordemServico.setStatusOrdemServico(status(statusAtual));
        LocalDateTime dataAbertura = LocalDateTime.now();
        ordemServico.setDataAbertura(dataAbertura);
        OrdemServicoJpaEntity ordemServicoSalva = ordemServicoRepository.save(ordemServico);
        historicoStatusOsRepository.save(new HistoricoStatusOsJpaEntity(
                ordemServicoSalva, ordemServicoSalva.getStatusOrdemServico(), dataAbertura));
        return ordemServicoSalva;
    }

    // O controller abre a transação em volta do caso de uso; aqui o teste faz o mesmo.
    private void transicionar(OrdemServicoJpaEntity ordemServico, StatusOrdemServico novoStatus) {
        transactionTemplate.executeWithoutResult(
                tx -> transicionarStatusUseCase.executar(ordemServico.getId(), novoStatus));
    }

    @Test
    @DisplayName("deve transicionar de RECEBIDA para EM_DIAGNOSTICO e persistir o novo status")
    void deveTransicionarDeRecebidaParaEmDiagnostico() {
        OrdemServicoJpaEntity ordemServico =
                persistirOrdemServico(persistirOrcamento(), StatusOrdemServico.RECEBIDA);

        transicionar(ordemServico, StatusOrdemServico.EM_DIAGNOSTICO);

        OrdemServicoJpaEntity recarregada = ordemServicoRepository.findById(ordemServico.getId()).orElseThrow();
        assertThat(recarregada.getStatusOrdemServico().getNome())
                .isEqualTo(StatusOrdemServico.EM_DIAGNOSTICO.name());
        assertThat(recarregada.getStatusOrdemServico().getId()).isNotNull();
    }

    @ParameterizedTest
    @CsvSource({
            "AGUARDANDO_APROVACAO, EM_DIAGNOSTICO",
            "EM_DIAGNOSTICO, EM_EXECUCAO",
            "EM_EXECUCAO, FINALIZADA",
            "FINALIZADA, ENTREGUE"
    })
    @DisplayName("deve permitir todas as transições válidas do fluxo da ordem de serviço")
    void devePermitirTodasAsTransicoesValidasDoFluxo(String statusAtual, String statusDestino) {
        OrdemServicoJpaEntity ordemServico = persistirOrdemServico(
                persistirOrcamento(), StatusOrdemServico.valueOf(statusAtual));

        transicionar(ordemServico, StatusOrdemServico.valueOf(statusDestino));

        assertThat(ordemServicoRepository.findById(ordemServico.getId()).orElseThrow()
                .getStatusOrdemServico().getNome())
                .isEqualTo(statusDestino);
    }

    @Test
    @DisplayName("deve lançar TransicaoStatusInvalidaException ao pular etapas do fluxo")
    void deveLancarExcecaoAoPularEtapas() {
        OrdemServicoJpaEntity ordemServico =
                persistirOrdemServico(persistirOrcamento(), StatusOrdemServico.RECEBIDA);

        assertThatThrownBy(() -> transicionar(ordemServico, StatusOrdemServico.EM_EXECUCAO))
                .isInstanceOf(TransicaoStatusInvalidaException.class)
                .hasMessageContaining("RECEBIDA")
                .hasMessageContaining("EM_EXECUCAO");

        assertThat(ordemServicoRepository.findById(ordemServico.getId()).orElseThrow()
                .getStatusOrdemServico().getNome())
                .isEqualTo(StatusOrdemServico.RECEBIDA.name());
    }

    @Test
    @DisplayName("deve lançar TransicaoStatusInvalidaException ao tentar retroceder no fluxo")
    void deveLancarExcecaoAoRetrocederNoFluxo() {
        OrdemServicoJpaEntity ordemServico =
                persistirOrdemServico(persistirOrcamento(), StatusOrdemServico.EM_EXECUCAO);

        assertThatThrownBy(() -> transicionar(ordemServico, StatusOrdemServico.RECEBIDA))
                .isInstanceOf(TransicaoStatusInvalidaException.class);
    }

    @Test
    @DisplayName("deve lançar TransicaoStatusInvalidaException ao transicionar uma OS já entregue")
    void deveLancarExcecaoAoTransicionarOrdemServicoEntregue() {
        OrdemServicoJpaEntity ordemServico =
                persistirOrdemServico(persistirOrcamento(), StatusOrdemServico.ENTREGUE);

        assertThatThrownBy(() -> transicionar(ordemServico, StatusOrdemServico.FINALIZADA))
                .isInstanceOf(TransicaoStatusInvalidaException.class);
    }

    @Test
    @DisplayName("deve dar baixa no estoque e registrar movimentação de saída ao transicionar para EM_EXECUCAO")
    void deveDarBaixaNoEstoqueAoEntrarEmExecucao() {
        OrcamentoJpaEntity orcamento = persistirOrcamento();

        ServicoJpaEntity servico = servicoRepository.save(
                new ServicoJpaEntity("Troca de pastilhas", "Freios", new BigDecimal("120.00"), 90, true));

        ItemEstoqueJpaEntity item = itemEstoqueRepository.save(new ItemEstoqueJpaEntity(
                "Pastilha de freio", "Peca", TipoItemEstoque.PECA,
                new BigDecimal("45.00"), 10, 1, true));

        ServicoItemJpaEntity servicoItem = new ServicoItemJpaEntity();
        servicoItem.setId(new ServicoItemJpaId(servico.getId(), item.getId()));
        servicoItem.setServico(servico);
        servicoItem.setItemEstoque(item);
        servicoItem.setQuantidadePadrao(3);
        servicoItemRepository.save(servicoItem);

        orcamentoServicoRepository.save(new OrcamentoServicoJpaEntity(orcamento, servico, new BigDecimal("255.00")));

        OrdemServicoJpaEntity ordemServico =
                persistirOrdemServico(orcamento, StatusOrdemServico.EM_DIAGNOSTICO);

        transicionar(ordemServico, StatusOrdemServico.EM_EXECUCAO);

        assertThat(itemEstoqueRepository.findById(item.getId()).orElseThrow().getQuantidadeAtual())
                .isEqualTo(7);

        List<MovimentacaoEstoqueJpaEntity> movimentacoes = movimentacaoEstoqueRepository.findAll();
        assertThat(movimentacoes).hasSize(1);
        MovimentacaoEstoqueJpaEntity saida = movimentacoes.get(0);
        assertThat(saida.getTipo()).isEqualTo(TipoMovimentacaoEstoque.SAIDA);
        assertThat(saida.getQuantidade()).isEqualTo(3);
        assertThat(saida.getItemEstoque().getId()).isEqualTo(item.getId());
        assertThat(saida.getOrdemServico().getId()).isEqualTo(ordemServico.getId());
    }

    @Test
    @DisplayName("deve desfazer status e histórico quando a baixa de estoque falhar")
    void deveDesfazerTransicaoQuandoEstoqueForInsuficiente() {
        OrcamentoJpaEntity orcamento = persistirOrcamento();
        ServicoJpaEntity servico = servicoRepository.save(
                new ServicoJpaEntity("Troca de pastilhas", "Freios", new BigDecimal("120.00"), 90, true));
        ItemEstoqueJpaEntity item = itemEstoqueRepository.save(new ItemEstoqueJpaEntity(
                "Pastilha de freio", "Peca", TipoItemEstoque.PECA,
                new BigDecimal("45.00"), 2, 1, true));

        ServicoItemJpaEntity servicoItem = new ServicoItemJpaEntity();
        servicoItem.setId(new ServicoItemJpaId(servico.getId(), item.getId()));
        servicoItem.setServico(servico);
        servicoItem.setItemEstoque(item);
        servicoItem.setQuantidadePadrao(3);
        servicoItemRepository.save(servicoItem);
        orcamentoServicoRepository.save(
                new OrcamentoServicoJpaEntity(orcamento, servico, new BigDecimal("255.00")));
        OrdemServicoJpaEntity ordemServico =
                persistirOrdemServico(orcamento, StatusOrdemServico.EM_DIAGNOSTICO);

        assertThatThrownBy(() -> transicionar(ordemServico, StatusOrdemServico.EM_EXECUCAO))
                .isInstanceOf(EstoqueInsuficienteException.class);

        OrdemServicoJpaEntity recarregada = ordemServicoRepository.findById(ordemServico.getId()).orElseThrow();
        assertThat(recarregada.getStatusOrdemServico().getNome())
                .isEqualTo(StatusOrdemServico.EM_DIAGNOSTICO.name());
        assertThat(itemEstoqueRepository.findById(item.getId()).orElseThrow().getQuantidadeAtual())
                .isEqualTo(2);
        assertThat(movimentacaoEstoqueRepository.count()).isZero();

        var historico = historicoStatusOsRepository
                .findByOrdemServico_IdOrderByDataHoraInicioAsc(ordemServico.getId());
        assertThat(historico).hasSize(1);
        assertThat(historico.getFirst().getStatus().getNome())
                .isEqualTo(StatusOrdemServico.EM_DIAGNOSTICO.name());
        assertThat(historico.getFirst().getDataHoraFim()).isNull();
    }
}
