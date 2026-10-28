package com.kap.mechanics_api.adapter.web;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;

public class NenhumCampoInformadoException extends RuntimeException {

    private final List<String> camposDisponiveis;

    public NenhumCampoInformadoException(Class<? extends Record> dtoClass) {
        super("Nenhum campo foi informado para atualização");
        this.camposDisponiveis = Arrays.stream(dtoClass.getRecordComponents())
                .map(RecordComponent::getName)
                .toList();
    }

    public List<String> getCamposDisponiveis() {
        return camposDisponiveis;
    }
}
