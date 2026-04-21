package com.marrakech.game.domain.valdata;

public final class Nombre{

    private final String value;

    public Nombre(String raw){
        if (raw == null || raw.trim().isEmpty()){
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }

        this.value = raw.trim();
    }

    public String value() {
        return value;
    }
}
