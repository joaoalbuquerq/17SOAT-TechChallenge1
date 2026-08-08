package com.kap.mechanics_api.dto.cliente;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AtualizacaoClienteRequestDTO(
		@NotBlank(message = "Informe o nome do cliente") 
		String nome, 
		
		@NotBlank(message = "Informe o CPF ou CNPJ do cliente") 
		String cpfCnpj,
		
		@Pattern(
			    regexp = "^\\d{10,11}$",
			    message = "Telefone inválido. Informe DDD + número."
		)
		@NotBlank(message = "Informe o CPF ou CNPJ do cliente") 
		String telefone,
		
		@Email(message = "E-mail inválido") 
		String email
		) {
	
    public boolean temAoMenosUmCampoPreenchido() {
        return nome != null || cpfCnpj != null || telefone != null
                || email != null;
    }
}
