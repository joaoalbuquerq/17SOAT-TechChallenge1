package com.kap.mechanics_api.adapter.persistence.repository;

import com.kap.mechanics_api.adapter.persistence.entity.ItemEstoqueJpaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ItemEstoqueRepository extends JpaRepository<ItemEstoqueJpaEntity, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select item from ItemEstoqueJpaEntity item where item.id = :id")
    Optional<ItemEstoqueJpaEntity> findByIdForUpdate(@Param("id") Integer id);
}
