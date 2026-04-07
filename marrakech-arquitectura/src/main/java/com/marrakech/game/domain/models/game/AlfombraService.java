package hassam.logica;

import hassam.modelo.Jugador;
import hassam.modelo.Posicion;
import hassam.modelo.Tablero;

/**
 * Gestiona la colocación de alfombras y el cálculo de monedas.
 * Equivalente a las funciones 'alfombras()', 'resmondas()' y 'dfs()' en C++.
 */
public class AlfombraService {

    /**
     * Coloca una alfombra (dos celdas adyacentes a Hassam) en el tablero.
     * Devuelve true si la colocación fue exitosa, false si las coordenadas son inválidas.
     *
     * @param tablero  El tablero de juego
     * @param jugador  El jugador que coloca la alfombra
     * @param hasam    Posición actual de Hassam
     * @param f1       Fila de la primera celda (0-indexada)
     * @param c1       Columna de la primera celda (0-indexada)
     * @param f2       Fila de la segunda celda (0-indexada)
     * @param c2       Columna de la segunda celda (0-indexada)
     * @param turno    0 = jugador azul, 1 = jugador rojo
     * @return true si la alfombra fue colocada correctamente
     */
    public boolean colocarAlfombra(Tablero tablero, Jugador jugador,
                                   Posicion hasam,
                                   int f1, int c1, int f2, int c2,
                                   int turno) {
        // Validar que ambas celdas estén dentro del tablero
        if (!tablero.estaEnRango(f1, c1) || !tablero.estaEnRango(f2, c2)) {
            return false;
        }

        // Validar que ambas celdas estén adyacentes a Hassam
        if (!esAdjacenteAHassam(hasam, f1, c1) || !esAdjacenteAHassam(hasam, f2, c2)) {
            return false;
        }

        String marca1 = (turno == 0) ? "A1" : "R1";
        String marca2 = (turno == 0) ? "A2" : "R2";
        String marcaPropia1 = marca1;
        String marcaPropia2 = marca2;

        // Verificar que no haya ya una alfombra propia en esas celdas
        if (tablero.getCelda(f1, c1).equals(marcaPropia1) &&
            tablero.getCelda(f2, c2).equals(marcaPropia2)) {
            return false;
        }

        tablero.setCelda(f1, c1, marca1);
        tablero.setCelda(f2, c2, marca2);
        jugador.usarTapete();
        return true;
    }

    /**
     * Verifica si una celda está adyacente a Hassam (rango de 1 casilla).
     */
    private boolean esAdjacenteAHassam(Posicion hasam, int fila, int col) {
        return fila >= hasam.getPosX() - 1 && fila <= hasam.getPosX() + 1 &&
               col  >= hasam.getPosY() - 1 && col  <= hasam.getPosY() + 1;
    }

    /**
     * Aplica la penalización de monedas cuando Hassam cae sobre la alfombra rival.
     * Cuenta todas las celdas conectadas del color rival usando DFS,
     * resta esa cantidad al jugador actual y la suma al rival.
     *
     * @param tablero        El tablero de juego
     * @param hasam          Posición actual de Hassam
     * @param jugadorActual  El jugador que acaba de mover
     * @param jugadorRival   El otro jugador
     * @param turno          0 = jugador azul, 1 = jugador rojo
     * @return Número de alfombras conectadas (monedas transferidas), 0 si no hay penalización
     */
    public int aplicarPenalizacion(Tablero tablero, Posicion hasam,
                                   Jugador jugadorActual, Jugador jugadorRival,
                                   int turno) {
        int x = hasam.getPosX();
        int y = hasam.getPosY();

        // El jugador actual es penalizado si Hassam cae sobre alfombras PROPIAS
        // (según la lógica original: cae sobre las del color del turno actual)
        String color1 = (turno == 0) ? "A1" : "R1";
        String color2 = (turno == 0) ? "A2" : "R2";

        String celdaActual = tablero.getCelda(x, y);
        if (!celdaActual.equals(color1) && !celdaActual.equals(color2)) {
            return 0; // No hay penalización
        }

        boolean[][] visitado = new boolean[Tablero.FILAS][Tablero.COLUMNAS];
        int conectadas = dfs(tablero, x, y, visitado, color1, color2);

        jugadorActual.restarMonedas(conectadas);
        jugadorRival.agregarMonedas(conectadas);

        return conectadas;
    }

    /**
     * DFS para contar celdas conectadas del mismo color a partir de (x, y).
     */
    private int dfs(Tablero tablero, int x, int y,
                    boolean[][] visitado, String color1, String color2) {
        if (x < 0 || x >= Tablero.FILAS || y < 0 || y >= Tablero.COLUMNAS) return 0;
        if (visitado[x][y]) return 0;

        String celda = tablero.getCelda(x, y);
        if (!celda.equals(color1) && !celda.equals(color2)) return 0;

        visitado[x][y] = true;
        int count = 1;

        int[] dx = {0, 0, -1, 1};
        int[] dy = {-1, 1, 0, 0};

        for (int i = 0; i < 4; i++) {
            count += dfs(tablero, x + dx[i], y + dy[i], visitado, color1, color2);
        }

        return count;
    }
}
