package com.marrakech.game.service;

import java.util.List;

import com.marrakech.game.repository.ChatRepositorio.Mensaje;
import com.marrakech.game.repository.IChatRepositorio;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class ChatServicio {

    private final IChatRepositorio chatRepo;
    private String   partidaId;
    private String   usuarioActual;
    private int      ultimoMensajeId;
    private Timeline chatTimeline;

    public ChatServicio(IChatRepositorio chatRepo) {
        this.chatRepo = chatRepo;
    }

    public void inicializar(String partidaId, String usuarioActual) {
        this.partidaId      = partidaId;
        this.usuarioActual  = usuarioActual;
        chatRepo.inicializarTabla();
        this.ultimoMensajeId = chatRepo.obtenerUltimoId(partidaId);
    }

    public List<Mensaje> cargarHistorial() {
        List<Mensaje> historial = chatRepo.obtenerMensajes(partidaId, 0);
        if (!historial.isEmpty())
            ultimoMensajeId = historial.get(historial.size() - 1).id;
        return historial;
    }

    public void iniciarPolling(Runnable onNuevos) {
        if (chatTimeline != null) chatTimeline.stop();
            chatTimeline = new Timeline(new KeyFrame(Duration.millis(500), e -> {
        System.out.println("[CHAT-POLLING] partidaId=" + partidaId + ", ultimoId=" + ultimoMensajeId);
            onNuevos.run();
        }));
        chatTimeline.setCycleCount(Timeline.INDEFINITE);
        chatTimeline.play();
        System.out.println("[CHAT-POLLING] Iniciado para partidaId=" + partidaId);
    }

    public void enviar(String texto) {
        if (texto == null || texto.isEmpty()) return;
        new Thread(() -> chatRepo.enviarMensaje(partidaId, usuarioActual, texto), "chat-send").start();
    }

    public void detenerPolling() {
        if (chatTimeline != null) { chatTimeline.stop(); chatTimeline = null; }
    }

    public List<Mensaje> obtenerMensajesDesde(int ultimoId) {
        return chatRepo.obtenerMensajes(partidaId, ultimoId);
    }

    public int obtenerUltimoIdMensaje() {
        return chatRepo.obtenerUltimoId(partidaId);
    }

    public String getPartidaId()               { return partidaId; }
    public String getUsuarioActual()           { return usuarioActual; }
    public int    getUltimoMensajeId()         { return ultimoMensajeId; }
    public void   setUltimoMensajeId(int id)   { this.ultimoMensajeId = id; }
}