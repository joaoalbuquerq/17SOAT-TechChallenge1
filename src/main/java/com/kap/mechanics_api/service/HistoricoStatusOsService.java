package com.kap.mechanics_api.service;

import com.kap.mechanics_api.dto.ordemservico.HistoricoStatusOsResponseDTO;
import com.kap.mechanics_api.exception.OrdemServicoNaoEncontradaException;
import com.kap.mechanics_api.repository.HistoricoStatusOsRepository;
import com.kap.mechanics_api.repository.OrdemServicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HistoricoStatusOsService {

    private final HistoricoStatusOsRepository historicoStatusOsRepository;
    private final OrdemServicoRepository ordemServicoRepository;

    public HistoricoStatusOsService(HistoricoStatusOsRepository historicoStatusOsRepository,
                                    OrdemServicoRepository ordemServicoRepository) {
        this.historicoStatusOsRepository = historicoStatusOsRepository;
        this.ordemServicoRepository = ordemServicoRepository;
    }

    public List<HistoricoStatusOsResponseDTO> buscarPorOrdemServico(Integer ordemServicoId) {
        if (!ordemServicoRepository.existsById(ordemServicoId)) {
            throw new OrdemServicoNaoEncontradaException(ordemServicoId);
        }

        return historicoStatusOsRepository
                .findByOrdemServico_IdOrderByDataHoraInicioAsc(ordemServicoId)
                .stream()
                .map(historico -> new HistoricoStatusOsResponseDTO(
                        historico.getStatus().getNome(),
                        historico.getDataHoraInicio(),
                        historico.getDataHoraFim()))
                .toList();
    }
}
