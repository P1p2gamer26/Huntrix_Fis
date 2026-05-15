package com.marrakech.game.service;

/**
 * Reglas de validación y reparación de alfombras sobre el tablero 7x7.
 * Extrae la lógica pura de posición para mantener GameController enfocado en UI.
 */
public class CarpetValidador {

    private CarpetValidador() {}

    /** Verifica que ambas celdas de la alfombra estén dentro del tablero. */
    public static boolean esCarpetValida(int x1, int y1, int x2, int y2) {
        return x1 >= 0 && x1 <= 6 && y1 >= 0 && y1 <= 6
            && x2 >= 0 && x2 <= 6 && y2 >= 0 && y2 <= 6;
    }

    /**
     * Verifica que la celda (x1,y1) tenga al menos una celda contigua válida
     * para colocar la segunda mitad de la alfombra (sin ocupar Assam).
     */
    public static boolean tiene2daOpcionValida(int x1, int y1, int assamX, int assamY) {
        for (int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
            int x2 = x1 + d[0], y2 = y1 + d[1];
            if (x2 == assamX && y2 == assamY) continue;
            if (esCarpetValida(x1, y1, x2, y2)) return true;
        }
        return false;
    }

    /**
     * Cuando se cubre una celda que era la primera mitad de una alfombra previa,
     * convierte la segunda mitad huérfana en una celda "sola" (orientación 3).
     * Cuando se cubre una segunda mitad (-1), limpia también su primera mitad.
     */
    public static void repararAlfombraAfectada(int col, int row, int[][] carpetOrientation) {
        int ori = carpetOrientation[col][row];
        if (ori == 0 || ori == 3) return;

        if (ori == 1 && row + 1 < 7 && carpetOrientation[col][row + 1] == -1)
            carpetOrientation[col][row + 1] = 3;
        else if (ori == 2 && col + 1 < 7 && carpetOrientation[col + 1][row] == -1)
            carpetOrientation[col + 1][row] = 3;
        else if (ori == -1) {
            if (row > 0 && carpetOrientation[col][row - 1] == 1)
                carpetOrientation[col][row - 1] = 3;
            else if (col > 0 && carpetOrientation[col - 1][row] == 2)
                carpetOrientation[col - 1][row] = 3;
        }
        carpetOrientation[col][row] = 0;
    }

    /**
     * Cuenta cuántas celdas contiguas pertenecen al mismo propietario (BFS/DFS).
     * Se usa para calcular el pago cuando Assam cae en una alfombra rival.
     */
    public static int contarContiguas(int x, int y, int owner, int[][] tileOwner) {
        return dfs(x, y, owner, new boolean[7][7], tileOwner);
    }

    private static int dfs(int x, int y, int owner, boolean[][] vis, int[][] tileOwner) {
        if (x < 0 || x > 6 || y < 0 || y > 6) return 0;
        if (vis[x][y] || tileOwner[x][y] != owner) return 0;
        vis[x][y] = true;
        return 1
            + dfs(x + 1, y, owner, vis, tileOwner)
            + dfs(x - 1, y, owner, vis, tileOwner)
            + dfs(x, y + 1, owner, vis, tileOwner)
            + dfs(x, y - 1, owner, vis, tileOwner);
    }
}
