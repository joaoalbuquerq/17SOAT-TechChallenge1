package com.kap.mechanics_api.adapter.persistence;

import com.kap.mechanics_api.adapter.persistence.entity.ClienteVeiculoJpaEntity;
import com.kap.mechanics_api.adapter.persistence.entity.ClienteVeiculoJpaId;
import com.kap.mechanics_api.core.veiculo.domain.Veiculo;
import com.kap.mechanics_api.core.veiculo.gateway.VeiculoGateway;
import com.kap.mechanics_api.adapter.persistence.repository.ClienteRepository;
import com.kap.mechanics_api.adapter.persistence.repository.ClienteVeiculoRepository;
import com.kap.mechanics_api.adapter.persistence.repository.VeiculoRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class VeiculoJpaGateway implements VeiculoGateway {

    private final VeiculoJpaMapper veiculoJpaMapper;
    private final VeiculoRepository veiculoRepository;
    private final ClienteRepository clienteRepository;
    private final ClienteVeiculoRepository clienteVeiculoRepository;

    public VeiculoJpaGateway(VeiculoJpaMapper veiculoJpaMapper, VeiculoRepository veiculoRepository,
                             ClienteRepository clienteRepository, ClienteVeiculoRepository clienteVeiculoRepository) {
        this.veiculoJpaMapper = veiculoJpaMapper;
        this.veiculoRepository = veiculoRepository;
        this.clienteRepository = clienteRepository;
        this.clienteVeiculoRepository = clienteVeiculoRepository;
    }

    @Override
    public Veiculo salvar(Veiculo veiculo) {
        var entity = veiculoJpaMapper.paraEntity(veiculo);
        return veiculoJpaMapper.paraDominio(veiculoRepository.save(entity));
    }

    @Override
    public List<Veiculo> listarTodos() {
        return veiculoRepository.findAll().stream().map(veiculoJpaMapper::paraDominio).toList();
    }

    @Override
    public Optional<Veiculo> pesquisarPorId(Integer id) {
        return veiculoRepository.findById(id).map(veiculoJpaMapper::paraDominio);
    }

    @Override
    public void excluir(Integer id) {
        veiculoRepository.deleteById(id);
    }

    @Override
    public void vincularCliente(Integer veiculoId, Integer clienteId) {
        if (clienteVeiculoRepository.existsById(new ClienteVeiculoJpaId(clienteId, veiculoId))) {
            return;
        }
        clienteVeiculoRepository.save(new ClienteVeiculoJpaEntity(
                veiculoRepository.getReferenceById(veiculoId),
                clienteRepository.getReferenceById(clienteId)));
    }
}
