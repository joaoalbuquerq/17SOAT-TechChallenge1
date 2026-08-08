package com.kap.mechanics_api.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.kap.mechanics_api.domain.Clientes;
import com.kap.mechanics_api.domain.Veiculo;
import com.kap.mechanics_api.dto.cliente.AtualizacaoClienteRequestDTO;
import com.kap.mechanics_api.dto.cliente.AtualizacaoClienteResponseDTO;
import com.kap.mechanics_api.dto.cliente.CriacaoClienteRequestDTO;
import com.kap.mechanics_api.dto.cliente.CriacaoClienteResponseDTO;
import com.kap.mechanics_api.dto.cliente.ListagemClienteResponseDTO;
import com.kap.mechanics_api.exception.ClienteNaoEncontradoException;
import com.kap.mechanics_api.exception.NenhumCampoInformadoException;
import com.kap.mechanics_api.mapper.ClientesMapper;
import com.kap.mechanics_api.repository.ClienteRepository;

@Service
public class ClienteService {
	
    private final ClienteRepository clienteRepository;
    private final ClientesMapper clientesMapper;
    
    public ClienteService(ClienteRepository clienteRepository, ClientesMapper clientesMapper) {
        this.clienteRepository = clienteRepository;
        this.clientesMapper = clientesMapper;
    }

    public CriacaoClienteResponseDTO salvar(CriacaoClienteRequestDTO clienteDTO) {
    	
    	Clientes clientes = clientesMapper.dtoToEntity(clienteDTO);
    	clientes.setDataCriacao(LocalDateTime.now());
    	clientes = clienteRepository.save(clientes);
    	CriacaoClienteResponseDTO response = clientesMapper.entityToDto(clientes);
    	
        return response;
    }
    
    public List<ListagemClienteResponseDTO> listar(){
        List<Clientes> clientes = clienteRepository.findAll();
        return clientesMapper.listEntityToListDto(clientes);
    }
    
    public Clientes pesquisarPorId(Integer id){
        return clienteRepository.findById(id).orElseThrow( () -> new ClienteNaoEncontradoException(id));
    }

    public ListagemClienteResponseDTO buscarPorId(Integer id){
        Clientes clientes = pesquisarPorId(id);
        return clientesMapper.entityToListagemDto(clientes);
    }
    
    public void deletar(Integer id) {
    	Clientes clientes = pesquisarPorId(id);
    	clienteRepository.delete(clientes);
    }
    
    public AtualizacaoClienteResponseDTO atualizar(AtualizacaoClienteRequestDTO dto, Integer id){

        if(!dto.temAoMenosUmCampoPreenchido()){
            throw new NenhumCampoInformadoException(AtualizacaoClienteRequestDTO.class);
        }

        Clientes cliente = pesquisarPorId(id);     
        
        if(StringUtils.hasText(dto.nome())){
        	cliente.setNome(dto.nome());
        }
        
        if(StringUtils.hasText(dto.cpfCnpj())){
        	cliente.setCpfCnpj(dto.cpfCnpj());
        }

        if(StringUtils.hasText(dto.email())){
        	cliente.setEmail(dto.email());
        }

        if(StringUtils.hasText(dto.telefone())){
        	cliente.setTelefone(dto.telefone());
        }

        Clientes clienteAlterado = clienteRepository.save(cliente);
        return clientesMapper.entityToAtualizacaoDto(clienteAlterado);
    }
    
}
