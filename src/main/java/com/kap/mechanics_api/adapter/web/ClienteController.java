package com.kap.mechanics_api.adapter.web;

import java.net.URI;
import java.util.List;

import com.kap.mechanics_api.core.cliente.domain.Cliente;
import com.kap.mechanics_api.core.cliente.domain.CpfCnpj;
import com.kap.mechanics_api.core.cliente.usecase.AtualizarClienteUseCase;
import com.kap.mechanics_api.core.cliente.usecase.BuscarClientePorDocumentoUseCase;
import com.kap.mechanics_api.core.cliente.usecase.BuscarClientePorIdUseCase;
import com.kap.mechanics_api.core.cliente.usecase.CadastrarClienteUseCase;
import com.kap.mechanics_api.core.cliente.usecase.ExcluirClienteUseCase;
import com.kap.mechanics_api.core.cliente.usecase.ListarClienteUseCase;
import com.kap.mechanics_api.adapter.web.doc.ClienteControllerDoc;
import com.kap.mechanics_api.adapter.web.NenhumCampoInformadoException;
import com.kap.mechanics_api.adapter.web.ClienteWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kap.mechanics_api.adapter.web.dto.cliente.AtualizacaoClienteRequestDTO;
import com.kap.mechanics_api.adapter.web.dto.cliente.AtualizacaoClienteResponseDTO;
import com.kap.mechanics_api.adapter.web.dto.cliente.CriacaoClienteRequestDTO;
import com.kap.mechanics_api.adapter.web.dto.cliente.CriacaoClienteResponseDTO;
import com.kap.mechanics_api.adapter.web.dto.cliente.ListagemClienteResponseDTO;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/cliente")
@PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
public class ClienteController implements ClienteControllerDoc {

    private final CadastrarClienteUseCase cadastrarClienteUseCase;
    private final ListarClienteUseCase listarClienteUseCase;
    private final BuscarClientePorIdUseCase buscarClientePorIdUseCase;
    private final BuscarClientePorDocumentoUseCase buscarClientePorDocumentoUseCase;
    private final AtualizarClienteUseCase atualizarClienteUseCase;
    private final ExcluirClienteUseCase excluirClienteUseCase;
    private final ClienteWebMapper clienteMapper;

    public ClienteController(CadastrarClienteUseCase cadastrarClienteUseCase,
                             ListarClienteUseCase listarClienteUseCase,
                             BuscarClientePorIdUseCase buscarClientePorIdUseCase,
                             BuscarClientePorDocumentoUseCase buscarClientePorDocumentoUseCase,
                             AtualizarClienteUseCase atualizarClienteUseCase,
                             ExcluirClienteUseCase excluirClienteUseCase,
                             ClienteWebMapper clienteMapper) {
        this.cadastrarClienteUseCase = cadastrarClienteUseCase;
        this.listarClienteUseCase = listarClienteUseCase;
        this.buscarClientePorIdUseCase = buscarClientePorIdUseCase;
        this.buscarClientePorDocumentoUseCase = buscarClientePorDocumentoUseCase;
        this.atualizarClienteUseCase = atualizarClienteUseCase;
        this.excluirClienteUseCase = excluirClienteUseCase;
        this.clienteMapper = clienteMapper;
    }

    @PostMapping
    @Override
    public ResponseEntity<CriacaoClienteResponseDTO> cadastrar(@Valid @RequestBody CriacaoClienteRequestDTO clienteDTO) {

        Cliente cliente = cadastrarClienteUseCase.executar(
                clienteDTO.nome(),
                new CpfCnpj(clienteDTO.cpfCnpj()),
                clienteDTO.telefone(),
                clienteDTO.email());

        CriacaoClienteResponseDTO response = clienteMapper.paraCriacaoDto(cliente);
        URI location = URI.create("/api/cliente/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @Override
    public ResponseEntity<List<ListagemClienteResponseDTO>> listar(){
        return ResponseEntity.ok(clienteMapper.paraListagemDtoLista(listarClienteUseCase.executar()));
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<ListagemClienteResponseDTO> pesquisarPorId(@PathVariable Integer id){
        return ResponseEntity.ok(clienteMapper.paraListagemDto(buscarClientePorIdUseCase.executar(id)));
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<Void> deletar(@PathVariable Integer id){
        excluirClienteUseCase.executar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<AtualizacaoClienteResponseDTO> atualizar(@Valid @RequestBody AtualizacaoClienteRequestDTO dto, @PathVariable Integer id){

        if (!dto.temAoMenosUmCampoPreenchido()) {
            throw new NenhumCampoInformadoException(AtualizacaoClienteRequestDTO.class);
        }

        // documento é opcional na atualização: sem texto, nada a alterar (null)
        CpfCnpj cpfCnpj = StringUtils.hasText(dto.cpfCnpj()) ? new CpfCnpj(dto.cpfCnpj()) : null;

        Cliente cliente = atualizarClienteUseCase.executar(id, dto.nome(), cpfCnpj, dto.telefone(), dto.email());
        return ResponseEntity.ok(clienteMapper.paraAtualizacaoDto(cliente));
    }

    @GetMapping("/documento/{documento}")
    @Override
    public ResponseEntity<ListagemClienteResponseDTO> pesquisarPorDocumento(
            @PathVariable String documento) {

        Cliente cliente = buscarClientePorDocumentoUseCase.executar(new CpfCnpj(documento));
        return ResponseEntity.ok(clienteMapper.paraListagemDto(cliente));
    }

}
