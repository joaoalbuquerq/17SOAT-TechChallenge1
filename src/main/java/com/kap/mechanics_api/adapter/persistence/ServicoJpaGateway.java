package com.kap.mechanics_api.adapter.persistence;

import com.kap.mechanics_api.core.servico.domain.Servico;
import com.kap.mechanics_api.core.servico.gateway.ServicoGateway;
import com.kap.mechanics_api.adapter.persistence.repository.ServicoRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ServicoJpaGateway implements ServicoGateway {

    private final ServicoJpaMapper servicoJpaMapper;
    private final ServicoRepository servicoRepository;

    public ServicoJpaGateway(ServicoJpaMapper servicoJpaMapper, ServicoRepository servicoRepository) {
        this.servicoJpaMapper = servicoJpaMapper;
        this.servicoRepository = servicoRepository;
    }

    @Override
    public Servico salvar(Servico servico) {
        var entity = servicoJpaMapper.paraEntity(servico);
        return servicoJpaMapper.paraDominio(servicoRepository.save(entity));
    }

    @Override
    public List<Servico> listarTodos() {
        return servicoRepository.findAll().stream().map(servicoJpaMapper::paraDominio).toList();
    }

    @Override
    public Optional<Servico> pesquisarPorId(Integer id) {
        return servicoRepository.findById(id).map(servicoJpaMapper::paraDominio);
    }

    @Override
    public void excluir(Integer id) {
        servicoRepository.deleteById(id);
    }
}
