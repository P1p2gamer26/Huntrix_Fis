package com.marrakech.game.service;

import com.marrakech.game.repository.IChatRepositorio;
import com.marrakech.game.repository.ChatRepositorio.Mensaje;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.util.Duration;

import java.util.List;

/**
 * Gestiona el chat en partida: historial, polling y envío de mensajes.
 * Recibe IChatRepositorio por constructor (inyección de dependencias).
 */
public class ChatServicio {

    private final IChatRepositorio chatRepo;

    private String   partidaId;
    private String   usuarioActual;
    private int      ultimoMensajeId;
    private Timeline chatTimeline;

    public ChatServicio(IChatRepositorio chatRepo) {
        this.chatRepo = chatRepo;
    }

    /** Prepara el servicio para una partida concreta. */
    public void inicializar(String partidaId, String usuarioActual) {
        this.partidaId      = partidaId;
        this.usuarioActual  = usuarioActual;
        chatRepo.inicializarTabla();
        this.ultimoMensajeId = chatRepo.obtenerUltimoId(partidaId);
    }

    /** Carga todos los mensajes anteriores (historial al entrar). */
    public List<Mensaje> cargarHistorial() {
        List<Mensaje> historial = chatRepo.obtenerMensajes(partidaId, 0);
        if (!historial.isEmpty())
            ultimoMensajeId = historial.get(historial.size() - 1).id;
        return historial;
    }

    /** Arranca el polling cada 1 segundo; llama onNuevosMensajes en el hilo UI. */
    public void iniciarPolling(Runnable onNuevosMensajes) {
        chatTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e ->
            new Thread(() -> {
                List<Mensaje> nuevos = chatRepo.obtenerMensajes(partidaId, ultimoMensajeId);
                if (!nuevos.isEmpty()) {
                    ultimoMensajeId = nuevos.get(nuevos.size() - 1).id;
                    Platform.runLater(onNuevosMensajes);
                }
            }, "chat-poller").start()
        ));
        chatTimeline.setCycleCount(Timeline.INDEFINITE);
        chatTimeline.play();
    }

    /** Envía un mensaje en hilo separado para no bloquear la UI. */
    public void enviar(String texto) {
        if (texto == null || texto.isEmpty()) return;
        new Thread(() -> chatRepo.enviarMensaje(partidaId, usuarioActual, texto),
                   "chat-send").start();
    }

    public void detenerPolling() {
        if (chatTimeline != null) { chatTimeline.stop(); chatTimeline = null; }
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public IChatRepositorio getChatRepo()      { return chatRepo; }
    public String getPartidaId()               { return partidaId; }
    public String getUsuarioActual()           { return usuarioActual; }
    public int    getUltimoMensajeId()         { return ultimoMensajeId; }
    public void   setUltimoMensajeId(int id)   { this.ultimoMensajeId = id; }
}
