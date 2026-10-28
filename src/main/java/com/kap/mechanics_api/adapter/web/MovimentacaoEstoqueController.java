package com.kap.mechanics_api.adapter.web;

import com.kap.mechanics_api.adapter.web.MovimentacaoEstoqueWebMapper;
import com.kap.mechanics_api.core.movimentacaoestoque.domain.MovimentacaoEstoqueConsulta;
import com.kap.mechanics_api.core.movimentacaoestoque.domain.TipoMovimentacaoEstoque;
import com.kap.mechanics_api.core.movimentacaoestoque.usecase.BuscarMovimentacaoPorIdUseCase;
import com.kap.mechanics_api.core.movimentacaoestoque.usecase.ListarMovimentacoesPorItemUseCase;
import com.kap.mechanics_api.core.movimentacaoestoque.usecase.ListarMovimentacoesPorOrdemServicoUseCase;
import com.kap.mechanics_api.core.movimentacaoestoque.usecase.ListarMovimentacoesPorPeriodoUseCase;
import com.kap.mechanics_api.core.movimentacaoestoque.usecase.ListarMovimentacoesPorTipoUseCase;
import com.kap.mechanics_api.core.movimentacaoestoque.usecase.ListarMovimentacoesUseCase;
import com.kap.mechanics_api.core.movimentacaoestoque.usecase.RegistrarEntradaEstoqueUseCase;
import com.kap.mechanics_api.core.movimentacaoestoque.usecase.RegistrarSaidaEstoqueUseCase;
import com.kap.mechanics_api.adapter.web.doc.MovimentacaoEstoqueControllerDoc;
import com.kap.mechanics_api.adapter.web.dto.movimentacaoestoque.MovimentacaoEstoqueResponseDTO;
import com.kap.mechanics_api.adapter.web.dto.movimentacaoestoque.RegistroEntradaMovimentacaoEstoqueRequestDTO;
import com.kap.mechanics_api.adapter.web.dto.movimentacaoestoque.RegistroSaidaMovimentacaoEstoqueRequestDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/movimentacao-estoque")
@PreAuthorize("hasAnyRole('ADMIN', 'ESTOQUISTA')")
public class MovimentacaoEstoqueController implements MovimentacaoEstoqueControllerDoc {

    private final RegistrarEntradaEstoqueUseCase registrarEntradaEstoqueUseCase;
    private final RegistrarSaidaEstoqueUseCase registrarSaidaEstoqueUseCase;
    private final ListarMovimentacoesUseCase listarMovimentacoesUseCase;
    private final ListarMovimentacoesPorItemUseCase listarMovimentacoesPorItemUseCase;
    private final ListarMovimentacoesPorOrdemServicoUseCase listarMovimentacoesPorOrdemServicoUseCase;
    private final ListarMovimentacoesPorTipoUseCase listarMovimentacoesPorTipoUseCase;
    private final ListarMovimentacoesPorPeriodoUseCase listarMovimentacoesPorPeriodoUseCase;
    private final BuscarMovimentacaoPorIdUseCase buscarMovimentacaoPorIdUseCase;
    private final MovimentacaoEstoqueWebMapper movimentacaoMapper;

    public MovimentacaoEstoqueController(RegistrarEntradaEstoqueUseCase registrarEntradaEstoqueUseCase,
                                         RegistrarSaidaEstoqueUseCase registrarSaidaEstoqueUseCase,
                                         ListarMovimentacoesUseCase listarMovimentacoesUseCase,
                                         ListarMovimentacoesPorItemUseCase listarMovimentacoesPorItemUseCase,
                                         ListarMovimentacoesPorOrdemServicoUseCase listarMovimentacoesPorOrdemServicoUseCase,
                                         ListarMovimentacoesPorTipoUseCase listarMovimentacoesPorTipoUseCase,
                                         ListarMovimentacoesPorPeriodoUseCase listarMovimentacoesPorPeriodoUseCase,
                                         BuscarMovimentacaoPorIdUseCase buscarMovimentacaoPorIdUseCase,
                                         MovimentacaoEstoqueWebMapper movimentacaoMapper) {
        this.registrarEntradaEstoqueUseCase = registrarEntradaEstoqueUseCase;
        this.registrarSaidaEstoqueUseCase = registrarSaidaEstoqueUseCase;
        this.listarMovimentacoesUseCase = listarMovimentacoesUseCase;
        this.listarMovimentacoesPorItemUseCase = listarMovimentacoesPorItemUseCase;
        this.listarMovimentacoesPorOrdemServicoUseCase = listarMovimentacoesPorOrdemServicoUseCase;
        this.listarMovimentacoesPorTipoUseCase = listarMovimentacoesPorTipoUseCase;
        this.listarMovimentacoesPorPeriodoUseCase = listarMovimentacoesPorPeriodoUseCase;
        this.buscarMovimentacaoPorIdUseCase = buscarMovimentacaoPorIdUseCase;
        this.movimentacaoMapper = movimentacaoMapper;
    }

