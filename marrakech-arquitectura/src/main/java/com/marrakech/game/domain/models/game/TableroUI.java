package hassam.ui;

import hassam.modelo.Posicion;
import hassam.modelo.Tablero;

/**
 * Renderiza el tablero en la consola.
 * Equivalente a la función 'tablon()' en C++.
 */
public class TableroUI {

    /**
     * Imprime el tablero con la posición actual de Hassam.
     *
     * @param tablero  El tablero de juego
     * @param hasam    Posición de Hassam para mostrar "Ha" en su celda
     */
    public void mostrar(Tablero tablero, Posicion hasam) {
        int filas    = Tablero.FILAS;
        int columnas = Tablero.COLUMNAS;

        // Encabezado de columnas
        System.out.print("  ");
        for (int j = 0; j < columnas; j++) {
            System.out.printf("   %d   ", j + 1);
        }
        System.out.println();

        // Filas del tablero
        for (int i = 0; i < filas; i++) {
            System.out.printf(" %d", i + 1);
            for (int j = 0; j < columnas; j++) {
                if (i == hasam.getPosX() && j == hasam.getPosY()) {
                    System.out.print(" | Ha |");
                } else {
                    System.out.printf(" | %s |", tablero.getCelda(i, j));
                }
            }
            System.out.println();
        }
    }
}
