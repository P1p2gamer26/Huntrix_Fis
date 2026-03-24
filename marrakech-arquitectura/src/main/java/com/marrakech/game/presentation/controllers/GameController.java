package com.marrakech.game.presentation.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import java.util.Random;

public class GameController {

    @FXML private VBox startScreen;
    @FXML private HBox gameScreen;
    @FXML private VBox endScreen;
    @FXML private GridPane boardGrid;
    @FXML private Label turnLabel;
    @FXML private Label statusLabel;
    @FXML private Button rollDiceBtn;
    @FXML private VBox panelJ1, panelJ2, panelJ3, panelJ4;
    @FXML private Label rugsJ1, rugsJ2, rugsJ3, rugsJ4;
    @FXML private Label moneyJ1, moneyJ2, moneyJ3, moneyJ4;
    @FXML private Label winnerLabel;
    @FXML private Label finalScores;

    private ImageView assamView;
    private int assamX = 3, assamY = 3;
    private int assamDir = 0;

    private enum Phase { MOVE, CARPET_1, CARPET_2 }
    private Phase currentPhase = Phase.MOVE;

    private int numPlayers = 2;
    private int currentPlayerIdx = 0;

    private int[]   money;
    private int[]   rugs;
    private Image[] carpetImages;

    private String[] playerColors = {"#e74c3c","#3498db","#2ecc71","#f39c12"};
    private String[] playerNames  = {"J1 (ROJO)","J2 (AZUL)","J3 (VERDE)","J4 (AMARILLO)"};

    // Tamaño de cada celda en píxeles
    private static final int CELL = 61;

    private StackPane[][] tiles     = new StackPane[7][7];
    private int[][]       tileOwner = new int[7][7];

    private int     firstCarpetX = -1, firstCarpetY = -1;
    private Boolean carpetHorizontal = null;

    public void initialize() {}

    @FXML private void startWith2() { startGame(2); }
    @FXML private void startWith3() { startGame(3); }
    @FXML private void startWith4() { startGame(4); }

    private void startGame(int n) {
        numPlayers = n;
        money = new int[n];
        rugs  = new int[n];
        for (int i = 0; i < n; i++) { money[i] = 30; rugs[i] = 15; }

        String[] imgPaths = {
            "/images/alfombra tablero roja.png",
            "/images/alfombra tablero azul.png",
            "/images/alfombra tablero morada.png",
            "/images/alfombra tablero amarillo.png"
        };
        carpetImages = new Image[n];
        for (int i = 0; i < n; i++) {
            try { carpetImages[i] = new Image(getClass().getResourceAsStream(imgPaths[i])); }
            catch (Exception e) { System.out.println("Sin imagen J" + (i+1)); }
        }

        panelJ3.setVisible(n >= 3); panelJ3.setManaged(n >= 3);
        panelJ4.setVisible(n >= 4); panelJ4.setManaged(n >= 4);

        boardGrid.getChildren().clear();
        for (int row = 0; row < 7; row++) {
            for (int col = 0; col < 7; col++) {
                tileOwner[col][row] = 0;
                StackPane tile = new StackPane();
                tile.setPrefSize(CELL, CELL);
                tile.getStyleClass().add("tile");
                final int r = row, c = col;
                tile.setOnMouseClicked(e -> handleTileClick(c, r));
                tiles[col][row] = tile;
                boardGrid.add(tile, col, row);
            }
        }

        assamX = 3; assamY = 3; assamDir = 0;
        try {
            Image img = new Image(getClass().getResourceAsStream("/images/assam.png"));
            assamView = new ImageView(img);
        } catch (Exception e) { assamView = new ImageView(); }
        assamView.setFitWidth(50); assamView.setFitHeight(58);
        assamView.setMouseTransparent(true);
        boardGrid.add(assamView, assamX, assamY);

        currentPlayerIdx = 0;
        currentPhase = Phase.MOVE;

        startScreen.setVisible(false);
        endScreen.setVisible(false);
        gameScreen.setVisible(true);

        actualizarUI();
    }

    @FXML protected void rotateLeft() {
        if (currentPhase != Phase.MOVE) return;
        assamDir = (assamDir + 3) % 4;
        assamView.setRotate(assamDir * 90);
    }

    @FXML protected void rotateRight() {
        if (currentPhase != Phase.MOVE) return;
        assamDir = (assamDir + 1) % 4;
        assamView.setRotate(assamDir * 90);
    }

