package com.kap.mechanics_api.movimentacaoestoque;

import com.kap.mechanics_api.domain.ItemEstoque;
import com.kap.mechanics_api.domain.MovimentacaoEstoque;
import com.kap.mechanics_api.domain.OrdemServico;
import com.kap.mechanics_api.domain.Usuario;
import com.kap.mechanics_api.dto.movimentacaoestoque.RegistroEntradaMovimentacaoEstoqueRequestDTO;
import com.kap.mechanics_api.dto.movimentacaoestoque.RegistroSaidaMovimentacaoEstoqueRequestDTO;
import com.kap.mechanics_api.enums.TipoItemEstoque;
import com.kap.mechanics_api.enums.TipoMovimentacaoEstoque;
import com.kap.mechanics_api.enums.TipoUsuario;
import com.kap.mechanics_api.exception.*;
import com.kap.mechanics_api.repository.ItemEstoqueRepository;
import com.kap.mechanics_api.repository.MovimentacaoEstoqueRepository;
import com.kap.mechanics_api.repository.OrdemServicoRepository;
import com.kap.mechanics_api.repository.OrcamentoServicoRepository;
import com.kap.mechanics_api.repository.ServicoItemRepository;
import com.kap.mechanics_api.repository.UsuarioRepository;
import com.kap.mechanics_api.service.MovimentacaoEstoqueService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovimentacaoEstoqueServiceTest {

    @Mock
    private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    @Mock
    private ItemEstoqueRepository itemEstoqueRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private OrdemServicoRepository ordemServicoRepository;

    @InjectMocks
    private MovimentacaoEstoqueService service;

    @Test
    void deveRegistrarEntradaEAtualizarSaldo() {
        ItemEstoque item = item(1, 10);
        Usuario usuario = usuario(7);
        RegistroEntradaMovimentacaoEstoqueRequestDTO request =
                new RegistroEntradaMovimentacaoEstoqueRequestDTO(1, 5);

        when(itemEstoqueRepository.findByIdForUpdate(1)).thenReturn(Optional.of(item));
        when(usuarioRepository.findByLogin("usuario7")).thenReturn(Optional.of(usuario));
        when(itemEstoqueRepository.save(item)).thenReturn(item);
        when(movimentacaoEstoqueRepository.save(any(MovimentacaoEstoque.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.registrarEntrada(request, "usuario7");

        assertEquals(TipoMovimentacaoEstoque.ENTRADA, response.tipo());
        assertEquals(15, response.saldoItemEstoque());
        assertEquals(15, item.getQuantidadeAtual());
        verify(itemEstoqueRepository).save(item);
        verify(movimentacaoEstoqueRepository).save(any(MovimentacaoEstoque.class));
    }

    @Test
    void deveRegistrarSaidaEAtualizarSaldo() {
        ItemEstoque item = item(2, 12);
        Usuario usuario = usuario(8);
        OrdemServico ordemServico = new OrdemServico();
        ordemServico.setId(55);
        ordemServico.setStatusOrdemServico(statusEmExecucao());
        RegistroSaidaMovimentacaoEstoqueRequestDTO request =
                new RegistroSaidaMovimentacaoEstoqueRequestDTO(2, 4, 55);

        when(itemEstoqueRepository.findByIdForUpdate(2)).thenReturn(Optional.of(item));
        when(usuarioRepository.findByLogin("usuario8")).thenReturn(Optional.of(usuario));
        when(ordemServicoRepository.findById(55)).thenReturn(Optional.of(ordemServico));
        when(itemEstoqueRepository.save(item)).thenReturn(item);
        when(movimentacaoEstoqueRepository.save(any(MovimentacaoEstoque.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.registrarSaida(request, "usuario8");

        assertEquals(TipoMovimentacaoEstoque.SAIDA, response.tipo());
        assertEquals(8, response.saldoItemEstoque());
        assertEquals(8, item.getQuantidadeAtual());
    }

    @Test
    void deveRejeitarSaidaQuandoEstoqueForInsuficiente() {
        ItemEstoque item = item(3, 2);
        Usuario usuario = usuario(9);
        OrdemServico ordemServico = new OrdemServico();
        ordemServico.setId(10);
        ordemServico.setStatusOrdemServico(statusEmExecucao());

        when(itemEstoqueRepository.findByIdForUpdate(3)).thenReturn(Optional.of(item));
        when(usuarioRepository.findByLogin("usuario9")).thenReturn(Optional.of(usuario));
        when(ordemServicoRepository.findById(10)).thenReturn(Optional.of(ordemServico));

        assertThrows(EstoqueInsuficienteException.class, () ->
                service.registrarSaida(new RegistroSaidaMovimentacaoEstoqueRequestDTO(3, 5, 10), "usuario9"));
    }

    @Test
    void deveRetornarErroQuandoItemNaoExiste() {
        when(itemEstoqueRepository.findByIdForUpdate(1)).thenReturn(Optional.empty());

        assertThrows(ItemEstoqueNaoEncontradoException.class, () ->
                service.registrarEntrada(new RegistroEntradaMovimentacaoEstoqueRequestDTO(1, 1), "usuario1"));
    }

    @Test
    void deveRetornarErroQuandoUsuarioNaoExiste() {
        when(itemEstoqueRepository.findByIdForUpdate(1)).thenReturn(Optional.of(item(1, 1)));
        when(usuarioRepository.findByLogin("usuario1")).thenReturn(Optional.empty());

        assertThrows(UsuarioNaoEncontradoException.class, () ->
                service.registrarEntrada(new RegistroEntradaMovimentacaoEstoqueRequestDTO(1, 1), "usuario1"));
    }

    @Test
    void deveRetornarErroQuandoOrdemServicoNaoExisteNaSaida() {
        when(itemEstoqueRepository.findByIdForUpdate(1)).thenReturn(Optional.of(item(1, 5)));
        when(usuarioRepository.findByLogin("usuario1")).thenReturn(Optional.of(usuario(1)));
        when(ordemServicoRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(OrdemServicoNaoEncontradaException.class, () ->
                service.registrarSaida(new RegistroSaidaMovimentacaoEstoqueRequestDTO(1, 1, 99), "usuario1"));
    }

    @Test
    void deveListarMovimentacoesPorItem() {
        when(movimentacaoEstoqueRepository.findByItemEstoque_IdOrderByDataHoraDesc(1))
                .thenReturn(java.util.List.of());

        var resultado = service.listarPorItem(1);

        assertTrue(resultado.isEmpty());
        verify(movimentacaoEstoqueRepository).findByItemEstoque_IdOrderByDataHoraDesc(1);
    }

    @Test
    void deveListarMovimentacoesPorOrdemServico() {
        when(movimentacaoEstoqueRepository.findByOrdemServico_IdOrderByDataHoraDesc(55))
                .thenReturn(java.util.List.of());

        var resultado = service.listarPorOrdemServico(55);

        assertTrue(resultado.isEmpty());
        verify(movimentacaoEstoqueRepository).findByOrdemServico_IdOrderByDataHoraDesc(55);
    }

    @Test
    void deveListarMovimentacoesPorTipo() {
        when(movimentacaoEstoqueRepository.findByTipoOrderByDataHoraDesc(TipoMovimentacaoEstoque.ENTRADA))
                .thenReturn(java.util.List.of());

        var resultado = service.listarPorTipo(TipoMovimentacaoEstoque.ENTRADA);

        assertTrue(resultado.isEmpty());
        verify(movimentacaoEstoqueRepository).findByTipoOrderByDataHoraDesc(TipoMovimentacaoEstoque.ENTRADA);
    }

    @Test
    void deveListarMovimentacoesPorPeriodo() {
        LocalDateTime inicio = LocalDateTime.of(2026, 8, 1, 0, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 8, 31, 23, 59);
        when(movimentacaoEstoqueRepository.findByDataHoraBetweenOrderByDataHoraDesc(inicio, fim))
                .thenReturn(java.util.List.of());

        var resultado = service.listarPorPeriodo(inicio, fim);

        assertTrue(resultado.isEmpty());
        verify(movimentacaoEstoqueRepository).findByDataHoraBetweenOrderByDataHoraDesc(inicio, fim);
    }

    @Test
    void deveRejeitarPeriodoInvertido() {
        LocalDateTime inicio = LocalDateTime.of(2026, 8, 31, 23, 59);
        LocalDateTime fim = LocalDateTime.of(2026, 8, 1, 0, 0);

        assertThrows(PeriodoMovimentacaoInvalidoException.class, () -> service.listarPorPeriodo(inicio, fim));
    }

    @Test
    void devePesquisarPorId(){

        //Preparar o cenario
        MovimentacaoEstoque movimentacaoEstoque = new MovimentacaoEstoque();
        movimentacaoEstoque.setId(1);

        when(movimentacaoEstoqueRepository.findById(1)).thenReturn(Optional.of(movimentacaoEstoque));

        MovimentacaoEstoque movimentacao = service.pesquisarPorId(1);

        assertSame(movimentacao, movimentacaoEstoque);

        verify(movimentacaoEstoqueRepository).findById(1);

    }

    @Test
    void deveLancarErroQuandoMovimentacaoNaoExiste() {
        when(movimentacaoEstoqueRepository.findById(1))
                .thenReturn(Optional.empty());

        assertThrows(MovimentacaoEstoqueNaoEncontradaException.class,
                () -> service.pesquisarPorId(1));

        verify(movimentacaoEstoqueRepository).findById(1);
    }

    @Test
    void deveValidarSeItemEstaAtivo() {
        ItemEstoque item = item(1, 10);
        item.setAtivo(false);
        Usuario usuario = usuario(1);
        RegistroEntradaMovimentacaoEstoqueRequestDTO request =
                new RegistroEntradaMovimentacaoEstoqueRequestDTO(1, 5);

        when(itemEstoqueRepository.findByIdForUpdate(item.getId()))
                .thenReturn(Optional.of(item));
        when(usuarioRepository.findByLogin("usuario1"))
                .thenReturn(Optional.of(usuario));

        assertThrows(ItemEstoqueInativoException.class, () ->
                service.registrarEntrada(request, "usuario1"));

        verify(itemEstoqueRepository, never()).save(any(ItemEstoque.class));
        verify(movimentacaoEstoqueRepository, never()).save(any(MovimentacaoEstoque.class));
    }

    private ItemEstoque item(Integer id, int quantidadeAtual) {
        ItemEstoque item = new ItemEstoque();
        item.setId(id);
        item.setNome("Item " + id);
        item.setDescricao("Descricao");
        item.setTipoItemEstoque(TipoItemEstoque.PECA);
        item.setValorUnitario(new BigDecimal("10.00"));
        item.setQuantidadeAtual(quantidadeAtual);
        item.setQuantidadeMinima(1);
        item.setAtivo(true);
        return item;
    }

    private Usuario usuario(Integer id) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome("Usuario");
        usuario.setLogin("usuario" + id);
        usuario.setSenhaHash("hash");
        usuario.setTipo(TipoUsuario.ESTOQUISTA);
        return usuario;
    }

    private com.kap.mechanics_api.domain.StatusOrdemServico statusEmExecucao() {
        var status = new com.kap.mechanics_api.domain.StatusOrdemServico();
        status.setNome("EM_EXECUCAO");
        return status;
    }
}
