package com.marrakech.game.repository;

public interface IEstadoJuegoRepositorio {
    void guardar(String partidaId, int turno, int ax, int ay, int adir, String tableroJson);
    String cargarUltimo(String partidaId);
    void limpiar(String partidaId);
    /** Marca el último estado de la partida como listo para que los otros jugadores lo lean. */
    void marcarListo(String partidaId);
    /** Devuelve true si el último estado guardado está marcado como listo. */
    boolean estaListo(String partidaId);
    /** Desmarca el estado (el jugador actual acaba de empezar su turno). */
    void desmarcarListo(String partidaId);
}
