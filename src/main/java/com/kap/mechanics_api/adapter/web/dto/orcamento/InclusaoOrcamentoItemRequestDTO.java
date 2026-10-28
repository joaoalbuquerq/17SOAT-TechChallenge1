package com.kap.mechanics_api.adapter.web.dto.orcamento;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
public record InclusaoOrcamentoItemRequestDTO(Integer servicoId, Integer itemEstoqueId, @NotNull @Min(1) Integer quantidade) {}
