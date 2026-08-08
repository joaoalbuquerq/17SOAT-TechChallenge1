package com.kap.mechanics_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kap.mechanics_api.domain.Clientes;

public interface ClienteRepository extends JpaRepository<Clientes, Integer> {

}
