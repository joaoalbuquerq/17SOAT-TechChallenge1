package com.kap.mechanics_api.adapter.persistence;

import com.kap.mechanics_api.core.itemestoque.domain.ItemEstoque;
import com.kap.mechanics_api.core.itemestoque.gateway.ItemEstoqueGateway;
import com.kap.mechanics_api.adapter.persistence.repository.ItemEstoqueRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ItemEstoqueJpaGateway implements ItemEstoqueGateway {

    private final ItemEstoqueJpaMapper itemEstoqueJpaMapper;
    private final ItemEstoqueRepository itemEstoqueRepository;

    public ItemEstoqueJpaGateway(ItemEstoqueJpaMapper itemEstoqueJpaMapper,
                                 ItemEstoqueRepository itemEstoqueRepository) {
        this.itemEstoqueJpaMapper = itemEstoqueJpaMapper;
        this.itemEstoqueRepository = itemEstoqueRepository;
    }

    @Override
    public ItemEstoque salvar(ItemEstoque itemEstoque) {
        var entity = itemEstoqueJpaMapper.paraEntity(itemEstoque);
        return itemEstoqueJpaMapper.paraDominio(itemEstoqueRepository.save(entity));
    }

    @Override
    public List<ItemEstoque> listarTodos() {
        return itemEstoqueRepository.findAll().stream().map(itemEstoqueJpaMapper::paraDominio).toList();
    }

    @Override
    public Optional<ItemEstoque> pesquisarPorId(Integer id) {
        return itemEstoqueRepository.findById(id).map(itemEstoqueJpaMapper::paraDominio);
    }

    @Override
    public Optional<ItemEstoque> pesquisarPorIdParaAtualizacao(Integer id) {
        return itemEstoqueRepository.findByIdForUpdate(id).map(itemEstoqueJpaMapper::paraDominio);
    }

    @Override
    public void excluir(Integer id) {
        itemEstoqueRepository.deleteById(id);
    }
}
