package com.kap.mechanics_api.exception;

public class ClienteNaoEncontradoException extends RuntimeException {
	
    public ClienteNaoEncontradoException(Integer id) {
        super("Cliente nao encontrado com o id " + id);
    }

}
