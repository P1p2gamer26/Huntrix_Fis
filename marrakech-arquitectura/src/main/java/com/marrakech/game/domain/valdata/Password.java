package com.marrakech.game.domain.valdata;

public final class Password{

    private final String value;

    public Password(String raw){
        if(raw == null || raw.length() < 6){
            throw new IllegalArgumentException("La contraseña debe tener mínimo 6 caracteres");
        }

        this.value = raw;
    }

    public String value(){
        return value;
    }
}