    @FXML protected void onRollDiceClick() {
        if (currentPhase != Phase.MOVE) return;

        int pasos = new Random().nextInt(4) + 1;
        moverAssam(pasos);

        GridPane.setColumnIndex(assamView, assamX);
        GridPane.setRowIndex(assamView, assamY);
        assamView.toFront();

        int dueno = tileOwner[assamX][assamY];
        if (dueno != 0 && dueno != currentPlayerIdx + 1) {
            int pago = contarContiguas(assamX, assamY, dueno);
            money[currentPlayerIdx] = Math.max(0, money[currentPlayerIdx] - pago);
            money[dueno - 1] += pago;
            statusLabel.setText("Dado: " + pasos + " — Pagas " + pago + " Dh a J" + dueno + ". Coloca tu alfombra.");
        } else {
            statusLabel.setText("Dado: " + pasos + " — Click en casilla adyacente a Assam para la 1ra mitad.");
        }

        actualizarUI();

        if (rugs[currentPlayerIdx] > 0) {
            currentPhase = Phase.CARPET_1;
            rollDiceBtn.setDisable(true);
        } else {
            pasarTurno();
        }
    }

    // Carril oficial del borde en sentido horario:
    // top (y=0) izq→der, right (x=6) arr→aba, bottom (y=6) der→izq, left (x=0) aba→arr
    // Cada posicion del borde tiene un indice 0..23 y una direccion de salida asociada.
    private int borderIndex(int x, int y) {
        if (y == 0 && x < 6)  return x;           // top:    0..5  → dir Este
        if (x == 6 && y < 6)  return 6 + y;        // right:  6..11 → dir Sur
        if (y == 6 && x > 0)  return 12 + (6 - x); // bottom: 12..17→ dir Oeste
        if (x == 0 && y > 0)  return 18 + (6 - y); // left:   18..23→ dir Norte
        return -1; // interior
    }

    private int[] borderPos(int idx) {
        idx = ((idx % 24) + 24) % 24;
        if (idx < 6)  return new int[]{idx, 0};
        if (idx < 12) return new int[]{6, idx - 6};
        if (idx < 18) return new int[]{6 - (idx - 12), 6};
        return new int[]{0, 6 - (idx - 18)};
    }

    // Direccion de avance segun segmento del borde
    private int borderDir(int idx) {
        idx = ((idx % 24) + 24) % 24;
        if (idx < 6)  return 1; // Este
        if (idx < 12) return 2; // Sur
        if (idx < 18) return 3; // Oeste
        return 0;               // Norte
    }

    private void moverAssam(int pasos) {
        for (int p = 0; p < pasos; p++) {
            int nx = assamX, ny = assamY;
            switch (assamDir) {
                case 0: ny--; break;
                case 1: nx++; break;
                case 2: ny++; break;
                case 3: nx--; break;
            }

            boolean fueraBorde = nx < 0 || nx > 6 || ny < 0 || ny > 6;

            if (fueraBorde) {
                // Assam sale del tablero: entrar al carril del borde
                // Buscar la posicion del borde mas cercana al punto de salida
                // La regla oficial: entra por el borde perpendicular en sentido horario
                // Simplificado: encontrar en que borde estaba y avanzar al siguiente segmento
                int bi = borderIndex(assamX, assamY);
                if (bi == -1) {
                    // Estaba en interior, ir al borde mas cercano
                    nx = Math.max(0, Math.min(6, nx));
                    ny = Math.max(0, Math.min(6, ny));
                } else {
                    // Avanzar en el carril (siguiente posicion del borde)
                    bi = bi + 1;
                    int[] pos = borderPos(bi);
                    nx = pos[0]; ny = pos[1];
                    assamDir = borderDir(bi);
                }
            }
            assamX = nx; assamY = ny;
        }
        // Actualizar direccion si Assam quedo en el borde
        int bi = borderIndex(assamX, assamY);
        if (bi != -1) assamDir = borderDir(bi);
        assamView.setRotate(assamDir * 90);
    }

    private void handleTileClick(int x, int y) {
        if (currentPhase == Phase.CARPET_1) {
            if (Math.abs(assamX - x) + Math.abs(assamY - y) == 1) {
                firstCarpetX = x; firstCarpetY = y;
                tiles[x][y].setStyle("-fx-background-color: rgba(255,255,255,0.25);");
                currentPhase = Phase.CARPET_2;
                statusLabel.setText("1ra mitad lista. Click en cualquier casilla contigua para la 2da mitad.");
            }
        } else if (currentPhase == Phase.CARPET_2) {
            boolean adyacente = Math.abs(firstCarpetX - x) + Math.abs(firstCarpetY - y) == 1;
            boolean noAssam   = !(x == assamX && y == assamY);
            // La orientación la determina dónde clickea el jugador la 2da mitad
            boolean esHorizontal = (y == firstCarpetY);

            if (adyacente && noAssam) {
                tiles[firstCarpetX][firstCarpetY].setStyle("");

                int player = currentPlayerIdx + 1;
                tileOwner[firstCarpetX][firstCarpetY] = player;
                tileOwner[x][y] = player;

                int colInicio = Math.min(firstCarpetX, x);
                int rowInicio = Math.min(firstCarpetY, y);

                colocarImagenAlfombra(colInicio, rowInicio, esHorizontal, player);

                rugs[currentPlayerIdx]--;
                actualizarUI();
                if (juegoTerminado()) mostrarFinDeJuego();
                else pasarTurno();
            }
        }
    }

