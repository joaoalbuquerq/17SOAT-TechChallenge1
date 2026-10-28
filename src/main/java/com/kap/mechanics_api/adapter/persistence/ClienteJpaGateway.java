package com.kap.mechanics_api.adapter.persistence;

import com.kap.mechanics_api.adapter.persistence.entity.ClienteJpaEntity;
import com.kap.mechanics_api.core.cliente.domain.Cliente;
import com.kap.mechanics_api.core.cliente.domain.CpfCnpj;
import com.kap.mechanics_api.core.cliente.gateway.ClienteGateway;
import com.kap.mechanics_api.adapter.persistence.repository.ClienteRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ClienteJpaGateway implements ClienteGateway {

    private final ClienteJpaMapper clienteJpaMapper;
    private final ClienteRepository clienteRepository;

    public ClienteJpaGateway(ClienteJpaMapper clienteJpaMapper, ClienteRepository clienteRepository){
        this.clienteJpaMapper= clienteJpaMapper;
        this.clienteRepository = clienteRepository;
    }

    @Override
    public Cliente salvar(Cliente cliente) {
        var entity = clienteJpaMapper.paraEntity(cliente);
        return clienteJpaMapper.paraDominio(clienteRepository.save(entity));
    }

    @Override
    public List<Cliente> listarTodos() {
        List<ClienteJpaEntity> clientes = clienteRepository.findAll();
        return clientes.stream().map(clienteJpaMapper::paraDominio).toList();
    }

    @Override
    public Optional<Cliente> pesquisarPorId(Integer id) {
        return clienteRepository.findById(id).map(clienteJpaMapper::paraDominio);
    }

    @Override
    public Optional<Cliente> buscarPorDocumento(CpfCnpj cpfCnpj) {
        return clienteRepository.findByCpfCnpj(cpfCnpj.getDocumento()).map(clienteJpaMapper::paraDominio);
    }

    @Override
    public void excluir(Integer id) {
        clienteRepository.deleteById(id);
    }
}
