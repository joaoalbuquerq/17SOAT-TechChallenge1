package com.kap.mechanics_api.adapter.web;

import com.kap.mechanics_api.adapter.web.doc.UsuarioControllerDoc;
import com.kap.mechanics_api.adapter.web.dto.usuario.*;
import com.kap.mechanics_api.adapter.web.NenhumCampoInformadoException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.kap.mechanics_api.adapter.web.UsuarioWebMapper;
import com.kap.mechanics_api.core.usuario.domain.TipoUsuario;
import com.kap.mechanics_api.core.usuario.usecase.AtualizarUsuarioUseCase;
import com.kap.mechanics_api.core.usuario.usecase.BuscarPorIdUseCase;
import com.kap.mechanics_api.core.usuario.usecase.CadastrarUsuarioUseCase;
import com.kap.mechanics_api.core.usuario.usecase.ExcluirUsuarioUseCase;
import com.kap.mechanics_api.core.usuario.usecase.ListarUsuarioUseCase;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/usuario")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController implements UsuarioControllerDoc {

    private final BuscarPorIdUseCase buscarPorIdUseCase;
    private final ListarUsuarioUseCase listarUsuarioUseCase;
    private final UsuarioWebMapper usuarioWebMapper;
    private final CadastrarUsuarioUseCase cadastrarUsuarioUseCase;
    private final ExcluirUsuarioUseCase excluirUsuarioUseCase;
    private final AtualizarUsuarioUseCase atualizarUsuarioUseCase;

    public UsuarioController(
            BuscarPorIdUseCase buscarPorIdUseCase,
            ListarUsuarioUseCase listarUsuarioUseCase,
            UsuarioWebMapper usuarioWebMapper,
            CadastrarUsuarioUseCase cadastrarUsuarioUseCase,
            ExcluirUsuarioUseCase excluirUsuarioUseCase,
            AtualizarUsuarioUseCase atualizarUsuarioUseCase) {

        this.buscarPorIdUseCase = buscarPorIdUseCase;
        this.listarUsuarioUseCase = listarUsuarioUseCase;
        this.usuarioWebMapper = usuarioWebMapper;
        this.cadastrarUsuarioUseCase = cadastrarUsuarioUseCase;
        this.excluirUsuarioUseCase = excluirUsuarioUseCase;
        this.atualizarUsuarioUseCase = atualizarUsuarioUseCase;
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<ListagemUsuarioDTO> pesquisarPorId(
            @PathVariable Integer id) {

        var usuario = buscarPorIdUseCase.executar(id);
        return ResponseEntity.ok(usuarioWebMapper.paraListagemDto(usuario));
    }

    @GetMapping
    @Override
    public ResponseEntity<List<ListagemUsuarioDTO>> listar() {

        var usuarios = listarUsuarioUseCase.executar();
        return ResponseEntity.ok(
                usuarioWebMapper.paraListagemDtoLista(usuarios));
    }

    @PostMapping
    @Override
    public ResponseEntity<CriacaoUsuarioResponseDTO> cadastrar(
            @Valid @RequestBody CriacaoUsuarioRequestDTO dto) {

        var usuario = cadastrarUsuarioUseCase.executar(
                dto.nome(),
                dto.login(),
                dto.senha(),
                TipoUsuario.valueOf(dto.tipo()));

        var response = usuarioWebMapper.paraCriacaoDto(usuario);
        URI location = URI.create("/api/usuario/" + response.id());

        return ResponseEntity.created(location).body(response);
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        excluirUsuarioUseCase.executar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<AtualizacaoUsuarioResponseDTO> atualizar(
            @PathVariable Integer id,
            @RequestBody AtualizacaoUsuarioRequestDTO dto) {

        if (!dto.temAoMenosUmCampoPreenchido()) {
            throw new NenhumCampoInformadoException(AtualizacaoUsuarioRequestDTO.class);
        }

        TipoUsuario tipo = null;
        if (dto.tipo() != null && !dto.tipo().isBlank()) {
            tipo = TipoUsuario.valueOf(dto.tipo());
        }

        var usuario = atualizarUsuarioUseCase.executar(
                id, dto.nome(), dto.login(), dto.senha(), tipo);

        return ResponseEntity.ok(usuarioWebMapper.paraAtualizacaoDto(usuario));
    }
}
