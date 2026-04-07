package com.marrakech.game.presentation.views;

import com.marrakech.game.infrastructure.persistence.JugadorRepository;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;

public class PerfilView extends StackPane {

    private Button btnVolver;
    private StackPane avatarContainer;
    private final JugadorRepository jugadorRepo = new JugadorRepository();

    private final String nombreUsuario;
    private final String correo;
    private final String estado;
    private final String fechaRegistro;
    private final int    victorias;

    public PerfilView(String nombreUsuario, String correo, String estado,
                      String fechaRegistro, int victorias) {
        this.nombreUsuario = nombreUsuario;
        this.correo        = correo;
        this.estado        = estado;
        this.fechaRegistro = fechaRegistro;
        this.victorias     = victorias;
        configurarFondo();
        configurarContenido();
    }

    private void configurarFondo() {
        Image imagen = new Image(getClass().getResourceAsStream("/images/background.jpg"));
        BackgroundImage bgImage = new BackgroundImage(imagen,
            BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.CENTER,
            new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true));
        setBackground(new Background(bgImage));
        Pane overlay = new Pane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.68);");
        overlay.prefWidthProperty().bind(widthProperty());
        overlay.prefHeightProperty().bind(heightProperty());
        getChildren().add(overlay);
    }

    private void configurarContenido() {
        VBox panel = new VBox(22);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setPadding(new Insets(40, 52, 40, 52));
        panel.setMaxWidth(520);
        panel.setStyle(
            "-fx-background-color: rgba(10,4,0,0.92);" +
            "-fx-border-color: #8B6914;" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;"
        );
        DropShadow sombra = new DropShadow();
        sombra.setColor(Color.web("#000000", 0.85)); sombra.setRadius(30);
        panel.setEffect(sombra);

        panel.getChildren().addAll(
            construirAvatar(),
            crearSeparador(),
            construirDatos(),
            btnVolver = crearBotonContorno("VOLVER AL MENÚ")
        );
        getChildren().add(panel);
    }

    private VBox construirAvatar() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);

        avatarContainer = new StackPane();
        avatarContainer.setMaxWidth(96);
        avatarContainer.setMaxHeight(96);
        avatarContainer.setCursor(Cursor.HAND);

        Circle circulo = new Circle(48);
        circulo.setFill(Color.web("#1A0800"));
        circulo.setStroke(Color.web("#C9922A"));
        circulo.setStrokeWidth(2.5);

        actualizarFotoEnAvatar(circulo);

        DropShadow ds = new DropShadow();
        ds.setColor(Color.web("#C9922A", 0.4)); ds.setRadius(12);
        avatarContainer.setEffect(ds);

        Text editarHint = new Text("✎ cambiar foto");
        editarHint.setFont(Font.font("Georgia", 10));
        editarHint.setFill(Color.web("#9E7A3A"));

        avatarContainer.setOnMouseClicked(e -> abrirSelectorFoto());
        avatarContainer.setOnMouseEntered(e ->
            avatarContainer.setStyle("-fx-opacity: 0.80;"));
        avatarContainer.setOnMouseExited(e ->
            avatarContainer.setStyle("-fx-opacity: 1.0;"));

        Text nombre = new Text(nombreUsuario);
        nombre.setFont(Font.font("Georgia", FontWeight.BOLD, 22));
        nombre.setFill(Color.web("#D4A017"));

        boolean activo = estado != null && estado.equalsIgnoreCase("Activo");
        Text etEstado = new Text("● " + (estado == null || estado.isEmpty() ? "Activo" : estado));
        etEstado.setFont(Font.font("Georgia", 13));
        etEstado.setFill(activo ? Color.web("#2ecc71") : Color.web("#e74c3c"));

        box.getChildren().addAll(avatarContainer, editarHint, nombre, etEstado);
        return box;
    }

    private void actualizarFotoEnAvatar(Circle circulo) {
        avatarContainer.getChildren().clear();
        avatarContainer.getChildren().add(circulo);

        byte[] fotoBytes = jugadorRepo.getFoto(nombreUsuario);
        if (fotoBytes != null && fotoBytes.length > 0) {
            Image img = new Image(new ByteArrayInputStream(fotoBytes));
            ImageView iv = new ImageView(img);
            iv.setFitWidth(96);
            iv.setFitHeight(96);
            iv.setPreserveRatio(false);
            iv.setClip(new Circle(48, 48, 48));
            avatarContainer.getChildren().add(iv);
        } else {
            Text icono = new Text("👤");
            icono.setFont(Font.font(42));
            avatarContainer.getChildren().add(icono);
        }
    }

    private void abrirSelectorFoto() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Seleccionar foto de perfil");
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        Stage ventana = (Stage) getScene().getWindow();
        File archivo = fc.showOpenDialog(ventana);

        if (archivo != null) {
            boolean ok = jugadorRepo.guardarFoto(nombreUsuario, archivo);
            if (ok) {
                Circle circulo = new Circle(48);
                circulo.setFill(Color.web("#1A0800"));
                circulo.setStroke(Color.web("#C9922A"));
                circulo.setStrokeWidth(2.5);
                actualizarFotoEnAvatar(circulo);
            }
        }
    }

    private Separator crearSeparador() {
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #4A3000;");
        return sep;
    }

    private VBox construirDatos() {
        VBox datos = new VBox(14);
        datos.setMaxWidth(Double.MAX_VALUE);
        datos.getChildren().addAll(
            crearCampo("Nombre de usuario", nombreUsuario),
            crearCampo("Correo",            correo == null || correo.isEmpty() ? "—" : correo),
            crearCampo("Fecha de registro", fechaRegistro == null || fechaRegistro.isEmpty() ? "—" : fechaRegistro),
            crearCampo("Victorias",         String.valueOf(victorias))
        );
        return datos;
    }

    private VBox crearCampo(String etiqueta, String valor) {
        Label lbl = new Label(etiqueta);
        lbl.setFont(Font.font("Georgia", FontWeight.BOLD, 11));
        lbl.setTextFill(Color.web("#9E7A3A"));

        Label val = new Label(valor);
        val.setFont(Font.font("Georgia", 15));
        val.setTextFill(Color.web("#E8D090"));
        val.setPadding(new Insets(7, 14, 7, 14));
        val.setMaxWidth(Double.MAX_VALUE);
        val.setStyle(
            "-fx-background-color: rgba(255,255,255,0.04);" +
            "-fx-border-color: #4A3000;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;"
        );
        return new VBox(4, lbl, val);
    }

    private Button crearBotonContorno(String texto) {
        Button btn = new Button(texto);
        btn.setPrefWidth(200); btn.setPrefHeight(42);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        String n = "-fx-background-color:transparent;-fx-text-fill:#D4A017;-fx-border-color:#8B6914;-fx-border-width:1.5;-fx-border-radius:4;-fx-background-radius:4;-fx-cursor:hand;";
        String h = "-fx-background-color:rgba(201,146,42,0.18);-fx-text-fill:#F0D060;-fx-border-color:#D4A017;-fx-border-width:1.5;-fx-border-radius:4;-fx-background-radius:4;-fx-cursor:hand;";
        btn.setStyle(n); btn.setOnMouseEntered(e->btn.setStyle(h)); btn.setOnMouseExited(e->btn.setStyle(n));
        return btn;
    }

    public Button getBtnVolver() { return btnVolver; }
}