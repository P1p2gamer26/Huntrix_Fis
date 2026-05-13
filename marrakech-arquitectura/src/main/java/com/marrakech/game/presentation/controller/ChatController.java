package com.marrakech.game.presentation.controller;

import com.marrakech.game.repositorio.ChatRepositorio.Mensaje;
import com.marrakech.game.servicios.ChatServicio;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ChatController {

    private final VBox chatBox;
    private final TextField chatInput;
    private final ScrollPane chatScroll;
    private final VBox chatPanel;
    private final String usuarioActual;

    private ChatServicio chatSvc;
    private String partidaId;

    public ChatController(VBox chatBox, TextField chatInput, ScrollPane chatScroll,
                          VBox chatPanel, String usuarioActual) {
        this.chatBox = chatBox;
        this.chatInput = chatInput;
        this.chatScroll = chatScroll;
        this.chatPanel = chatPanel;
        this.usuarioActual = usuarioActual;
    }

    public void setServicios(ChatServicio chatSvc, String partidaId) {
        this.chatSvc = chatSvc;
        this.partidaId = partidaId;
    }

    public void inicializar(boolean modoMultijugador) {
        if (chatPanel != null) {
            chatPanel.setVisible(modoMultijugador);
            chatPanel.setManaged(modoMultijugador);
        }
    }

    public void cargarHistorial() {
        if (chatSvc == null) return;
        java.util.List<Mensaje> todos = chatSvc.cargarHistorial();
        for (Mensaje m : todos) agregarBurbuja(m);
        scrollAlFinal();
    }

    public void cargarNuevos() {
        if (chatSvc == null) return;
        java.util.List<Mensaje> nuevos = chatSvc.getChatRepo()
            .obtenerMensajes(partidaId, chatSvc.getUltimoMensajeId());
        for (Mensaje m : nuevos) {
            if (!m.usuario.equals(usuarioActual)) agregarBurbuja(m);
            chatSvc.setUltimoMensajeId(m.id);
        }
        scrollAlFinal();
    }

    public void enviar() {
        if (chatSvc == null || chatInput == null) return;
        String texto = chatInput.getText().trim();
        if (texto.isEmpty()) return;
        chatInput.clear();
        chatSvc.enviar(texto);

        new Thread(() -> {
            int nuevoId = chatSvc.getChatRepo().obtenerUltimoId(partidaId);
            String hora = new java.text.SimpleDateFormat("HH:mm").format(new java.util.Date());
            Mensaje m = new Mensaje(nuevoId, usuarioActual, texto, hora);
            Platform.runLater(() -> {
                chatSvc.setUltimoMensajeId(nuevoId);
                agregarBurbuja(m);
                scrollAlFinal();
            });
        }, "chat-send-local").start();
    }

    private void agregarBurbuja(Mensaje m) {
        boolean esPropio = m.usuario.equals(usuarioActual);
        Label lblTexto = new Label(m.texto);
        lblTexto.setWrapText(true); lblTexto.setMaxWidth(160);
        lblTexto.setStyle("-fx-font-size:12px;-fx-text-fill:" + (esPropio ? "#1A0A00" : "#F0E0B0") + ";");
        Label lblMeta = new Label(m.usuario + "  " + m.hora);
        lblMeta.setStyle("-fx-font-size:10px;-fx-text-fill:" + (esPropio ? "#5A3010" : "#9E7A3A") + ";");
        VBox burbuja = new VBox(2, lblTexto, lblMeta);
        burbuja.getStyleClass().add(esPropio ? "burbuja-propia" : "burbuja-ajena");
        burbuja.setMaxWidth(170);
        HBox fila = new HBox(burbuja);
        fila.setAlignment(esPropio ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        fila.setPadding(new Insets(1, 0, 1, 0));
        chatBox.getChildren().add(fila);
    }

    public void scrollAlFinal() {
        if (chatScroll != null) { chatScroll.layout(); chatScroll.setVvalue(1.0); }
    }
}