    /**
     * Coloca UNA ImageView que abarca 2 celdas del GridPane.
     * colSpan=2 si horizontal, rowSpan=2 si vertical.
     */
    private void colocarImagenAlfombra(int col, int row, boolean horizontal, int player) {
        Image img = carpetImages[player - 1];
        if (img == null) return;

        ImageView iv = new ImageView(img);
        iv.setMouseTransparent(true); // los clicks pasan a las StackPane de abajo

        if (horizontal) {
            iv.setFitWidth(CELL * 2);
            iv.setFitHeight(CELL);
            GridPane.setColumnSpan(iv, 2);
            GridPane.setRowSpan(iv, 1);
        } else {
            iv.setFitWidth(CELL);
            iv.setFitHeight(CELL * 2);
            GridPane.setColumnSpan(iv, 1);
            GridPane.setRowSpan(iv, 2);
        }
        iv.setPreserveRatio(false);
            if (horizontal) iv.setRotate(90);
            if (horizontal) iv.setRotate(90);

        boardGrid.add(iv, col, row);
        // Assam siempre encima
        assamView.toFront();
    }

    private void pasarTurno() {
        firstCarpetX = -1; firstCarpetY = -1; carpetHorizontal = null;
        currentPlayerIdx = (currentPlayerIdx + 1) % numPlayers;
        currentPhase = Phase.MOVE;
        rollDiceBtn.setDisable(false);
        assamView.toFront();
        actualizarUI();
        statusLabel.setText("Rota a Assam y lanza el dado.");
    }

    private boolean juegoTerminado() {
        for (int i = 0; i < numPlayers; i++) if (rugs[i] > 0) return false;
        return true;
    }

    private void mostrarFinDeJuego() {
        int[] enTablero = new int[numPlayers];
        for (int r = 0; r < 7; r++)
            for (int c = 0; c < 7; c++)
                if (tileOwner[c][r] > 0) enTablero[tileOwner[c][r] - 1]++;

        int win = 0;
        for (int i = 1; i < numPlayers; i++)
            if (money[i] > money[win] || (money[i] == money[win] && enTablero[i] > enTablero[win]))
                win = i;

        winnerLabel.setText("GANA: " + playerNames[win] + "!");
        winnerLabel.setStyle("-fx-font-size:36px; -fx-font-weight:bold; -fx-text-fill:" + playerColors[win] + ";");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numPlayers; i++)
            sb.append(playerNames[i]).append(": ").append(money[i]).append(" Dh | Tiles: ").append(enTablero[i]).append("\n");
        finalScores.setText(sb.toString());

        gameScreen.setVisible(false);
        endScreen.setVisible(true);
    }

    @FXML private void restartGame() {
        endScreen.setVisible(false);
        gameScreen.setVisible(false);
        boardGrid.getChildren().clear();
        startScreen.setVisible(true);
    }

    private int contarContiguas(int x, int y, int owner) {
        return dfs(x, y, owner, new boolean[7][7]);
    }

    private int dfs(int x, int y, int owner, boolean[][] vis) {
        if (x < 0 || x > 6 || y < 0 || y > 6 || vis[x][y] || tileOwner[x][y] != owner) return 0;
        vis[x][y] = true;
        return 1 + dfs(x+1,y,owner,vis) + dfs(x-1,y,owner,vis)
                 + dfs(x,y+1,owner,vis) + dfs(x,y-1,owner,vis);
    }

    private void actualizarUI() {
        Label[] rl = {rugsJ1, rugsJ2, rugsJ3, rugsJ4};
        Label[] ml = {moneyJ1, moneyJ2, moneyJ3, moneyJ4};
        VBox[]  pl = {panelJ1, panelJ2, panelJ3, panelJ4};
        for (int i = 0; i < numPlayers; i++) {
            if (rl[i] != null) rl[i].setText(String.valueOf(rugs[i]));
            if (ml[i] != null) ml[i].setText(money[i] + " Dh");
            if (pl[i] != null) pl[i].setStyle(i == currentPlayerIdx
                ? "-fx-border-color:" + playerColors[i] + "; -fx-border-width:3px; -fx-border-radius:10px;"
                : "");
        }
        turnLabel.setText("TURNO: " + playerNames[currentPlayerIdx]);
    }
}
