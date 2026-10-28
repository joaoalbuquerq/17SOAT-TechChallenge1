package com.kap.mechanics_api.adapter.web.dto.movimentacaoestoque;

import com.kap.mechanics_api.core.movimentacaoestoque.domain.TipoMovimentacaoEstoque;

import java.time.LocalDateTime;

public record MovimentacaoEstoqueResponseDTO(
        Integer id,
        Integer itemEstoqueId,
        String itemEstoqueNome,
        TipoMovimentacaoEstoque tipo,
        Integer quantidade,
        LocalDateTime dataHora,
        Integer usuarioId,
        Integer ordemServicoId,
        Integer saldoItemEstoque
) {
}
