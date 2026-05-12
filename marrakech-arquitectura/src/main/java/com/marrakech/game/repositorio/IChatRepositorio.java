package com.marrakech.game.repositorio;

import com.marrakech.game.repositorio.ChatRepositorio.Mensaje;

import java.util.List;

/** Contrato de persistencia para mensajes de chat. */
public interface IChatRepositorio {
    void inicializarTabla();
    void enviarMensaje(String partidaId, String usuario, String texto);
    List<Mensaje> obtenerMensajes(String partidaId, int desdeId);
    int obtenerUltimoId(String partidaId);
}
