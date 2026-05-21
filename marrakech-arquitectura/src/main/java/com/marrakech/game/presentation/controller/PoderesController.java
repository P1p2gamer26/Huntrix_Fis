package com.marrakech.game.presentation.controller;

import com.marrakech.game.service.GestionJuegoServicio;
import com.marrakech.game.service.GestionJuegoServicio.Reliquia;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;

import java.util.List;
import java.util.Random;

/**
 * Controlador auxiliar de superpoderes (reliquias).
 * Centraliza los diálogos y la lógica de cada reliquia,
 * manteniendo GameController limpio.
 */
public class PoderesController {

    private final GestionJuegoServicio juegoSvc;
    private final boolean poderesActivados;
    private final Random rng = new Random();

    public PoderesController(GestionJuegoServicio juegoSvc, boolean poderesActivados) {
        this.juegoSvc        = juegoSvc;
        this.poderesActivados = poderesActivados;
    }

    // ── Brújula del Mercader ──────────────────────────────────────────────────

    /**
     * Resuelve cuántos pasos moverá Assam este turno.
     * Si el jugador tiene la Brújula y acepta usarla, elige entre 1-6.
     * Si no tiene o rechaza, lanza el dado normal.
     * Devuelve -1 si el diálogo fue cancelado sin elegir nada.
     */
    public int resolverDado() {
        if (poderesActivados
                && juegoSvc.tieneReliquia(juegoSvc.getCurrentPlayerIdx(), Reliquia.BRUJULA_MERCADER)) {

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

                int elegido = dialogo.showAndWait().orElse(-1);
                if (elegido > 0) {
                    juegoSvc.consumirReliquia(Reliquia.BRUJULA_MERCADER);
                    return elegido;
                }
                // Si cerró el diálogo sin elegir, dado normal (no se consume la reliquia)
            }
        }

        // Dado normal
        return rng.nextInt(6) + 1;
    }

    // ── Recolección de reliquias en el recorrido ──────────────────────────────

    /**
     * Revisa cada casilla del path de Assam y recoge las reliquias que encuentre.
     * Devuelve la última reliquia recogida, o null si no se recogió ninguna.
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

    // ── Cáliz Dorado ─────────────────────────────────────────────────────────

    /**
     * Calcula y aplica el pago por caer en alfombra ajena.
     * Si el jugador tiene el Cáliz y acepta usarlo, paga la mitad (sin decimales).
     * Devuelve el mensaje de estado para mostrar en pantalla.
     */
    public String resolverPago(int ax, int ay, int pasos) {
        // Calcular el pago sin aplicarlo todavía
        int pago = com.marrakech.game.service.JuegoServicio.calcularPago(
            ax, ay, juegoSvc.getCurrentPlayerIdx(), juegoSvc.getTileOwner());

        if (pago <= 0) {
            return "Dado: " + pasos + " — Haz click en una casilla adyacente.";
        }

        int dueno = juegoSvc.getTileOwner()[ax][ay];
        String nombreDueno = nombreJugador(dueno - 1);

        if (poderesActivados
                && juegoSvc.tieneReliquia(juegoSvc.getCurrentPlayerIdx(), Reliquia.CALIZ_DORADO)) {

            int pagoMitad = pago / 2;

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("🏆 Cáliz Dorado");
            confirm.setHeaderText("Debes pagar " + pago + " Dh a " + nombreDueno);
            confirm.setContentText("¿Usas el Cáliz Dorado para pagar solo " + pagoMitad + " Dh?");
            confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

            boolean usar = confirm.showAndWait()
                .map(r -> r == ButtonType.YES).orElse(false);

            int pagoFinal = usar ? pagoMitad : pago;

            // Aplicar el pago final directamente sobre el array
            int[] dinero = juegoSvc.getMoney();
            dinero[juegoSvc.getCurrentPlayerIdx()] =
                Math.max(0, dinero[juegoSvc.getCurrentPlayerIdx()] - pagoFinal);
            if (dueno > 0) dinero[dueno - 1] += pagoFinal;

            if (usar) {
                juegoSvc.consumirReliquia(Reliquia.CALIZ_DORADO);
                return "🏆 Cáliz Dorado — pagaste solo " + pagoFinal
                    + " Dh en vez de " + pago + ". Coloca tu alfombra.";
            } else {
                return "Dado: " + pasos + " — Pagaste " + pago
                    + " Dh a " + nombreDueno + ". Coloca tu alfombra.";
            }
        }

        // Sin cáliz: aplicar pago normal
        juegoSvc.aplicarPago(ax, ay);
        return "Dado: " + pasos + " — Pagas " + pago
            + " Dh a " + nombreDueno + ". Coloca tu alfombra.";
    }

    // ── Alfombra del Sultán ───────────────────────────────────────────────────

    /**
     * Pregunta si el jugador quiere usar la Alfombra del Sultán para colocar
     * una segunda alfombra. Devuelve true si la usa (y la consume),
     * false si no la tiene, no está en fase de alfombra, o rechaza.
     */
    public boolean resolverSultan() {
        if (!poderesActivados
                || !juegoSvc.tieneReliquia(juegoSvc.getCurrentPlayerIdx(), Reliquia.ALFOMBRA_SULTAN)
                || juegoSvc.getCurrentPhase() != 1) {
            return false;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("✨ Alfombra del Sultán");
        confirm.setHeaderText("Tienes la Alfombra del Sultán");
        confirm.setContentText("¿Quieres usarla para colocar una segunda alfombra este turno?");
        confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        boolean usar = confirm.showAndWait()
            .map(r -> r == ButtonType.YES).orElse(false);

        if (usar) {
            juegoSvc.consumirReliquia(Reliquia.ALFOMBRA_SULTAN);
        }
        return usar;
    }

    // ── Utilidad ──────────────────────────────────────────────────────────────

    private String nombreJugador(int idx) {
        String[] names = {"J1 (ROJO)", "J2 (AZUL)", "J3 (VERDE)", "J4 (AMARILLO)"};
        return idx >= 0 && idx < names.length ? names[idx] : "J" + (idx + 1);
    }
}
