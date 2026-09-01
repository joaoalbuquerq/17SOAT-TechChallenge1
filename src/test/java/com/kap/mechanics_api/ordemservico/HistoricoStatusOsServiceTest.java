package com.kap.mechanics_api.ordemservico;

import com.kap.mechanics_api.domain.HistoricoStatusOs;
import com.kap.mechanics_api.domain.OrdemServico;
import com.kap.mechanics_api.domain.StatusOrdemServico;
import com.kap.mechanics_api.exception.OrdemServicoNaoEncontradaException;
import com.kap.mechanics_api.repository.HistoricoStatusOsRepository;
import com.kap.mechanics_api.repository.OrdemServicoRepository;
import com.kap.mechanics_api.service.HistoricoStatusOsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistoricoStatusOsServiceTest {

    @Mock
    private HistoricoStatusOsRepository historicoStatusOsRepository;

    @Mock
    private OrdemServicoRepository ordemServicoRepository;

    @InjectMocks
    private HistoricoStatusOsService historicoStatusOsService;

    @Test
    void deveListarHistoricoDaOrdemServicoEmOrdemCronologica() {
        LocalDateTime inicio = LocalDateTime.of(2026, 9, 1, 8, 0);
        OrdemServico ordemServico = new OrdemServico();
        StatusOrdemServico recebida = status("RECEBIDA");
        StatusOrdemServico diagnostico = status("EM_DIAGNOSTICO");

        HistoricoStatusOs primeiro = new HistoricoStatusOs(ordemServico, recebida, inicio);
        primeiro.setDataHoraFim(inicio.plusMinutes(30));
        HistoricoStatusOs segundo = new HistoricoStatusOs(
                ordemServico, diagnostico, inicio.plusMinutes(30));

        when(ordemServicoRepository.existsById(10)).thenReturn(true);
        when(historicoStatusOsRepository.findByOrdemServico_IdOrderByDataHoraInicioAsc(10))
                .thenReturn(List.of(primeiro, segundo));

        var resultado = historicoStatusOsService.buscarPorOrdemServico(10);

        assertThat(resultado)
                .extracting(item -> item.status())
                .containsExactly("RECEBIDA", "EM_DIAGNOSTICO");
        assertThat(resultado.getFirst().dataHoraFim()).isEqualTo(inicio.plusMinutes(30));
        assertThat(resultado.get(1).dataHoraFim()).isNull();
        verify(historicoStatusOsRepository).findByOrdemServico_IdOrderByDataHoraInicioAsc(10);
    }

    @Test
    void deveLancarErroQuandoOrdemServicoNaoExiste() {
        when(ordemServicoRepository.existsById(10)).thenReturn(false);

        assertThatThrownBy(() -> historicoStatusOsService.buscarPorOrdemServico(10))
                .isInstanceOf(OrdemServicoNaoEncontradaException.class);
    }

    private StatusOrdemServico status(String nome) {
        StatusOrdemServico status = new StatusOrdemServico();
        status.setNome(nome);
        return status;
    }
}
