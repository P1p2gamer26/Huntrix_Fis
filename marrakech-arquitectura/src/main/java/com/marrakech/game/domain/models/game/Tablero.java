package hassam.modelo;

/**
 * Representa el tablero 7x7 del juego.
 * Cada celda puede estar vacía o contener una mitad de alfombra:
 *   "A1" / "A2" = alfombra azul (jugador 0)
 *   "R1" / "R2" = alfombra roja (jugador 1)
 *   "  "        = celda vacía
 */
public class Tablero {

    public static final int FILAS    = 7;
    public static final int COLUMNAS = 7;

    private final String[][] celdas;

    public Tablero() {
        celdas = new String[FILAS][COLUMNAS];
        limpiar();
    }

    /** Inicializa todas las celdas como vacías. */
    public void limpiar() {
        for (int i = 0; i < FILAS; i++) {
            for (int j = 0; j < COLUMNAS; j++) {
                celdas[i][j] = "  ";
            }
        }
    }

    public String getCelda(int fila, int col) {
        return celdas[fila][col];
    }

    public void setCelda(int fila, int col, String valor) {
        celdas[fila][col] = valor;
    }

    public boolean estaEnRango(int fila, int col) {
        return fila >= 0 && fila < FILAS && col >= 0 && col < COLUMNAS;
    }

    /**
     * Cuenta las celdas que contienen alguno de los tokens indicados.
     * Útil para contar alfombras de cada color en el tablero.
     */
    public int contarCeldas(String token1, String token2) {
        int count = 0;
        for (int i = 0; i < FILAS; i++) {
            for (int j = 0; j < COLUMNAS; j++) {
                if (celdas[i][j].equals(token1) || celdas[i][j].equals(token2)) {
                    count++;
                }
            }
        }
        return count;
    }
}
