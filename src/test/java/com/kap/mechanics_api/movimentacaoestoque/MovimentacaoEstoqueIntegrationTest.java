package com.kap.mechanics_api.movimentacaoestoque;

import com.kap.mechanics_api.adapter.persistence.repository.*;
import com.kap.mechanics_api.adapter.persistence.repository.UsuarioRepository;
import tools.jackson.databind.json.JsonMapper;
import com.kap.mechanics_api.adapter.persistence.entity.ItemEstoqueJpaEntity;
import com.kap.mechanics_api.adapter.persistence.entity.MovimentacaoEstoqueJpaEntity;
import com.kap.mechanics_api.adapter.persistence.entity.OrcamentoJpaEntity;
import com.kap.mechanics_api.adapter.persistence.entity.OrdemServicoJpaEntity;
import com.kap.mechanics_api.adapter.persistence.entity.StatusOrdemServicoJpaEntity;
import com.kap.mechanics_api.adapter.persistence.entity.UsuarioJpaEntity;
import com.kap.mechanics_api.adapter.persistence.entity.ClienteJpaEntity;
import com.kap.mechanics_api.adapter.persistence.entity.VeiculoJpaEntity;
import com.kap.mechanics_api.adapter.web.dto.movimentacaoestoque.RegistroEntradaMovimentacaoEstoqueRequestDTO;
import com.kap.mechanics_api.adapter.web.dto.movimentacaoestoque.RegistroSaidaMovimentacaoEstoqueRequestDTO;
import com.kap.mechanics_api.core.itemestoque.domain.TipoItemEstoque;
import com.kap.mechanics_api.core.movimentacaoestoque.domain.TipoMovimentacaoEstoque;
import com.kap.mechanics_api.core.orcamento.domain.StatusOrcamento;
import com.kap.mechanics_api.core.usuario.domain.TipoUsuario;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MovimentacaoEstoqueIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @Autowired
    private ItemEstoqueRepository itemEstoqueRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private VeiculoRepository veiculoRepository;

    @Autowired
    private OrcamentoRepository orcamentoRepository;

    @Autowired
    private StatusOrdemServicoRepository statusOrdemServicoRepository;

    @Autowired
    private OrdemServicoRepository ordemServicoRepository;

    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    @Autowired
    private OrcamentoServicoRepository orcamentoServicoRepository;

    @Autowired
    private HistoricoStatusOsRepository historicoStatusOsRepository;


    @Autowired
    private ServicoItemRepository servicoItemRepository;

    @Autowired
    private OrcamentoItemRepository orcamentoItemRepository;

    private Integer itemId;
    private Integer usuarioId;
    private Integer ordemServicoId;

    @BeforeEach
    void setup() {
        servicoItemRepository.deleteAll();
        orcamentoItemRepository.deleteAll();
        movimentacaoEstoqueRepository.deleteAll();
        historicoStatusOsRepository.deleteAll();
        ordemServicoRepository.deleteAll();
        orcamentoServicoRepository.deleteAll();
        orcamentoRepository.deleteAll();
        clienteRepository.deleteAll();
        veiculoRepository.deleteAll();
        itemEstoqueRepository.deleteAll();
        statusOrdemServicoRepository.deleteAll();
        usuarioRepository.deleteAll();

        UsuarioJpaEntity usuario = new UsuarioJpaEntity();
        usuario.setNome("Estoque");
        usuario.setLogin("estoque");
        usuario.setSenhaHash("hash");
        usuario.setTipo(TipoUsuario.ESTOQUISTA);
        usuario.setDataCriacao(LocalDateTime.now());
        usuario = usuarioRepository.save(usuario);
        usuarioId = usuario.getId();

        ItemEstoqueJpaEntity item = new ItemEstoqueJpaEntity();
        item.setNome("Filtro de oleo");
        item.setDescricao("Filtro");
        item.setTipoItemEstoque(TipoItemEstoque.PECA);
        item.setValorUnitario(new BigDecimal("35.90"));
        item.setQuantidadeAtual(10);
        item.setQuantidadeMinima(2);
        item.setAtivo(true);
        item = itemEstoqueRepository.save(item);
        itemId = item.getId();

        ClienteJpaEntity clienteJpaEntity = new ClienteJpaEntity();
        clienteJpaEntity.setNome("Cliente");
        clienteJpaEntity.setCpfCnpj("12345678900");
        clienteJpaEntity.setTelefone("51999999999");
        clienteJpaEntity.setEmail("cliente@email.com");
        clienteJpaEntity.setDataCriacao(LocalDateTime.now());
        clienteJpaEntity = clienteRepository.save(clienteJpaEntity);

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
        orcamento = orcamentoRepository.save(orcamento);

        StatusOrdemServicoJpaEntity status = new StatusOrdemServicoJpaEntity();
        status.setNome("EM_EXECUCAO");
        status = statusOrdemServicoRepository.save(status);

        OrdemServicoJpaEntity ordemServico = new OrdemServicoJpaEntity();
        ordemServico.setOrcamento(orcamento);
        ordemServico.setUsuarioAtendente(usuario);
        ordemServico.setStatusOrdemServico(status);
        ordemServico.setDataAbertura(java.time.LocalDateTime.now());
        ordemServicoId = ordemServicoRepository.save(ordemServico).getId();
    }

    @Test
    @WithMockUser(username = "estoque", roles = "ESTOQUISTA")
    void deveRegistrarEntradaPersistirMovimentacaoEAtualizarSaldo() throws Exception {
        var request = new RegistroEntradaMovimentacaoEstoqueRequestDTO(itemId, 7);

        mockMvc.perform(post("/api/movimentacao-estoque/entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(autenticado()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value(TipoMovimentacaoEstoque.ENTRADA.name()))
                .andExpect(jsonPath("$.saldoItemEstoque").value(17));

        ItemEstoqueJpaEntity itemAtualizado = itemEstoqueRepository.findById(itemId).orElseThrow();
        assertEquals(17, itemAtualizado.getQuantidadeAtual());
        assertEquals(1, movimentacaoEstoqueRepository.findAll().size());
    }

    @Test
    @WithMockUser(username = "estoque", roles = "ESTOQUISTA")
    void deveRegistrarSaidaPersistirMovimentacaoEAtualizarSaldo() throws Exception {
        var request = new RegistroSaidaMovimentacaoEstoqueRequestDTO(itemId, 4, ordemServicoId);

        mockMvc.perform(post("/api/movimentacao-estoque/saida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(autenticado()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value(TipoMovimentacaoEstoque.SAIDA.name()))
                .andExpect(jsonPath("$.saldoItemEstoque").value(6))
                .andExpect(jsonPath("$.ordemServicoId").value(ordemServicoId));

        ItemEstoqueJpaEntity itemAtualizado = itemEstoqueRepository.findById(itemId).orElseThrow();
        assertEquals(6, itemAtualizado.getQuantidadeAtual());
        assertEquals(1, movimentacaoEstoqueRepository.findAll().size());
    }

    @Test
    @WithMockUser(username = "estoque", roles = "ESTOQUISTA")
    void deveRetornar400QuandoSaidaUltrapassarSaldo() throws Exception {
        var request = new RegistroSaidaMovimentacaoEstoqueRequestDTO(itemId, 50, ordemServicoId);

        mockMvc.perform(post("/api/movimentacao-estoque/saida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(autenticado()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "estoque", roles = "ESTOQUISTA")
    void deveListarMovimentacoesPersistidas() throws Exception {
        movimentacaoEstoqueRepository.save(new MovimentacaoEstoqueJpaEntity(
                itemEstoqueRepository.findById(itemId).orElseThrow(),
                TipoMovimentacaoEstoque.ENTRADA,
                1,
                usuarioRepository.findById(usuarioId).orElseThrow(),
                null
        ));

        mockMvc.perform(get("/api/movimentacao-estoque").with(autenticado()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(username = "estoque", roles = "ESTOQUISTA")
    void deveListarMovimentacoesPorItem() throws Exception {
        var item = itemEstoqueRepository.findById(itemId).orElseThrow();
        var usuario = usuarioRepository.findById(usuarioId).orElseThrow();
        movimentacaoEstoqueRepository.save(new MovimentacaoEstoqueJpaEntity(item, TipoMovimentacaoEstoque.ENTRADA, 1, usuario, null));
        movimentacaoEstoqueRepository.save(new MovimentacaoEstoqueJpaEntity(item, TipoMovimentacaoEstoque.ENTRADA, 2, usuario, null));

        mockMvc.perform(get("/api/movimentacao-estoque/item/" + itemId).with(autenticado()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(username = "estoque", roles = "ESTOQUISTA")
    void deveListarMovimentacoesPorOrdemServico() throws Exception {
        var item = itemEstoqueRepository.findById(itemId).orElseThrow();
        var usuario = usuarioRepository.findById(usuarioId).orElseThrow();
        movimentacaoEstoqueRepository.save(new MovimentacaoEstoqueJpaEntity(item, TipoMovimentacaoEstoque.SAIDA, 1, usuario, ordemServicoRepository.findById(ordemServicoId).orElseThrow()));

        mockMvc.perform(get("/api/movimentacao-estoque/ordem-servico/" + ordemServicoId).with(autenticado()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(username = "estoque", roles = "ESTOQUISTA")
    void deveListarMovimentacoesPorTipo() throws Exception {
        var item = itemEstoqueRepository.findById(itemId).orElseThrow();
        var usuario = usuarioRepository.findById(usuarioId).orElseThrow();
        movimentacaoEstoqueRepository.save(new MovimentacaoEstoqueJpaEntity(item, TipoMovimentacaoEstoque.ENTRADA, 1, usuario, null));
        movimentacaoEstoqueRepository.save(new MovimentacaoEstoqueJpaEntity(item, TipoMovimentacaoEstoque.SAIDA, 2, usuario, ordemServicoRepository.findById(ordemServicoId).orElseThrow()));

        mockMvc.perform(get("/api/movimentacao-estoque/tipo/ENTRADA").with(autenticado()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].tipo").value(TipoMovimentacaoEstoque.ENTRADA.name()));
    }

    @Test
    @WithMockUser(username = "estoque", roles = "ESTOQUISTA")
    void deveListarMovimentacoesPorPeriodo() throws Exception {
        var item = itemEstoqueRepository.findById(itemId).orElseThrow();
        var usuario = usuarioRepository.findById(usuarioId).orElseThrow();

        var movimentacao = new MovimentacaoEstoqueJpaEntity(
                item,
                TipoMovimentacaoEstoque.ENTRADA,
                1,
                usuario,
                null
        );

        movimentacao.setDataHora(
                LocalDateTime.of(2026, 8, 15, 10, 0)
        );

        movimentacaoEstoqueRepository.save(movimentacao);

        mockMvc.perform(get("/api/movimentacao-estoque/periodo")
                        .param("inicio", "2026-08-01T00:00:00")
                        .param("fim", "2026-08-31T23:59:59")
                        .with(autenticado()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(username = "estoque", roles = "ESTOQUISTA")
    void deveRetornar400QuandoPeriodoForInverso() throws Exception {
        mockMvc.perform(get("/api/movimentacao-estoque/periodo")
                        .param("inicio", "2026-08-31T23:59:59")
                        .param("fim", "2026-08-01T00:00:00")
                        .with(autenticado()))
                .andExpect(status().isBadRequest());
    }

    private RequestPostProcessor autenticado() {
        return jwt()
                .jwt(jwt -> jwt.subject("estoque"))
                .authorities(new SimpleGrantedAuthority("ROLE_ESTOQUISTA"));
    }
}
