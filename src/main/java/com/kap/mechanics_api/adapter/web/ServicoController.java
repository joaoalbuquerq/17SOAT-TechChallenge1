package com.kap.mechanics_api.adapter.web;

import com.kap.mechanics_api.adapter.web.ServicoWebMapper;
import com.kap.mechanics_api.core.servico.domain.Servico;
import com.kap.mechanics_api.core.servico.usecase.AtualizarServicoUseCase;
import com.kap.mechanics_api.core.servico.usecase.BuscarServicoPorIdUseCase;
import com.kap.mechanics_api.core.servico.usecase.CadastrarServicoUseCase;
import com.kap.mechanics_api.core.servico.usecase.ExcluirServicoUseCase;
import com.kap.mechanics_api.core.servico.usecase.ListarServicoUseCase;
import com.kap.mechanics_api.adapter.web.doc.ServicoControllerDoc;
import com.kap.mechanics_api.adapter.web.dto.servico.*;
import com.kap.mechanics_api.adapter.web.NenhumCampoInformadoException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servico")
@PreAuthorize("hasAnyRole('ADMIN', 'ESTOQUISTA')")
public class ServicoController implements ServicoControllerDoc {

    private final CadastrarServicoUseCase cadastrarServicoUseCase;
    private final ListarServicoUseCase listarServicoUseCase;
    private final BuscarServicoPorIdUseCase buscarServicoPorIdUseCase;
    private final AtualizarServicoUseCase atualizarServicoUseCase;
    private final ExcluirServicoUseCase excluirServicoUseCase;
    private final ServicoWebMapper servicoMapper;

    public ServicoController(CadastrarServicoUseCase cadastrarServicoUseCase,
                             ListarServicoUseCase listarServicoUseCase,
                             BuscarServicoPorIdUseCase buscarServicoPorIdUseCase,
                             AtualizarServicoUseCase atualizarServicoUseCase,
                             ExcluirServicoUseCase excluirServicoUseCase,
                             ServicoWebMapper servicoMapper) {
        this.cadastrarServicoUseCase = cadastrarServicoUseCase;
        this.listarServicoUseCase = listarServicoUseCase;
        this.buscarServicoPorIdUseCase = buscarServicoPorIdUseCase;
        this.atualizarServicoUseCase = atualizarServicoUseCase;
        this.excluirServicoUseCase = excluirServicoUseCase;
        this.servicoMapper = servicoMapper;
    }

    @Override
    @PostMapping
    public ResponseEntity<ServicoResponseDTO> cadastrar(
            @Valid @RequestBody CriacaoServicoRequestDTO dto) {

        Servico servico = cadastrarServicoUseCase.executar(
                dto.nome(), dto.descricao(), dto.valorMaoDeObra(), dto.tempoEstimadoMin(), dto.ativo());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(servicoMapper.paraDto(servico));
    }

    @Override
    @GetMapping
    public ResponseEntity<List<ServicoResponseDTO>> listar() {

        return ResponseEntity.ok(servicoMapper.paraDtoLista(listarServicoUseCase.executar()));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<ServicoResponseDTO> buscarPorId(
            @PathVariable Integer id) {

        return ResponseEntity.ok(servicoMapper.paraDto(buscarServicoPorIdUseCase.executar(id)));
    }

    @Override
    @PatchMapping("/{id}")
    public ResponseEntity<ServicoResponseDTO> atualizar(
            @Valid @RequestBody AtualizacaoServicoRequestDTO dto,
            @PathVariable Integer id) {

        if (!dto.temAoMenosUmCampoPreenchido()) {
            throw new NenhumCampoInformadoException(AtualizacaoServicoRequestDTO.class);
        }

        Servico servico = atualizarServicoUseCase.executar(
                id, dto.nome(), dto.descricao(), dto.valorMaoDeObra(), dto.tempoEstimadoMin(), dto.ativo());

        return ResponseEntity.ok(servicoMapper.paraDto(servico));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
            @PathVariable Integer id) {

        excluirServicoUseCase.executar(id);

        return ResponseEntity.noContent().build();
    }
}
