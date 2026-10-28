package com.kap.mechanics_api.adapter.web.dto.cliente;

public record AtualizacaoClienteResponseDTO(
		Integer id,
		String nome,
		String cpfCnpj,
		String telefone,
		String email
		) {

}
