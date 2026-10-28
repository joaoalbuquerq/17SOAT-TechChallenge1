package com.kap.mechanics_api.orcamento;

import com.kap.mechanics_api.adapter.persistence.entity.ServicoJpaEntity;

import com.kap.mechanics_api.adapter.persistence.entity.OrcamentoJpaEntity;
import com.kap.mechanics_api.adapter.persistence.entity.OrcamentoServicoJpaEntity;
import com.kap.mechanics_api.adapter.persistence.entity.OrcamentoServicoJpaId;
import com.kap.mechanics_api.adapter.web.dto.orcamento.GeracaoOrcamentoRequestDTO;
import com.kap.mechanics_api.core.orcamento.domain.StatusOrcamento;
import com.kap.mechanics_api.adapter.persistence.repository.OrcamentoRepository;
import com.kap.mechanics_api.adapter.persistence.repository.OrcamentoServicoRepository;
import com.kap.mechanics_api.adapter.persistence.repository.ServicoItemRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrcamentoServicoJpaEntityTest {

    @Test
    void construtorPadraoDeveIniciarComTodosOsCamposNulos() {
        OrcamentoServicoJpaEntity orcamentoServico = new OrcamentoServicoJpaEntity();

        assertNull(orcamentoServico.getId());
        assertNull(orcamentoServico.getOrcamento());
        assertNull(orcamentoServico.getServico());
        assertNull(orcamentoServico.getValorCobrado());
    }

    @Test
    void construtorComArgumentosDevePreencherOrcamentoServicoEValorCobrado() {
        OrcamentoJpaEntity orcamento = new OrcamentoJpaEntity();
        ServicoJpaEntity servico = new ServicoJpaEntity();
        BigDecimal valorCobrado = new BigDecimal("150.00");

        OrcamentoServicoJpaEntity orcamentoServico = new OrcamentoServicoJpaEntity(orcamento, servico, valorCobrado);

        assertEquals(orcamento, orcamentoServico.getOrcamento());
        assertEquals(servico, orcamentoServico.getServico());
        assertEquals(valorCobrado, orcamentoServico.getValorCobrado());
    }

    @Test
    void construtorComArgumentosNaoDeveDefinirIdAutomaticamente() {
        // O id é um @EmbeddedId com @MapsId, então deve ser derivado
        // pelo JPA a partir de orcamento/servico persistidos, não pelo construtor.
        OrcamentoServicoJpaEntity orcamentoServico = new OrcamentoServicoJpaEntity(
                new OrcamentoJpaEntity(), new ServicoJpaEntity(), BigDecimal.TEN);

        assertNull(orcamentoServico.getId());
    }

    @Test
    void setIdDeveAtualizarIdCorretamente() {
        OrcamentoServicoJpaEntity orcamentoServico = new OrcamentoServicoJpaEntity();
        OrcamentoServicoJpaId id = new OrcamentoServicoJpaId(1, 10);

        orcamentoServico.setId(id);

        assertEquals(id, orcamentoServico.getId());
    }

    @Test
    void setOrcamentoDeveAtualizarOrcamentoCorretamente() {
        OrcamentoServicoJpaEntity orcamentoServico = new OrcamentoServicoJpaEntity();
        OrcamentoJpaEntity orcamento = new OrcamentoJpaEntity();
        orcamento.setId(5);

        orcamentoServico.setOrcamento(orcamento);

        assertEquals(orcamento, orcamentoServico.getOrcamento());
        assertEquals(5, orcamentoServico.getOrcamento().getId());
    }

    @Test
    void setServicoDeveAtualizarServicoCorretamente() {
        OrcamentoServicoJpaEntity orcamentoServico = new OrcamentoServicoJpaEntity();
        ServicoJpaEntity servico = new ServicoJpaEntity();
        servico.setId(7);

        orcamentoServico.setServico(servico);

        assertEquals(servico, orcamentoServico.getServico());
        assertEquals(7, orcamentoServico.getServico().getId());
    }

    @Test
    void setValorCobradoDeveAtualizarValorCorretamente() {
        OrcamentoServicoJpaEntity orcamentoServico = new OrcamentoServicoJpaEntity();
        BigDecimal novoValor = new BigDecimal("299.90");

        orcamentoServico.setValorCobrado(novoValor);

        assertEquals(novoValor, orcamentoServico.getValorCobrado());
    }

    @Test
    void setValorCobradoDevePermitirValorZero() {
        OrcamentoServicoJpaEntity orcamentoServico = new OrcamentoServicoJpaEntity();

        orcamentoServico.setValorCobrado(BigDecimal.ZERO);

        assertEquals(0, BigDecimal.ZERO.compareTo(orcamentoServico.getValorCobrado()));
    }

    @Test
    void setValorCobradoDevePermitirValorNulo() {
        OrcamentoServicoJpaEntity orcamentoServico = new OrcamentoServicoJpaEntity(
                new OrcamentoJpaEntity(), new ServicoJpaEntity(), BigDecimal.TEN);

        orcamentoServico.setValorCobrado(null);

        assertNull(orcamentoServico.getValorCobrado());
    }

}
