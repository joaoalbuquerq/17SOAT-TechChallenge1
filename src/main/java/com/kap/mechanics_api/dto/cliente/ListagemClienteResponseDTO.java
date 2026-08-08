package com.kap.mechanics_api.dto.cliente;

public record ListagemClienteResponseDTO(
		Integer id,
		String nome,
		String cpfCnpj,
		String telefone,
		String email
		) {

}
