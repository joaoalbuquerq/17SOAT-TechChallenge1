package com.kap.mechanics_api.adapter.web;

import com.kap.mechanics_api.adapter.web.VeiculoWebMapper;
import com.kap.mechanics_api.core.veiculo.domain.Veiculo;
import com.kap.mechanics_api.core.veiculo.usecase.AtualizarVeiculoUseCase;
import com.kap.mechanics_api.core.veiculo.usecase.BuscarVeiculoPorIdUseCase;
import com.kap.mechanics_api.core.veiculo.usecase.CadastrarVeiculoUseCase;
import com.kap.mechanics_api.core.veiculo.usecase.ExcluirVeiculoUseCase;
import com.kap.mechanics_api.core.veiculo.usecase.ListarVeiculoUseCase;
import com.kap.mechanics_api.adapter.web.doc.VeiculoControllerDoc;
import com.kap.mechanics_api.adapter.web.dto.veiculo.*;
import com.kap.mechanics_api.adapter.web.NenhumCampoInformadoException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController()
@RequestMapping("/api/veiculo")
@PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
public class VeiculoController implements VeiculoControllerDoc {

    private final CadastrarVeiculoUseCase cadastrarVeiculoUseCase;
    private final ListarVeiculoUseCase listarVeiculoUseCase;
    private final BuscarVeiculoPorIdUseCase buscarVeiculoPorIdUseCase;
    private final AtualizarVeiculoUseCase atualizarVeiculoUseCase;
    private final ExcluirVeiculoUseCase excluirVeiculoUseCase;
    private final VeiculoWebMapper veiculoMapper;

    public VeiculoController(CadastrarVeiculoUseCase cadastrarVeiculoUseCase,
                             ListarVeiculoUseCase listarVeiculoUseCase,
                             BuscarVeiculoPorIdUseCase buscarVeiculoPorIdUseCase,
                             AtualizarVeiculoUseCase atualizarVeiculoUseCase,
                             ExcluirVeiculoUseCase excluirVeiculoUseCase,
                             VeiculoWebMapper veiculoMapper) {
        this.cadastrarVeiculoUseCase = cadastrarVeiculoUseCase;
        this.listarVeiculoUseCase = listarVeiculoUseCase;
        this.buscarVeiculoPorIdUseCase = buscarVeiculoPorIdUseCase;
        this.atualizarVeiculoUseCase = atualizarVeiculoUseCase;
        this.excluirVeiculoUseCase = excluirVeiculoUseCase;
        this.veiculoMapper = veiculoMapper;
    }

    // @Transactional: o caso de uso grava o veículo e o vínculo com o cliente; o core não conhece
    // transação, então a fronteira atômica fica na borda.
    @PostMapping
    @Transactional
    @Override
    public ResponseEntity<CriacaoVeiculoResponseDTO> cadastrar(@RequestBody @Valid CriacaoVeiculoRequestDTO dto){

        Veiculo veiculo = cadastrarVeiculoUseCase.executar(
                dto.placa(), dto.marca(), dto.modelo(), dto.ano(), dto.clienteId());

        CriacaoVeiculoResponseDTO response = veiculoMapper.paraCriacaoDto(veiculo);
        URI location = URI.create("/api/veiculo/" + response.id());

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @Override
    public ResponseEntity<List<ListagemVeiculoResponseDTO>> listar(){
        return ResponseEntity.ok(veiculoMapper.paraListagemDtoLista(listarVeiculoUseCase.executar()));
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<ListagemVeiculoResponseDTO> pesquisarPorId(@PathVariable Integer id){
        return ResponseEntity.ok(veiculoMapper.paraListagemDto(buscarVeiculoPorIdUseCase.executar(id)));
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<Void> deletar(@PathVariable Integer id){
        excluirVeiculoUseCase.executar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @Transactional
    @Override
    public ResponseEntity<AtualizacaoVeiculoResponseDTO> atualizar(@RequestBody AtualizacaoVeiculoRequestDTO dto, @PathVariable Integer id){

        if (!dto.temAoMenosUmCampoPreenchido()) {
            throw new NenhumCampoInformadoException(AtualizacaoVeiculoRequestDTO.class);
        }

        Veiculo veiculo = atualizarVeiculoUseCase.executar(
                id, dto.placa(), dto.marca(), dto.modelo(), dto.ano(), dto.clienteId());
        return ResponseEntity.ok(veiculoMapper.paraAtualizacaoDto(veiculo));
    }

}
