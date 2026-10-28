package com.kap.mechanics_api;

import com.kap.mechanics_api.adapter.web.dto.cliente.*;
import com.kap.mechanics_api.dto.usuario.*;
import com.kap.mechanics_api.adapter.web.dto.veiculo.*;
import com.kap.mechanics_api.core.usuario.domain.TipoUsuario;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Utilities {

    public static List<ListagemVeiculoResponseDTO>  produzirListaVeiculosResponse(){
        List<ListagemVeiculoResponseDTO> veiculosDto = new ArrayList<>();
        ListagemVeiculoResponseDTO dto1 = new ListagemVeiculoResponseDTO(Integer.valueOf(1), "ABC1234", "Honda", "Civic", 2020, Integer.valueOf(1));
        ListagemVeiculoResponseDTO dto2 = new ListagemVeiculoResponseDTO(Integer.valueOf(2), "ADD1234", "Honda", "CRV", 2022, Integer.valueOf(1));
        veiculosDto.add(dto1);
        veiculosDto.add(dto2);

        return veiculosDto;
    }

    public static ListagemVeiculoResponseDTO produzirRespostaListagemVeiculoDto(){
        return new ListagemVeiculoResponseDTO(Integer.valueOf(1), "ABC1234", "Honda", "Civic", 2020,Integer.valueOf(1));
    }

    public static CriacaoVeiculoResponseDTO produzirVeiculoSalvoDto(){
        return new CriacaoVeiculoResponseDTO(Integer.valueOf(1), "ABC1234", "Honda", "Civic", 2020);
    }

    public static CriacaoVeiculoRequestDTO produzirVeiculoRequestDto(){
        return new CriacaoVeiculoRequestDTO("ABC1234", "Honda", "Civic", 2020, Integer.valueOf(1));
    }

    public static AtualizacaoVeiculoRequestDTO produzirAtualizacaoVeiculoDto(){
        return new AtualizacaoVeiculoRequestDTO("ABC1234", "Honda2", "Civic2", Integer.valueOf(2020), Integer.valueOf(1));
    }

    public static AtualizacaoVeiculoRequestDTO produzirAtualizacaoVeiculoDtoInvalido(){
        return new AtualizacaoVeiculoRequestDTO("", "", "", null, null);
    }

    public static AtualizacaoVeiculoResponseDTO produzirAtualizacaoVeiculoResponseDto(){
        return new AtualizacaoVeiculoResponseDTO(Integer.valueOf(1), "ABC1234", "Honda2", "Civic2", 2020, Integer.valueOf(1));
    }

    public static List<ListagemUsuarioDTO> produzirListaUsuariosResponse() {
        List<ListagemUsuarioDTO> usuariosDto = new ArrayList<>();
        ListagemUsuarioDTO dto1 = new ListagemUsuarioDTO( Integer.valueOf(1), "pedin", "1234", "pedinApelao", LocalDateTime.now(), TipoUsuario.ATENDENTE );
        ListagemUsuarioDTO dto2 = new ListagemUsuarioDTO( Integer.valueOf(2), "joao", "1234", "joaoApelao", LocalDateTime.now(), TipoUsuario.ATENDENTE );
        usuariosDto.add(dto1);
        usuariosDto.add(dto2);
        return usuariosDto;
    }
    public static ListagemUsuarioDTO produzirRespostaListagemUsuarioDto() {
        return new ListagemUsuarioDTO( Integer.valueOf(1), "pedin", "1234", "pedinApelao", LocalDateTime.now(), TipoUsuario.ATENDENTE );
    }
    public static CriacaoUsuarioResponseDTO produzirUsuarioSalvoDto() {
        return new CriacaoUsuarioResponseDTO( Integer.valueOf(1), "pedin", "pedinApelao", LocalDateTime.now(), TipoUsuario.ATENDENTE );
    }
    public static CriacaoUsuarioRequestDTO produzirUsuarioRequestDto() {
        return new CriacaoUsuarioRequestDTO( "pedin", "pedinApelao", "1234", TipoUsuario.ATENDENTE.toString() );
    }
    public static AtualizacaoUsuarioRequestDTO produzirAtualizacaoUsuarioDto() {
        return new AtualizacaoUsuarioRequestDTO( "pedin2", "pedinApelao2", "12345", "ATENDENTE" );
    }
    public static AtualizacaoUsuarioRequestDTO produzirAtualizacaoUsuarioDtoInvalido() {
        return new AtualizacaoUsuarioRequestDTO( "", "", "", "" );
    }
    public static AtualizacaoUsuarioResponseDTO produzirAtualizacaoUsuarioResponseDto() {
        return new AtualizacaoUsuarioResponseDTO( Integer.valueOf(1), "pedin2", "pedinApelao2", "12345", TipoUsuario.ATENDENTE );
    }

    public static CriacaoClienteRequestDTO produzirClienteRequestDto() {
        return new CriacaoClienteRequestDTO("João Silva", "12345678900", "51999999999", "joao@email.com");
    }

    public static CriacaoClienteResponseDTO produzirClienteSalvoDto() {
        return new CriacaoClienteResponseDTO(1, "João Silva", "12345678900", "51999999999", "joao@email.com");
    }

    public static ListagemClienteResponseDTO produzirRespostaListagemClienteDto() {
        return new ListagemClienteResponseDTO(1, "João Silva", "12345678900", "51999999999", "joao@email.com");
    }

    public static List<ListagemClienteResponseDTO> produzirListaClientesResponse() {
        List<ListagemClienteResponseDTO> clientes = new ArrayList<>();
        clientes.add(new ListagemClienteResponseDTO(1, "João Silva", "12345678900", "51999999999", "joao@email.com"));
        clientes.add(new ListagemClienteResponseDTO(2, "Maria Souza", "98765432100", "51988888888", "maria@email.com"));
        return clientes;
    }

    public static AtualizacaoClienteRequestDTO produzirAtualizacaoClienteDtoInvalido() {
        return new AtualizacaoClienteRequestDTO(null, null, null, null);
    }

    public static AtualizacaoClienteRequestDTO produzirAtualizacaoClienteDto() {
        return new AtualizacaoClienteRequestDTO("João Silva Junior", "12345678900", "51988887777", "joaojr@email.com");
    }

    public static AtualizacaoClienteResponseDTO produzirAtualizacaoClienteResponseDto() {
        return new AtualizacaoClienteResponseDTO(1, "João Silva Junior", "12345678900", "51988887777", "joaojr@email.com");
    }


}
