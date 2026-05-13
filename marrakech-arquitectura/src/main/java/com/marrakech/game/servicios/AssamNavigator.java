package com.marrakech.game.servicios;

/**
 * Calcula el movimiento de Assam sobre el tablero 7x7, incluyendo los rebotes
 * en los bordes siguiendo el circuito perimetral del juego Marrakech.
 *
 * El circuito comienza en (0,0) y avanza en sentido horario:
 *   borde norte (y=0) → borde este (x=6) → borde sur (y=6) → borde oeste (x=0)
 */
public class AssamNavigator {

    private AssamNavigator() {}

    /**
     * Calcula el camino completo de Assam paso a paso.
     * @return array [pasos+1][3] con {x, y, dir} para cada posición.
     */
    public static int[][] computePath(int pasos, int startX, int startY, int startDir) {
        int[][] path = new int[pasos + 1][3];
        path[0][0] = startX; path[0][1] = startY; path[0][2] = startDir;

        for (int p = 0; p < pasos; p++) {
            int x = path[p][0], y = path[p][1], dir = path[p][2];
            int nx = x, ny = y;
            switch (dir) {
                case 0: ny--; break;
                case 1: nx++; break;
                case 2: ny++; break;
                case 3: nx--; break;
            }
            if (nx < 0 || nx > 6 || ny < 0 || ny > 6) {
                int bi = borderIndex(x, y);
                if (bi != -1) {
                    bi++;
                    int[] pos = borderPos(bi);
                    nx = pos[0]; ny = pos[1]; dir = borderDir(bi);
                } else {
                    nx = Math.max(0, Math.min(6, nx));
                    ny = Math.max(0, Math.min(6, ny));
                }
            }
            path[p + 1][0] = nx; path[p + 1][1] = ny; path[p + 1][2] = dir;
        }

        // Ajuste de dirección en caso de que el destino final sea una esquina de borde
        int biFinal = borderIndex(path[pasos][0], path[pasos][1]);
        if (biFinal != -1) path[pasos][2] = borderDir(biFinal);
        return path;
    }

    /** Índice en el circuito perimetral (0-23) de la celda, o -1 si está en el interior. */
    public static int borderIndex(int x, int y) {
        if (y == 0 && x < 6) return x;
        if (x == 6 && y < 6) return 6 + y;
        if (y == 6 && x > 0) return 12 + (6 - x);
        if (x == 0 && y > 0) return 18 + (6 - y);
        return -1;
    }

    /** Convierte un índice del circuito perimetral a coordenadas (x, y). */
    public static int[] borderPos(int idx) {
        idx = ((idx % 24) + 24) % 24;
        if (idx < 6)  return new int[]{idx, 0};
        if (idx < 12) return new int[]{6, idx - 6};
        if (idx < 18) return new int[]{6 - (idx - 12), 6};
        return new int[]{0, 6 - (idx - 18)};
    }

    /** Dirección de avance estándar para cada índice del circuito perimetral. */
    public static int borderDir(int idx) {
        idx = ((idx % 24) + 24) % 24;
        if (idx < 6)  return 1; // este
        if (idx < 12) return 2; // sur
        if (idx < 18) return 3; // oeste
        return 0;               // norte
    }
}
