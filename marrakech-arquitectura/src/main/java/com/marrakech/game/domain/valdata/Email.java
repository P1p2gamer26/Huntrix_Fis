package com.marrakech.game.domain.valdata;

public final class Email{

    private final String value;

    public Email(String raw){
        if (raw == null) {
            throw new IllegalArgumentException("El correo no puede ser null");
        }

        String v = raw.trim().toLowerCase();

        if(!v.matches("^[\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$")){
            throw new IllegalArgumentException("Correo inválido");
        }

        this.value = v;
    }

    public String value() {
        return value;
    }
}
