package com.kap.mechanics_api.adapter.web;

import com.kap.mechanics_api.adapter.web.ItemEstoqueWebMapper;
import com.kap.mechanics_api.core.itemestoque.domain.ItemEstoque;
import com.kap.mechanics_api.core.itemestoque.usecase.AtualizarItemEstoqueUseCase;
import com.kap.mechanics_api.core.itemestoque.usecase.BuscarItemEstoquePorIdUseCase;
import com.kap.mechanics_api.core.itemestoque.usecase.CadastrarItemEstoqueUseCase;
import com.kap.mechanics_api.core.itemestoque.usecase.ExcluirItemEstoqueUseCase;
import com.kap.mechanics_api.core.itemestoque.usecase.ListarItemEstoqueUseCase;
import com.kap.mechanics_api.adapter.web.doc.ItemEstoqueControllerDoc;
import com.kap.mechanics_api.adapter.web.dto.itemestoque.AtualizacaoItemEstoqueRequestDTO;
import com.kap.mechanics_api.adapter.web.dto.itemestoque.CriacaoItemEstoqueRequestDTO;
import com.kap.mechanics_api.adapter.web.dto.itemestoque.ItemEstoqueResponseDTO;
import com.kap.mechanics_api.adapter.web.NenhumCampoInformadoException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/item-estoque")
@PreAuthorize("hasAnyRole('ADMIN', 'ESTOQUISTA')")
public class ItemEstoqueController implements ItemEstoqueControllerDoc {

    private final CadastrarItemEstoqueUseCase cadastrarItemEstoqueUseCase;
    private final ListarItemEstoqueUseCase listarItemEstoqueUseCase;
    private final BuscarItemEstoquePorIdUseCase buscarItemEstoquePorIdUseCase;
    private final AtualizarItemEstoqueUseCase atualizarItemEstoqueUseCase;
    private final ExcluirItemEstoqueUseCase excluirItemEstoqueUseCase;
    private final ItemEstoqueWebMapper itemEstoqueMapper;

    public ItemEstoqueController(CadastrarItemEstoqueUseCase cadastrarItemEstoqueUseCase,
                                 ListarItemEstoqueUseCase listarItemEstoqueUseCase,
                                 BuscarItemEstoquePorIdUseCase buscarItemEstoquePorIdUseCase,
                                 AtualizarItemEstoqueUseCase atualizarItemEstoqueUseCase,
                                 ExcluirItemEstoqueUseCase excluirItemEstoqueUseCase,
                                 ItemEstoqueWebMapper itemEstoqueMapper) {
        this.cadastrarItemEstoqueUseCase = cadastrarItemEstoqueUseCase;
        this.listarItemEstoqueUseCase = listarItemEstoqueUseCase;
        this.buscarItemEstoquePorIdUseCase = buscarItemEstoquePorIdUseCase;
        this.atualizarItemEstoqueUseCase = atualizarItemEstoqueUseCase;
        this.excluirItemEstoqueUseCase = excluirItemEstoqueUseCase;
        this.itemEstoqueMapper = itemEstoqueMapper;
    }

    @PostMapping
    @Override
    public ResponseEntity<ItemEstoqueResponseDTO> cadastrar(@Valid @RequestBody CriacaoItemEstoqueRequestDTO dto) {
        ItemEstoque item = cadastrarItemEstoqueUseCase.executar(dto.nome(), dto.descricao(), dto.tipoItemEstoque(),
                dto.valorUnitario(), dto.quantidadeMinima(), dto.ativo());

        ItemEstoqueResponseDTO response = itemEstoqueMapper.paraDto(item);
        return ResponseEntity.created(URI.create("/api/item-estoque/" + response.id())).body(response);
    }

    @GetMapping
    @Override
    public ResponseEntity<List<ItemEstoqueResponseDTO>> listar() {
        return ResponseEntity.ok(itemEstoqueMapper.paraDtoLista(listarItemEstoqueUseCase.executar()));
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<ItemEstoqueResponseDTO> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(itemEstoqueMapper.paraDto(buscarItemEstoquePorIdUseCase.executar(id)));
    }

    @PatchMapping("/{id}")
    @Override
    public ResponseEntity<ItemEstoqueResponseDTO> atualizar(@PathVariable Integer id,
                                                            @Valid @RequestBody AtualizacaoItemEstoqueRequestDTO dto) {
        if (!dto.temAoMenosUmCampoPreenchido()) {
            throw new NenhumCampoInformadoException(AtualizacaoItemEstoqueRequestDTO.class);
        }

        ItemEstoque item = atualizarItemEstoqueUseCase.executar(id, dto.nome(), dto.descricao(),
                dto.tipoItemEstoque(), dto.valorUnitario(), dto.quantidadeMinima(), dto.ativo());

        return ResponseEntity.ok(itemEstoqueMapper.paraDto(item));
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        excluirItemEstoqueUseCase.executar(id);
        return ResponseEntity.noContent().build();
    }
}
