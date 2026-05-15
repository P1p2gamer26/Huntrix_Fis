package com.marrakech.game.repository;

public interface IEstadoJuegoRepositorio {
    void guardar(String partidaId, int turno, int ax, int ay, int adir, String tableroJson);
    String cargarUltimo(String partidaId);
}
