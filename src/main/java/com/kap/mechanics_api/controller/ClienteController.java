package com.kap.mechanics_api.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kap.mechanics_api.dto.cliente.AtualizacaoClienteRequestDTO;
import com.kap.mechanics_api.dto.cliente.AtualizacaoClienteResponseDTO;
import com.kap.mechanics_api.dto.cliente.CriacaoClienteRequestDTO;
import com.kap.mechanics_api.dto.cliente.CriacaoClienteResponseDTO;
import com.kap.mechanics_api.dto.cliente.ListagemClienteResponseDTO;
import com.kap.mechanics_api.service.ClienteService;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
	
    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    public ResponseEntity<CriacaoClienteResponseDTO> cadastrar(@RequestBody CriacaoClienteRequestDTO clienteDTO) {
    	
    	CriacaoClienteResponseDTO response = clienteService.salvar(clienteDTO);    
    	URI location = URI.create("/api/clientes/" + response.id());
        return ResponseEntity.created(location).body(response);
    }
    
    @GetMapping
    public ResponseEntity<List<ListagemClienteResponseDTO>> listar(){
        return ResponseEntity.ok(clienteService.listar());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ListagemClienteResponseDTO> pesquisarPorId(@PathVariable Integer id){
        return ResponseEntity.ok(clienteService.buscarPorId(id));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id){
    	clienteService.deletar(id);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<AtualizacaoClienteResponseDTO> atualizar(@RequestBody AtualizacaoClienteRequestDTO dto, @PathVariable Integer id){
        return ResponseEntity.ok(clienteService.atualizar(dto, id));
    }
    
    
}
