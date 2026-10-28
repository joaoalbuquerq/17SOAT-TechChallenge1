package com.kap.mechanics_api.ordemservico;

import com.kap.mechanics_api.adapter.persistence.entity.UsuarioJpaEntity;
import com.kap.mechanics_api.adapter.persistence.entity.ClienteJpaEntity;
import com.kap.mechanics_api.adapter.persistence.entity.HistoricoStatusOsJpaEntity;
import com.kap.mechanics_api.adapter.persistence.entity.OrcamentoJpaEntity;
import com.kap.mechanics_api.adapter.persistence.entity.OrdemServicoJpaEntity;
import com.kap.mechanics_api.adapter.persistence.entity.StatusOrdemServicoJpaEntity;
import com.kap.mechanics_api.adapter.persistence.entity.VeiculoJpaEntity;
import com.kap.mechanics_api.adapter.web.dto.ordemservico.AtualizacaoStatusOrdemServicoRequestDTO;
import com.kap.mechanics_api.core.orcamento.domain.StatusOrcamento;
import com.kap.mechanics_api.core.ordemservico.domain.StatusOrdemServico;
import com.kap.mechanics_api.core.usuario.domain.TipoUsuario;
import com.kap.mechanics_api.adapter.persistence.repository.*;
import com.kap.mechanics_api.adapter.persistence.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrdemServicoIntegrationTest {

    private static final String ENDPOINT = "/api/ordem-servico";
    private static final String LOGIN_ATENDENTE = "atendente-teste";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @Autowired
    private OrdemServicoRepository ordemServicoRepository;

    @Autowired
    private OrcamentoRepository orcamentoRepository;

    @Autowired
    private OrcamentoServicoRepository orcamentoServicoRepository;

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

    @Autowired
    private ServicoRepository servicoRepository;

    @Autowired
    private ItemEstoqueRepository itemEstoqueRepository;

    @Autowired
    private ServicoItemRepository servicoItemRepository;

    @Autowired
    private OrcamentoItemRepository orcamentoItemRepository;

    @BeforeEach
    void limparDadosBaseAntesDoTeste() {
        servicoItemRepository.deleteAll();
        orcamentoItemRepository.deleteAll();
        movimentacaoEstoqueRepository.deleteAll();
        itemEstoqueRepository.deleteAll();
        historicoStatusOsRepository.deleteAll();
        ordemServicoRepository.deleteAll();
        orcamentoServicoRepository.deleteAll();
        orcamentoRepository.deleteAll();
        servicoRepository.deleteAll();
        clienteRepository.deleteAll();
        veiculoRepository.deleteAll();
        statusOrdemServicoRepository.deleteAll();
        usuarioRepository.deleteAll();

    }

    private RequestPostProcessor definirRole(String role) {
        return jwt()
                .jwt(jwt -> jwt.subject(LOGIN_ATENDENTE))
                .authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }

    private RequestPostProcessor atendente() {
        return definirRole("ATENDENTE");
    }

    private RequestPostProcessor admin() {
        return definirRole("ADMIN");
    }

    private void seedStatusOrdemServico() {
        for (StatusOrdemServico valor : StatusOrdemServico.values()) {
            StatusOrdemServicoJpaEntity status = new StatusOrdemServicoJpaEntity();
            status.setNome(valor.name());
            statusOrdemServicoRepository.save(status);
        }
    }

    private StatusOrdemServicoJpaEntity buscarStatus(StatusOrdemServico valor) {
        return statusOrdemServicoRepository.findByNome(valor.name()).orElseThrow();
    }

    private UsuarioJpaEntity persistirUsuario() {
        UsuarioJpaEntity usuario = new UsuarioJpaEntity();
        usuario.setNome(LOGIN_ATENDENTE);
        usuario.setLogin(LOGIN_ATENDENTE);
        usuario.setSenhaHash("hash");
        usuario.setTipo(TipoUsuario.ATENDENTE);
        usuario.setDataCriacao(LocalDateTime.now());
        return usuarioRepository.save(usuario);
    }

    private OrcamentoJpaEntity persistirOrcamento(StatusOrcamento statusOrcamento) {
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
        orcamento.setValorTotal(new java.math.BigDecimal("100.00"));
        orcamento.setStatusOrcamento(statusOrcamento);
        orcamento.setDataCriacao(LocalDateTime.now());
        return orcamentoRepository.save(orcamento);
    }

    private OrdemServicoJpaEntity persistirOrdemServico(OrcamentoJpaEntity orcamento, UsuarioJpaEntity usuario, StatusOrdemServico statusAtual) {
        OrdemServicoJpaEntity ordemServico = new OrdemServicoJpaEntity();
        ordemServico.setOrcamento(orcamento);
        ordemServico.setUsuarioAtendente(usuario);
        ordemServico.setStatusOrdemServico(buscarStatus(statusAtual));
        LocalDateTime dataAbertura = LocalDateTime.now();
        ordemServico.setDataAbertura(dataAbertura);
        OrdemServicoJpaEntity ordemServicoSalva = ordemServicoRepository.save(ordemServico);
        historicoStatusOsRepository.save(new HistoricoStatusOsJpaEntity(
                ordemServicoSalva, ordemServicoSalva.getStatusOrdemServico(), dataAbertura));
        return ordemServicoSalva;
    }

    @Test
    @DisplayName("deve gerar a OS para um orçamento aprovado e retornar 201 com o cabeçalho Location")
    void deveGerarOrdemServicoParaOrcamentoAprovado() throws Exception {
        seedStatusOrdemServico();
        UsuarioJpaEntity usuario = persistirUsuario();
        OrcamentoJpaEntity orcamento = persistirOrcamento(StatusOrcamento.APROVADO);

        mockMvc.perform(post(ENDPOINT + "/" + orcamento.getId()).with(atendente()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/ordem-servico/")));

        assertThat(ordemServicoRepository.findAll()).hasSize(1);
        OrdemServicoJpaEntity gerada = ordemServicoRepository.findByOrcamento_Id(orcamento.getId()).orElseThrow();
        assertThat(gerada.getStatusOrdemServico().getNome())
                .isEqualTo(StatusOrdemServico.AGUARDANDO_APROVACAO.name());
        assertThat(gerada.getUsuarioAtendente().getId()).isEqualTo(usuario.getId());

        var historico = historicoStatusOsRepository
                .findByOrdemServico_IdOrderByDataHoraInicioAsc(gerada.getId());
        assertThat(historico).hasSize(1);
        assertThat(historico.getFirst().getStatus().getNome())
                .isEqualTo(StatusOrdemServico.AGUARDANDO_APROVACAO.name());
        assertThat(historico.getFirst().getDataHoraInicio()).isEqualTo(gerada.getDataAbertura());
        assertThat(historico.getFirst().getDataHoraFim()).isNull();
    }

    @Test
    @DisplayName("deve retornar 404 ao gerar a OS para um orçamento que não está aprovado")
    void deveRetornar404AoGerarOrdemServicoParaOrcamentoNaoAprovado() throws Exception {
        seedStatusOrdemServico();
        persistirUsuario();
        OrcamentoJpaEntity orcamento = persistirOrcamento(StatusOrcamento.PENDENTE);

        mockMvc.perform(post(ENDPOINT + "/" + orcamento.getId()).with(atendente()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Orçamento precisa estar aprovado para gerar OS"));

        assertThat(ordemServicoRepository.count()).isZero();
    }

    @Test
    @DisplayName("deve retornar 404 ao gerar a OS para um orçamento inexistente")
    void deveRetornar404AoGerarOrdemServicoParaOrcamentoInexistente() throws Exception {
        seedStatusOrdemServico();
        persistirUsuario();

        mockMvc.perform(post(ENDPOINT + "/999999").with(atendente()))
                .andExpect(status().isNotFound());

        assertThat(ordemServicoRepository.count()).isZero();
    }

    @Test
    @DisplayName("deve retornar 404 ao gerar a OS quando o orçamento já possui uma OS")
    void deveRetornar404AoGerarOrdemServicoQuandoJaExiste() throws Exception {
        seedStatusOrdemServico();
        UsuarioJpaEntity usuario = persistirUsuario();
        OrcamentoJpaEntity orcamento = persistirOrcamento(StatusOrcamento.APROVADO);
        persistirOrdemServico(orcamento, usuario, StatusOrdemServico.AGUARDANDO_APROVACAO);

        mockMvc.perform(post(ENDPOINT + "/" + orcamento.getId()).with(atendente()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Este orçamento já possui uma OS gerada"));

        assertThat(ordemServicoRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("deve transicionar a OS de AGUARDANDO_APROVACAO para EM_DIAGNOSTICO e retornar 204")
    void deveTransicionarStatusDaOrdemServico() throws Exception {
        seedStatusOrdemServico();
        UsuarioJpaEntity usuario = persistirUsuario();
        OrcamentoJpaEntity orcamento = persistirOrcamento(StatusOrcamento.APROVADO);
        OrdemServicoJpaEntity ordemServico =
                persistirOrdemServico(orcamento, usuario, StatusOrdemServico.AGUARDANDO_APROVACAO);

        AtualizacaoStatusOrdemServicoRequestDTO request =
                new AtualizacaoStatusOrdemServicoRequestDTO(StatusOrdemServico.EM_DIAGNOSTICO);

        mockMvc.perform(patch(ENDPOINT + "/" + ordemServico.getId() + "/status")
                        .with(atendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        assertThat(ordemServicoRepository.findById(ordemServico.getId()).orElseThrow()
                .getStatusOrdemServico().getNome())
                .isEqualTo(StatusOrdemServico.EM_DIAGNOSTICO.name());

        var historico = historicoStatusOsRepository
                .findByOrdemServico_IdOrderByDataHoraInicioAsc(ordemServico.getId());
        assertThat(historico).hasSize(2);
        assertThat(historico.get(0).getDataHoraFim()).isEqualTo(historico.get(1).getDataHoraInicio());
        assertThat(historico.get(1).getStatus().getNome())
                .isEqualTo(StatusOrdemServico.EM_DIAGNOSTICO.name());
        assertThat(historico.get(1).getDataHoraFim()).isNull();
    }

//    @Test
//    @DisplayName("deve consultar o histórico de status pelo ID da OS")
//    void deveConsultarHistoricoStatusPorOrdemServico() throws Exception {
//        seedStatusOrdemServico();
//        Usuario usuario = persistirUsuario();
//        OrcamentoJpaEntity orcamento = persistirOrcamento(StatusOrcamento.APROVADO);
//        OrdemServicoJpaEntity ordemServico = persistirOrdemServico(
//                orcamento, usuario, StatusOrdemServico.AGUARDANDO_APROVACAO);
//
//        AtualizacaoStatusOrdemServicoRequestDTO request =
//                new AtualizacaoStatusOrdemServicoRequestDTO(StatusOrdemServico.EM_DIAGNOSTICO);
//        mockMvc.perform(patch(ENDPOINT + "/" + ordemServico.getId() + "/status")
//                        .with(atendente())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isNoContent());
//
//        mockMvc.perform(get(ENDPOINT + "/" + ordemServico.getId() + "/historico-status")
//                        .with(atendente()))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.length()").value(2))
//                .andExpect(jsonPath("$[0].status")
//                        .value(StatusOrdemServico.AGUARDANDO_APROVACAO.name()))
//                .andExpect(jsonPath("$[0].dataHoraFim").isNotEmpty())
//                .andExpect(jsonPath("$[1].status")
//                        .value(StatusOrdemServico.EM_DIAGNOSTICO.name()))
//                .andExpect(jsonPath("$[1].dataHoraFim").isEmpty());
//    }

//    @Test
//    @DisplayName("deve calcular o tempo médio da OS para o serviço informado")
//    void deveCalcularTempoMedioExecucaoPorServico() throws Exception {
//        seedStatusOrdemServico();
//        Usuario usuario = persistirUsuario();
//        OrcamentoJpaEntity orcamento = persistirOrcamento(StatusOrcamento.APROVADO);
//        ServicoJpaEntity trocaOleo = servicoRepository.save(new ServicoJpaEntity(
//                "Troca de óleo", "Troca", new BigDecimal("100.00"), 60, true));
//        ServicoJpaEntity alinhamento = servicoRepository.save(new ServicoJpaEntity(
//                "Alinhamento", "Alinhamento", new BigDecimal("80.00"), 30, true));
//        orcamentoServicoRepository.save(
//                new OrcamentoServicoJpaEntity(orcamento, trocaOleo, new BigDecimal("100.00")));
//        orcamentoServicoRepository.save(
//                new OrcamentoServicoJpaEntity(orcamento, alinhamento, new BigDecimal("80.00")));
//        OrdemServicoJpaEntity ordemServico =
//                persistirOrdemServico(orcamento, usuario, StatusOrdemServico.FINALIZADA);
//
//        LocalDateTime inicio = LocalDateTime.of(2026, 8, 29, 10, 0);
//        HistoricoStatusOsJpaEntity execucao = new HistoricoStatusOsJpaEntity(
//                ordemServico, buscarStatus(StatusOrdemServico.EM_EXECUCAO), inicio);
//        execucao.setDataHoraFim(inicio.plusMinutes(90));
//        historicoStatusOsRepository.save(execucao);
//
//        mockMvc.perform(get("/api/indicadores/tempo-medio-execucao/por-servico/{servicoId}",
//                                trocaOleo.getId())
//                        .with(admin()))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.servicoId").value(trocaOleo.getId()))
//                .andExpect(jsonPath("$.servicoNome").value("Troca de óleo"))
//                .andExpect(jsonPath("$.tempoMedioMinutos").value(90.00))
//                .andExpect(jsonPath("$.quantidadeOsConsideradas").value(1));
//
//        mockMvc.perform(get("/api/indicadores/tempo-medio-execucao/por-servico/{servicoId}",
//                                alinhamento.getId())
//                        .with(atendente()))
//                .andExpect(status().isForbidden());
//    }

    @Test
    @DisplayName("deve transicionar de EM_DIAGNOSTICO para EM_EXECUCAO acionando a baixa de estoque mesmo sem itens orçados")
    void deveTransicionarParaEmExecucaoSemItensOrcados() throws Exception {
        seedStatusOrdemServico();
        UsuarioJpaEntity usuario = persistirUsuario();
        OrcamentoJpaEntity orcamento = persistirOrcamento(StatusOrcamento.APROVADO);
        OrdemServicoJpaEntity ordemServico =
                persistirOrdemServico(orcamento, usuario, StatusOrdemServico.EM_DIAGNOSTICO);

        AtualizacaoStatusOrdemServicoRequestDTO request =
                new AtualizacaoStatusOrdemServicoRequestDTO(StatusOrdemServico.EM_EXECUCAO);

        mockMvc.perform(patch(ENDPOINT + "/" + ordemServico.getId() + "/status")
                        .with(atendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        assertThat(ordemServicoRepository.findById(ordemServico.getId()).orElseThrow()
                .getStatusOrdemServico().getNome())
                .isEqualTo(StatusOrdemServico.EM_EXECUCAO.name());
        assertThat(movimentacaoEstoqueRepository.count()).isZero();
    }

    @Test
    @DisplayName("deve retornar 400 ao solicitar uma transição de status inválida")
    void deveRetornar400ParaTransicaoInvalida() throws Exception {
        seedStatusOrdemServico();
        UsuarioJpaEntity usuario = persistirUsuario();
        OrcamentoJpaEntity orcamento = persistirOrcamento(StatusOrcamento.APROVADO);
        OrdemServicoJpaEntity ordemServico =
                persistirOrdemServico(orcamento, usuario, StatusOrdemServico.AGUARDANDO_APROVACAO);

        AtualizacaoStatusOrdemServicoRequestDTO request =
                new AtualizacaoStatusOrdemServicoRequestDTO(StatusOrdemServico.FINALIZADA);

        mockMvc.perform(patch(ENDPOINT + "/" + ordemServico.getId() + "/status")
                        .with(atendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Transicao de status invalida"));

        assertThat(ordemServicoRepository.findById(ordemServico.getId()).orElseThrow()
                .getStatusOrdemServico().getNome())
                .isEqualTo(StatusOrdemServico.AGUARDANDO_APROVACAO.name());
    }

    @Test
    @DisplayName("deve retornar 400 ao transicionar o status sem informar o novo status")
    void deveRetornar400QuandoNovoStatusNaoInformado() throws Exception {
        seedStatusOrdemServico();
        UsuarioJpaEntity usuario = persistirUsuario();
        OrcamentoJpaEntity orcamento = persistirOrcamento(StatusOrcamento.APROVADO);
        OrdemServicoJpaEntity ordemServico =
                persistirOrdemServico(orcamento, usuario, StatusOrdemServico.AGUARDANDO_APROVACAO);

        mockMvc.perform(patch(ENDPOINT + "/" + ordemServico.getId() + "/status")
                        .with(atendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("deve retornar 404 ao transicionar o status de uma OS inexistente")
    void deveRetornar404AoTransicionarStatusDeOrdemServicoInexistente() throws Exception {
        seedStatusOrdemServico();
        AtualizacaoStatusOrdemServicoRequestDTO request =
                new AtualizacaoStatusOrdemServicoRequestDTO(StatusOrdemServico.EM_DIAGNOSTICO);

        mockMvc.perform(patch(ENDPOINT + "/999999/status")
                        .with(atendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Ordem de serviço não encontrada"));
    }

    @Test
    @DisplayName("deve retornar 401 ao acessar a ordem de serviço sem autenticação")
    void deveRetornar401SemAutenticacao() throws Exception {
        mockMvc.perform(post(ENDPOINT + "/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("deve retornar 403 ao acessar a ordem de serviço com um papel sem permissão")
    void deveRetornar403ParaPapelSemPermissao() throws Exception {
        AtualizacaoStatusOrdemServicoRequestDTO request =
                new AtualizacaoStatusOrdemServicoRequestDTO(StatusOrdemServico.EM_DIAGNOSTICO);

        mockMvc.perform(patch(ENDPOINT + "/1/status")
                        .with(definirRole("ESTOQUISTA"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
