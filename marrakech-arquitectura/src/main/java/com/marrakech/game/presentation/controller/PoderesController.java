package com.marrakech.game.presentation.controller;

import com.marrakech.game.service.GestionJuegoServicio;
import com.marrakech.game.service.GestionJuegoServicio.Reliquia;
import com.marrakech.game.service.JuegoServicio;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;

import java.util.List;
import java.util.Random;

/**
 * Controlador auxiliar de superpoderes (reliquias).
 *
 * Jerarquía de ejecución al terminar el movimiento de Assam:
 *   1. resolverDado()          — antes de mover (Brújula)
 *   2. recogerReliquias()      — durante el recorrido
 *   3. resolverPostMovimiento() — al aterrizar:
 *        a) ¿está sobre alfombra ajena? → pago normal
 *        b) ¿tiene Cáliz?              → preguntar si lo usa → aplicar pago
 *        c) ¿tiene Sultán?             → preguntar si lo usa
 *        Devuelve un ResultadoPost con el mensaje y si se activa el sultán.
 */
public class PoderesController {

    private final GestionJuegoServicio juegoSvc;
    private final boolean poderesActivados;
    private final Random rng = new Random();

    public PoderesController(GestionJuegoServicio juegoSvc, boolean poderesActivados) {
        this.juegoSvc         = juegoSvc;
        this.poderesActivados = poderesActivados;
    }

    // ── 1. Brújula del Mercader ───────────────────────────────────────────────

    /**
     * Resuelve cuántos pasos mover a Assam.
     * Primero comprueba que el jugador tenga la Brújula en el inventario.
     * Si la tiene, pregunta; si acepta, muestra el selector 1-6.
     * Si cancela en cualquier punto, lanza el dado normal sin consumir la reliquia.
     */
    public int resolverDado() {
        if (poderesActivados
                && juegoSvc.getInventarioJugador(juegoSvc.getCurrentPlayerIdx())
                            [Reliquia.BRUJULA_MERCADER.ordinal()]) {

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("🧭 Brújula del Mercader");
            confirm.setHeaderText("Tienes la Brújula del Mercader");
            confirm.setContentText("¿Quieres usarla para elegir cuántas casillas mover?");
            confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

            boolean usar = confirm.showAndWait()
                .map(r -> r == ButtonType.YES).orElse(false);

            if (usar) {
                ChoiceDialog<Integer> dialogo = new ChoiceDialog<>(1, List.of(1, 2, 3, 4, 5, 6));
                dialogo.setTitle("🧭 Brújula del Mercader");
                dialogo.setHeaderText("Elige cuántas casillas mover a Assam");
                dialogo.setContentText("Pasos:");

                int elegido = dialogo.showAndWait().orElse(0);
                if (elegido > 0) {
                    juegoSvc.consumirReliquia(Reliquia.BRUJULA_MERCADER);
                    return elegido;
                }
            }
        }
        return rng.nextInt(6) + 1;
    }

    // ── 2. Recolección en el recorrido ────────────────────────────────────────

    /**
     * Recorre cada casilla del path y recoge reliquias.
     * Solo actúa si los poderes están activos.
     * Devuelve la última reliquia recogida, o null.
     */
    public Reliquia recogerReliquiasEnRecorrido(int[][] path, int pasos) {
        if (!poderesActivados) return null;

        Reliquia ultimaRecogida = null;
        for (int paso = 1; paso <= pasos; paso++) {
            Reliquia r = juegoSvc.intentarRecogerReliquia(path[paso][0], path[paso][1]);
            if (r != null) ultimaRecogida = r;
        }
        return ultimaRecogida;
    }

    // ── 3. Post-movimiento: Cáliz → pago → Sultán ────────────────────────────

    /** Resultado del post-movimiento: mensaje a mostrar + si el Sultán fue activado. */
    public static class ResultadoPost {
        public final String mensaje;
        public final boolean sultanActivado;
        ResultadoPost(String mensaje, boolean sultanActivado) {
            this.mensaje         = mensaje;
            this.sultanActivado  = sultanActivado;
        }
    }

