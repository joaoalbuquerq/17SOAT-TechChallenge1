package com.kap.mechanics_api.cliente;

import com.kap.mechanics_api.adapter.persistence.entity.ClienteJpaEntity;
import com.kap.mechanics_api.adapter.web.dto.cliente.AtualizacaoClienteRequestDTO;
import com.kap.mechanics_api.adapter.web.dto.cliente.CriacaoClienteRequestDTO;
import com.kap.mechanics_api.adapter.persistence.repository.*;
import com.kap.mechanics_api.repository.UsuarioRepository;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClienteIntegrationTest {

    private static final String ENDPOINT = "/api/cliente";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private OrcamentoRepository orcamentoRepository;

    @Autowired
    private OrcamentoServicoRepository orcamentoServicoRepository;

    @Autowired
    private OrdemServicoRepository ordemServicoRepository;

    @Autowired
    private ItemEstoqueRepository itemEstoqueRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private VeiculoRepository veiculoRepository;

    @Autowired
    private StatusOrdemServicoRepository statusOrdemServicoRepository;

    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    @Autowired
    private HistoricoStatusOsRepository historicoStatusOsRepository;

    @Autowired
    private ServicoItemRepository servicoItemRepository;

    @Autowired
    private OrcamentoItemRepository orcamentoItemRepository;

    @BeforeEach
    void limparDadosBaseAposTestes() {
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

    }

    private RequestPostProcessor definirRole(String role) {
        return jwt()
                .jwt(jwt -> jwt.subject("usuario-teste"))
                .authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }

    private RequestPostProcessor admin() {
        return definirRole("ADMIN");
    }

    private ClienteJpaEntity persistirCliente(String nome, String documento, String telefone, String email) {
        return clienteRepository.save(new ClienteJpaEntity(nome, documento, telefone, email, LocalDateTime.now()));
    }

    @Test
    @DisplayName("deve cadastrar cliente, retornar 201 com Location e persistir no banco")
    void deveCadastrarClienteEPersistir() throws Exception {
        CriacaoClienteRequestDTO request =
                new CriacaoClienteRequestDTO("João Silva", "12345678900", "51999999999", "joao@email.com");

        mockMvc.perform(post(ENDPOINT)
                        .with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/cliente/")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("João Silva"))
                .andExpect(jsonPath("$.cpfCnpj").value("12345678900"))
                .andExpect(jsonPath("$.telefone").value("51999999999"))
                .andExpect(jsonPath("$.email").value("joao@email.com"));

        List<ClienteJpaEntity> persistidos = clienteRepository.findAll();
        assertThat(persistidos).hasSize(1);
        ClienteJpaEntity salvo = persistidos.get(0);
        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getNome()).isEqualTo("João Silva");
        assertThat(salvo.getCpfCnpj()).isEqualTo("12345678900");
        assertThat(salvo.getTelefone()).isEqualTo("51999999999");
        assertThat(salvo.getEmail()).isEqualTo("joao@email.com");
        assertThat(salvo.getDataCriacao()).isNotNull();
    }

    @Test
    @DisplayName("deve retornar 400 e não persistir quando a requisição/dto for inválido")
    void deveRetornar400ParaQuandoDTOforInvalido() throws Exception {
        CriacaoClienteRequestDTO invalido =
                new CriacaoClienteRequestDTO("", "", "abc", "email-invalido");

        mockMvc.perform(post(ENDPOINT)
                        .with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros").exists());

        assertThat(clienteRepository.count()).isZero();
    }




    @Test
    @DisplayName("deve listar todos os clientes existentes")
    void deveListarClientes() throws Exception {
        persistirCliente("João Silva", "12345678900", "51999999999", "joao@email.com");
        persistirCliente("Maria Souza", "98765432100", "51988888888", "maria@email.com");

        mockMvc.perform(get(ENDPOINT).with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.nome == 'João Silva')]").exists())
                .andExpect(jsonPath("$[?(@.nome == 'Maria Souza')]").exists());
    }

    @Test
    @DisplayName("deve retornar lista vazia quando não existir nenhum clientes")
    void deveRetornarListaVaziaQuandoNaoTiverClienteCadastrado() throws Exception {
        mockMvc.perform(get(ENDPOINT).with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("deve retornar 200 com o cliente quando o id existente")
    void deveBuscarPorIdExistente() throws Exception {
        ClienteJpaEntity salvo = persistirCliente("João Silva", "12345678900", "51999999999", "joao@email.com");

        mockMvc.perform(get(ENDPOINT + "/" + salvo.getId()).with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(salvo.getId()))
                .andExpect(jsonPath("$.nome").value("João Silva"))
                .andExpect(jsonPath("$.cpfCnpj").value("12345678900"))
                .andExpect(jsonPath("$.telefone").value("51999999999"))
                .andExpect(jsonPath("$.email").value("joao@email.com"));
    }

    @Test
    @DisplayName("deve retornar 404 quando o id de cliente que não existe")
    void deveRetornar404ParaIdInexistente() throws Exception {
        mockMvc.perform(get(ENDPOINT + "/999999").with(admin()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Cliente não encontrado"))
                .andExpect(jsonPath("$.detail").value(containsString("999999")));
    }




    @Test
    @DisplayName("deve atualizar o cliente existente e realizar a mudança no banco")
    void deveAtualizarCliente() throws Exception {
        ClienteJpaEntity salvo = persistirCliente("João Silva", "12345678900", "51999999999", "joao@email.com");

        AtualizacaoClienteRequestDTO request =
                new AtualizacaoClienteRequestDTO("João Silva Junior", "12345678900", "51988887777", "joaojr@email.com");

        mockMvc.perform(put(ENDPOINT + "/" + salvo.getId())
                        .with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(salvo.getId()))
                .andExpect(jsonPath("$.nome").value("João Silva Junior"))
                .andExpect(jsonPath("$.telefone").value("51988887777"))
                .andExpect(jsonPath("$.email").value("joaojr@email.com"));

        ClienteJpaEntity atualizado = clienteRepository.findById(salvo.getId()).orElseThrow();
        assertThat(atualizado.getNome()).isEqualTo("João Silva Junior");
        assertThat(atualizado.getTelefone()).isEqualTo("51988887777");
        assertThat(atualizado.getEmail()).isEqualTo("joaojr@email.com");
    }

    @Test
    @DisplayName("deve retornar 404 ao atualizar cliente inexistente")
    void deveRetornar404ParaClienteInexistente() throws Exception {
        AtualizacaoClienteRequestDTO request =
                new AtualizacaoClienteRequestDTO("João Silva Junior", "12345678900", "51988887777", "joaojr@email.com");

        mockMvc.perform(put(ENDPOINT + "/999999")
                        .with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Cliente não encontrado"));
    }

    @Test
    @DisplayName("deve retornar 400 quando nenhum campo válido é informado")
    void deveRetornar400ParaRequisicaoSemCamposObrigatorios() throws Exception {
        ClienteJpaEntity salvo = persistirCliente("João Silva", "12345678900", "51999999999", "joao@email.com");

        AtualizacaoClienteRequestDTO vazio =
                new AtualizacaoClienteRequestDTO(null, null, null, null);

        mockMvc.perform(put(ENDPOINT + "/" + salvo.getId())
                        .with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vazio)))
                .andExpect(status().isBadRequest());

        ClienteJpaEntity inalterado = clienteRepository.findById(salvo.getId()).orElseThrow();
        assertThat(inalterado.getNome()).isEqualTo("João Silva");
    }

    @Test
    @DisplayName("deve remover o cliente existente e retornar 204")
    void deveDeletarClienteExistente() throws Exception {
        ClienteJpaEntity salvo = persistirCliente("João Silva", "12345678900", "51999999999", "joao@email.com");

        mockMvc.perform(delete(ENDPOINT + "/" + salvo.getId()).with(admin()))
                .andExpect(status().isNoContent());

        assertThat(clienteRepository.findById(salvo.getId())).isEmpty();
    }

    @Test
    @DisplayName("deve retornar 404 ao remover cliente inexistente")
    void deveRetornar404AoDeletarInexistente() throws Exception {
        mockMvc.perform(delete(ENDPOINT + "/999999").with(admin()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Cliente não encontrado"));
    }

    @Test
    @DisplayName("deve encontrar cliente informando o documento sem mascara")
    void deveBuscarPorDocumentoSemMascara() throws Exception {
        persistirCliente("João Silva", "12345678900", "51999999999", "joao@email.com");

        mockMvc.perform(get(ENDPOINT + "/documento/12345678900").with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("João Silva"))
                .andExpect(jsonPath("$.cpfCnpj").value("12345678900"));
    }

    @Test
    @DisplayName("deve encontrar cliente normalizando o documento formatado")
    void deveBuscarPorDocumentoFormatado() throws Exception {
        persistirCliente("João Silva", "12345678900", "51999999999", "joao@email.com");

        mockMvc.perform(get(ENDPOINT + "/documento/123.456.789-00").with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpfCnpj").value("12345678900"));
    }

    @Test
    @DisplayName("deve retornar 404 quando nenhum cliente possui o documento")
    void deveRetornar404ParaDocumentoInexistente() throws Exception {
        mockMvc.perform(get(ENDPOINT + "/documento/98765432100").with(admin()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Cliente não encontrado"));
    }

    @Test
    @DisplayName("deve retornar 400 quando o documento é inválido")
    void deveRetornar400ParaDocumentoInvalido() throws Exception {
        mockMvc.perform(get(ENDPOINT + "/documento/123").with(admin()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("deve retornar 401 quando a requisição não está autenticada")
    void deveRetornar401SemAutenticacao() throws Exception {
        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("deve retornar 403 quando o papel não tem permissão")
    void deveRetornar403ParaPapelSemPermissao() throws Exception {
        mockMvc.perform(get(ENDPOINT).with(definirRole("ESTOQUISTA")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("deve permitir acesso para o papel ATENDENTE")
    void devePermitirAcessoParaAtendente() throws Exception {
        persistirCliente("João Silva", "12345678900", "51999999999", "joao@email.com");

        mockMvc.perform(get(ENDPOINT).with(definirRole("ATENDENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

}
