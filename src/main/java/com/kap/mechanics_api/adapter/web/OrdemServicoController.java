package com.kap.mechanics_api.adapter.web;

import com.kap.mechanics_api.adapter.web.OrdemServicoWebMapper;
import com.kap.mechanics_api.core.ordemservico.domain.OrdemServico;
import com.kap.mechanics_api.core.ordemservico.usecase.ConsultarItensDaOrdemServicoUseCase;
import com.kap.mechanics_api.core.ordemservico.usecase.GerarOrdemServicoUseCase;
import com.kap.mechanics_api.core.ordemservico.usecase.IncluirItemNaOrdemServicoUseCase;
import com.kap.mechanics_api.core.ordemservico.usecase.ListarOrdensServicoPorClienteUseCase;
import com.kap.mechanics_api.core.ordemservico.usecase.ConsultarOrdemServicoUseCase;
import com.kap.mechanics_api.core.ordemservico.usecase.ListarOrdensServicoUseCase;
import com.kap.mechanics_api.core.ordemservico.usecase.TransicionarStatusOrdemServicoUseCase;
import com.kap.mechanics_api.adapter.web.doc.OrdemServicoControllerDoc;
import com.kap.mechanics_api.adapter.web.dto.ordemservico.AtualizacaoStatusOrdemServicoRequestDTO;
import com.kap.mechanics_api.adapter.web.dto.ordemservico.ConsultaOrdemServicoItensResponseDTO;
import com.kap.mechanics_api.adapter.web.dto.ordemservico.InclusaoOrdemServicoItemRequestDTO;
import com.kap.mechanics_api.adapter.web.dto.ordemservico.ListagemOrdemServicoResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/ordem-servico")
@PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
public class OrdemServicoController implements OrdemServicoControllerDoc {

    private final GerarOrdemServicoUseCase gerarOrdemServicoUseCase;
    private final TransicionarStatusOrdemServicoUseCase transicionarStatusUseCase;
    private final IncluirItemNaOrdemServicoUseCase incluirItemUseCase;
    private final ConsultarItensDaOrdemServicoUseCase consultarItensUseCase;
    private final ConsultarOrdemServicoUseCase consultarUseCase;
    private final ListarOrdensServicoUseCase listarUseCase;
    private final ListarOrdensServicoPorClienteUseCase listarPorClienteUseCase;
    private final OrdemServicoWebMapper ordemServicoMapper;

    public OrdemServicoController(GerarOrdemServicoUseCase gerarOrdemServicoUseCase,
                                  TransicionarStatusOrdemServicoUseCase transicionarStatusUseCase,
                                  IncluirItemNaOrdemServicoUseCase incluirItemUseCase,
                                  ConsultarItensDaOrdemServicoUseCase consultarItensUseCase,
                                  ConsultarOrdemServicoUseCase consultarUseCase,
                                  ListarOrdensServicoUseCase listarUseCase,
                                  ListarOrdensServicoPorClienteUseCase listarPorClienteUseCase,
                                  OrdemServicoWebMapper ordemServicoMapper) {
        this.gerarOrdemServicoUseCase = gerarOrdemServicoUseCase;
        this.transicionarStatusUseCase = transicionarStatusUseCase;
        this.incluirItemUseCase = incluirItemUseCase;
        this.consultarItensUseCase = consultarItensUseCase;
        this.consultarUseCase = consultarUseCase;
        this.listarUseCase = listarUseCase;
        this.listarPorClienteUseCase = listarPorClienteUseCase;
        this.ordemServicoMapper = ordemServicoMapper;
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    @Override
    public ResponseEntity<ListagemOrdemServicoResponseDTO> consultar(@PathVariable Integer id) {
        return ResponseEntity.ok(ordemServicoMapper.paraDto(consultarUseCase.executar(id)));
    }

    @PostMapping("/{id}/itens")
    @Transactional
    public ResponseEntity<Void> incluirItem(@PathVariable Integer id,
                                            @Valid @RequestBody InclusaoOrdemServicoItemRequestDTO dto) {
        incluirItemUseCase.executar(id, dto.servicoId(), dto.itemEstoqueId(), dto.quantidade());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/itens")
    @Transactional(readOnly = true)
    public ResponseEntity<ConsultaOrdemServicoItensResponseDTO> consultarItens(@PathVariable Integer id) {
        return ResponseEntity.ok(ordemServicoMapper.paraDto(consultarItensUseCase.executar(id)));
    }

    @PostMapping("/{orcamentoId}")
    @Transactional
    @Override
    public ResponseEntity<Void> gerarOrdemServico(
            @PathVariable Integer orcamentoId,
            Authentication authentication) {
        OrdemServico ordemServico = gerarOrdemServicoUseCase.executar(orcamentoId, authentication.getName());
        return ResponseEntity.created(URI.create("/api/ordem-servico/" + ordemServico.getId())).build();
    }

    @PatchMapping("/{id}/status")
    @Transactional
    @Override
    public ResponseEntity<Void> transicionarStatus(
            @PathVariable Integer id,
            @Valid @RequestBody AtualizacaoStatusOrdemServicoRequestDTO dto) {
        transicionarStatusUseCase.executar(id, dto.status());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Transactional(readOnly = true)
    @Override
    public ResponseEntity<List<ListagemOrdemServicoResponseDTO>> listarPorCliente(
            @RequestParam(required = false) Integer clienteId,
            @RequestParam(required = false) String cpfCnpj) {
        return ResponseEntity.ok(ordemServicoMapper.paraDtoLista(listarPorClienteUseCase.executar(clienteId, cpfCnpj)));
    }

    @GetMapping("/listar")
    @Transactional(readOnly = true)
    @Override
    public ResponseEntity<List<ListagemOrdemServicoResponseDTO>> listarTodas() {
        return ResponseEntity.ok(ordemServicoMapper.paraDtoLista(listarUseCase.executar()));
    }
}