    /**
     * Ejecuta la jerarquía completa al terminar el movimiento de Assam:
     *   1. Calcula si hay pago (alfombra ajena bajo Assam).
     *   2. Si hay pago y el jugador tiene el Cáliz, pregunta si lo usa.
     *   3. Aplica el pago final (con o sin descuento).
     *   4. Si el jugador tiene la Alfombra del Sultán, pregunta si la usa.
     *
     * @param ax    columna final de Assam
     * @param ay    fila final de Assam
     * @param pasos número de pasos lanzados (para el mensaje)
     */
    public ResultadoPost resolverPostMovimiento(int ax, int ay, int pasos) {
        int jugador = juegoSvc.getCurrentPlayerIdx();
        boolean[] inv = juegoSvc.getInventarioJugador(jugador);

        // ── Paso 1: calcular pago sin aplicarlo aún ───────────────────────────
        int pago = JuegoServicio.calcularPago(ax, ay, jugador, juegoSvc.getTileOwner());
        int dueno = juegoSvc.getTileOwner()[ax][ay];
        String nombreDueno = nombreJugador(dueno - 1);
        String mensajePago;

        if (pago > 0) {
            // ── Paso 2: ¿tiene Cáliz? ─────────────────────────────────────────
            int pagoFinal = pago;
            boolean caliz = poderesActivados && inv[Reliquia.CALIZ_DORADO.ordinal()];

            if (caliz) {
                int pagoMitad = pago / 2;

                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("🏆 Cáliz Dorado");
                confirm.setHeaderText("Debes pagar " + pago + " Dh a " + nombreDueno);
                confirm.setContentText("¿Usas el Cáliz Dorado para pagar solo " + pagoMitad + " Dh?");
                confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

                boolean usar = confirm.showAndWait()
                    .map(r -> r == ButtonType.YES).orElse(false);

                if (usar) {
                    pagoFinal = pagoMitad;
                    juegoSvc.consumirReliquia(Reliquia.CALIZ_DORADO);
                    mensajePago = "🏆 Cáliz Dorado — pagaste solo " + pagoFinal
                        + " Dh en vez de " + pago + ". Coloca tu alfombra.";
                } else {
                    mensajePago = "Dado: " + pasos + " — Pagaste " + pago
                        + " Dh a " + nombreDueno + ". Coloca tu alfombra.";
                }
            } else {
                mensajePago = "Dado: " + pasos + " — Pagas " + pago
                    + " Dh a " + nombreDueno + ". Coloca tu alfombra.";
            }

            // ── Paso 3: aplicar el pago final ────────────────────────────────
            int[] dinero = juegoSvc.getMoney();
            dinero[jugador] = Math.max(0, dinero[jugador] - pagoFinal);
            if (dueno > 0) dinero[dueno - 1] += pagoFinal;

        } else {
            mensajePago = "Dado: " + pasos + " — Haz click en una casilla adyacente.";
        }

        // ── Paso 4: ¿tiene Alfombra del Sultán? ──────────────────────────────
        boolean sultan = false;
        if (poderesActivados && inv[Reliquia.ALFOMBRA_SULTAN.ordinal()]) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("✨ Alfombra del Sultán");
            confirm.setHeaderText("Tienes la Alfombra del Sultán");
            confirm.setContentText("¿Quieres usarla para colocar una segunda alfombra este turno?");
            confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

            sultan = confirm.showAndWait()
                .map(r -> r == ButtonType.YES).orElse(false);

            if (sultan) juegoSvc.consumirReliquia(Reliquia.ALFOMBRA_SULTAN);
        }

        return new ResultadoPost(mensajePago, sultan);
    }

    // ── Utilidad ──────────────────────────────────────────────────────────────

    private String nombreJugador(int idx) {
        String[] names = {"J1 (ROJO)", "J2 (AZUL)", "J3 (VERDE)", "J4 (AMARILLO)"};
        return idx >= 0 && idx < names.length ? names[idx] : "J" + (idx + 1);
    }
}
