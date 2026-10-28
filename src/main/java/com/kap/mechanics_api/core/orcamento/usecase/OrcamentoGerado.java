package com.kap.mechanics_api.core.orcamento.usecase;

import com.kap.mechanics_api.core.orcamento.domain.Orcamento;
import com.kap.mechanics_api.core.ordemservico.domain.OrdemServico;

/** Resultado da abertura: o orçamento pendente e a ordem de serviço aberta junto com ele. */
public record OrcamentoGerado(Orcamento orcamento, OrdemServico ordemServico) {
}
