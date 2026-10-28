package com.kap.mechanics_api.adapter.web.dto.ordemservico;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
public record InclusaoOrdemServicoItemRequestDTO(Integer servicoId, Integer itemEstoqueId, @NotNull @Min(1) Integer quantidade) {}
