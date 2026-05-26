package com.marrakech.game.presentation.controller;

import com.marrakech.game.repository.ChatRepositorio.Mensaje;
import com.marrakech.game.service.ChatServicio;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.text.SimpleDateFormat;
import java.util.Date;

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
        java.util.List<Mensaje> nuevos = chatSvc.obtenerMensajesDesde(chatSvc.getUltimoMensajeId());
        System.out.println("[CHAT-CTRL] Polling: ultimoId=" + chatSvc.getUltimoMensajeId() + ", nuevos=" + nuevos.size());
        for (Mensaje m : nuevos) {
            System.out.println("[CHAT-CTRL] Mensaje: id=" + m.id + ", usuario=" + m.usuario + ", texto=" + m.texto);
            if (!m.usuario.equals(usuarioActual)) agregarBurbuja(m);
            chatSvc.setUltimoMensajeId(m.id);
        }
        if (!nuevos.isEmpty()) scrollAlFinal();
    }

    public void enviar() {
        if (chatSvc == null || chatInput == null) return;
        String texto = chatInput.getText().trim();
        if (texto.isEmpty()) return;
        chatInput.clear();

        String hora = new SimpleDateFormat("HH:mm").format(new Date());
        Mensaje mLocal = new Mensaje(-1, usuarioActual, texto, hora);
        agregarBurbuja(mLocal);
        scrollAlFinal();

        new Thread(() -> {
            chatSvc.enviar(texto);
            try { Thread.sleep(300); } catch (InterruptedException ignored) {}
            int nuevoId = chatSvc.obtenerUltimoIdMensaje();
            Platform.runLater(() -> chatSvc.setUltimoMensajeId(nuevoId));
        }, "chat-send-local").start();
    }

    private void agregarBurbuja(Mensaje m) {
        boolean esPropio = m.usuario.equals(usuarioActual);

        Label lblTexto = new Label(m.texto);
        lblTexto.setWrapText(true);
        lblTexto.setMaxWidth(155);
        lblTexto.setTextFill(javafx.scene.paint.Color.web(esPropio ? "#1A0A00" : "#FFFFFF"));
        lblTexto.setStyle("-fx-font-size:12px;-fx-font-weight:" + (esPropio ? "bold" : "normal") + ";");

        Label lblMeta = new Label(m.usuario + "  " + m.hora);
        lblMeta.setTextFill(javafx.scene.paint.Color.web(esPropio ? "#5A3010" : "#F0C060"));
        lblMeta.setStyle("-fx-font-size:10px;");

        VBox burbuja = new VBox(2, lblTexto, lblMeta);
        burbuja.setMaxWidth(170);
        if (esPropio) {
            burbuja.setStyle(
                "-fx-background-color:#C9922A;" +
                "-fx-background-radius:10 10 2 10;" +
                "-fx-padding:6 10 6 10;");
        } else {
            burbuja.setStyle(
                "-fx-background-color:rgba(80,45,5,0.95);" +
                "-fx-border-color:#C9922A;" +
                "-fx-border-width:1;" +
                "-fx-background-radius:10 10 10 2;" +
                "-fx-border-radius:10 10 10 2;" +
                "-fx-padding:6 10 6 10;");
        }

        HBox fila = new HBox(burbuja);
        fila.setAlignment(esPropio ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        fila.setPadding(new Insets(1, 0, 1, 0));
        chatBox.getChildren().add(fila);
    }

    public void scrollAlFinal() {
        if (chatScroll != null) { chatScroll.layout(); chatScroll.setVvalue(1.0); }
    }
}