package hassam.logica;

import hassam.modelo.Jugador;
import hassam.modelo.Tablero;

/**
 * Determina el ganador al final de la partida.
 * La puntuación combina: alfombras en tablero + tapetes restantes + monedas.
 */
public class GanadorService {

    /**
     * Calcula el puntaje total de un jugador.
     *
     * @param tablero  El tablero final
     * @param jugador  El jugador a evaluar
     * @param token1   Primer token del color del jugador (ej. "A1")
     * @param token2   Segundo token del color del jugador (ej. "A2")
     * @return Puntaje total
     */
    public int calcularPuntaje(Tablero tablero, Jugador jugador,
                               String token1, String token2) {
        int alfombrasEnTablero = tablero.contarCeldas(token1, token2);
        return alfombrasEnTablero + jugador.getTapetes() + jugador.getMonedas();
    }

    /**
     * Determina el nombre del ganador comparando los puntajes de ambos jugadores.
     *
     * @param tablero   El tablero final
     * @param jugador0  Jugador azul
     * @param jugador1  Jugador rojo
     * @return Nombre del ganador, o "Empate" si hay igualdad
     */
    public String determinarGanador(Tablero tablero,
                                    Jugador jugador0, Jugador jugador1) {
        int puntaje0 = calcularPuntaje(tablero, jugador0, "A1", "A2");
        int puntaje1 = calcularPuntaje(tablero, jugador1, "R1", "R2");

        if (puntaje0 > puntaje1) return jugador0.getNombre();
        if (puntaje1 > puntaje0) return jugador1.getNombre();
        return "Empate";
    }

    /**
     * Verifica si el juego debe terminar por condición de monedas o tapetes.
     */
    public boolean juegoTerminado(Jugador jugador0, Jugador jugador1) {
        // Algún jugador quedó sin monedas
        if (jugador0.getMonedas() <= 0 || jugador1.getMonedas() <= 0) return true;
        // Ambos jugadores se quedaron sin tapetes
        if (jugador0.getTapetes() == 0 && jugador1.getTapetes() == 0) return true;
        return false;
    }
}
