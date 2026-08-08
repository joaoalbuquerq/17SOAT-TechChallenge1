package com.kap.mechanics_api.dto.cliente;

public record CriacaoClienteResponseDTO(
		Integer id,
		String nome,
		String cpfCnpj,
		String telefone,
		String email
		) {

}
