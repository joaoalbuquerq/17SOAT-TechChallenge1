package com.kap.mechanics_api.adapter.web;

import com.kap.mechanics_api.adapter.web.OrcamentoWebMapper;
import com.kap.mechanics_api.core.orcamento.usecase.AtualizarStatusDoOrcamentoUseCase;
import com.kap.mechanics_api.core.orcamento.usecase.ConsultarItensDoOrcamentoUseCase;
import com.kap.mechanics_api.core.orcamento.usecase.GerarOrcamentoUseCase;
import com.kap.mechanics_api.core.orcamento.usecase.IncluirItemNoOrcamentoUseCase;
import com.kap.mechanics_api.adapter.web.doc.OrcamentoControllerDoc;
import com.kap.mechanics_api.adapter.web.dto.orcamento.AtualizacaoStatusOrcamentoRequestDTO;
import com.kap.mechanics_api.adapter.web.dto.orcamento.ConsultaOrcamentoItensResponseDTO;
import com.kap.mechanics_api.adapter.web.dto.orcamento.GeracaoOrcamentoRequestDTO;
import com.kap.mechanics_api.adapter.web.dto.orcamento.InclusaoOrcamentoItemRequestDTO;
import jakarta.validation.Valid;
import com.kap.mechanics_api.adapter.web.dto.orcamento.GeracaoOrcamentoResponseDTO;
import com.kap.mechanics_api.core.orcamento.usecase.OrcamentoGerado;
import java.net.URI;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orcamento")
@PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
public class OrcamentoController implements OrcamentoControllerDoc {

    private final GerarOrcamentoUseCase gerarOrcamentoUseCase;
    private final AtualizarStatusDoOrcamentoUseCase atualizarStatusUseCase;
    private final IncluirItemNoOrcamentoUseCase incluirItemUseCase;
    private final ConsultarItensDoOrcamentoUseCase consultarItensUseCase;
    private final OrcamentoWebMapper orcamentoMapper;

    public OrcamentoController(GerarOrcamentoUseCase gerarOrcamentoUseCase,
                               AtualizarStatusDoOrcamentoUseCase atualizarStatusUseCase,
                               IncluirItemNoOrcamentoUseCase incluirItemUseCase,
                               ConsultarItensDoOrcamentoUseCase consultarItensUseCase,
                               OrcamentoWebMapper orcamentoMapper) {
        this.gerarOrcamentoUseCase = gerarOrcamentoUseCase;
        this.atualizarStatusUseCase = atualizarStatusUseCase;
        this.incluirItemUseCase = incluirItemUseCase;
        this.consultarItensUseCase = consultarItensUseCase;
        this.orcamentoMapper = orcamentoMapper;
    }

    @PostMapping("/{id}/itens")
    @Transactional
    public ResponseEntity<Void> incluirItem(@PathVariable Integer id,
                                            @Valid @RequestBody InclusaoOrcamentoItemRequestDTO dto) {
        incluirItemUseCase.executar(id, dto.servicoId(), dto.itemEstoqueId(), dto.quantidade());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/itens")
    @Transactional(readOnly = true)
    public ResponseEntity<ConsultaOrcamentoItensResponseDTO> consultarItens(@PathVariable Integer id) {
        return ResponseEntity.ok(orcamentoMapper.paraDto(consultarItensUseCase.executar(id)));
    }

    @PostMapping("/gerarOrcamento")
    @Transactional
    @Override
    public ResponseEntity<GeracaoOrcamentoResponseDTO> gerarOrcamento(
            @Valid @RequestBody GeracaoOrcamentoRequestDTO dto,
            Authentication authentication) {
        OrcamentoGerado gerado = gerarOrcamentoUseCase.executar(
                dto.clienteId(), dto.veiculoId(), dto.servicosIds(), authentication.getName());
        Integer ordemServicoId = gerado.ordemServico().getId();
        return ResponseEntity.created(URI.create("/api/ordem-servico/" + ordemServicoId))
                .body(new GeracaoOrcamentoResponseDTO(gerado.orcamento().getId(), ordemServicoId));
    }

    @PatchMapping("/{id}/status")
    @Transactional
    @Override
    public ResponseEntity<String> atualizarStatus(
            @PathVariable Integer id,
            @Valid @RequestBody AtualizacaoStatusOrcamentoRequestDTO dto) {

        atualizarStatusUseCase.executar(id, dto.status());
        return ResponseEntity.ok("status do orçamento atualizado com sucesso!");
    }
}