    // @Transactional: o caso de uso trava o item, atualiza o saldo e grava o lançamento; o core não
    // conhece transação, então a fronteira atômica fica na borda.
    @Override
    @PostMapping("/entrada")
    @Transactional
    public ResponseEntity<MovimentacaoEstoqueResponseDTO> registrarEntrada(
            @Valid @RequestBody RegistroEntradaMovimentacaoEstoqueRequestDTO dto,
            Authentication authentication) {
        MovimentacaoEstoqueConsulta consulta = registrarEntradaEstoqueUseCase.executar(
                dto.itemEstoqueId(), dto.quantidade(), authentication.getName());
        return criado(movimentacaoMapper.paraDto(consulta));
    }

    @Override
    @PostMapping("/saida")
    @Transactional
    public ResponseEntity<MovimentacaoEstoqueResponseDTO> registrarSaida(
            @Valid @RequestBody RegistroSaidaMovimentacaoEstoqueRequestDTO dto,
            Authentication authentication) {
        MovimentacaoEstoqueConsulta consulta = registrarSaidaEstoqueUseCase.executar(
                dto.itemEstoqueId(), dto.quantidade(), dto.ordemServicoId(), authentication.getName());
        return criado(movimentacaoMapper.paraDto(consulta));
    }

    @Override
    @GetMapping
    public ResponseEntity<List<MovimentacaoEstoqueResponseDTO>> listar() {
        return ResponseEntity.ok(movimentacaoMapper.paraDtoLista(listarMovimentacoesUseCase.executar()));
    }

    @Override
    @GetMapping("/item/{itemEstoqueId}")
    public ResponseEntity<List<MovimentacaoEstoqueResponseDTO>> listarPorItem(@PathVariable Integer itemEstoqueId) {
        return ResponseEntity.ok(movimentacaoMapper.paraDtoLista(
                listarMovimentacoesPorItemUseCase.executar(itemEstoqueId)));
    }

    @Override
    @GetMapping("/ordem-servico/{ordemServicoId}")
    public ResponseEntity<List<MovimentacaoEstoqueResponseDTO>> listarPorOrdemServico(@PathVariable Integer ordemServicoId) {
        return ResponseEntity.ok(movimentacaoMapper.paraDtoLista(
                listarMovimentacoesPorOrdemServicoUseCase.executar(ordemServicoId)));
    }

    @Override
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<MovimentacaoEstoqueResponseDTO>> listarPorTipo(@PathVariable TipoMovimentacaoEstoque tipo) {
        return ResponseEntity.ok(movimentacaoMapper.paraDtoLista(listarMovimentacoesPorTipoUseCase.executar(tipo)));
    }

    @Override
    @GetMapping("/periodo")
    public ResponseEntity<List<MovimentacaoEstoqueResponseDTO>> listarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        return ResponseEntity.ok(movimentacaoMapper.paraDtoLista(
                listarMovimentacoesPorPeriodoUseCase.executar(inicio, fim)));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<MovimentacaoEstoqueResponseDTO> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(movimentacaoMapper.paraDto(buscarMovimentacaoPorIdUseCase.executar(id)));
    }

    private ResponseEntity<MovimentacaoEstoqueResponseDTO> criado(MovimentacaoEstoqueResponseDTO response) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(URI.create("/api/movimentacao-estoque/" + response.id()))
                .body(response);
    }
}
