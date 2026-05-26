package com.marrakech.game.service;

public class CarpetValidador {

    private CarpetValidador() {}

    /** Verifica que ambas celdas de la alfombra estén dentro del tablero. */
    public static boolean esCarpetValida(int x1, int y1, int x2, int y2) {
        return x1 >= 0 && x1 <= 6 && y1 >= 0 && y1 <= 6
            && x2 >= 0 && x2 <= 6 && y2 >= 0 && y2 <= 6;
    }

    /** Verifica que (x,y) esté en las 8 celdas del 3x3 alrededor de Assam (no en Assam mismo). */
    public static boolean estaEnRango3x3(int x, int y, int assamX, int assamY) {
        if (x == assamX && y == assamY) return false;
        return Math.abs(x - assamX) <= 1 && Math.abs(y - assamY) <= 1;
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
     * Verifica que las dos celdas de la alfombra no cubran las dos mitades
     * de una misma alfombra ajena.
     * Retorna true si la colocación es válida.
     */
    public static boolean noTapaAlfombraCompleta(int x1, int y1, int x2, int y2,
                                                  int currentPlayer, int[][] tileOwner,
                                                  int[][] carpetOrientation) {
        int[] cabeza1 = getCabeza(x1, y1, carpetOrientation);
        int[] cabeza2 = getCabeza(x2, y2, carpetOrientation);

        if (cabeza1 != null && cabeza2 != null
                && cabeza1[0] == cabeza2[0] && cabeza1[1] == cabeza2[1]
                && tileOwner[cabeza1[0]][cabeza1[1]] != currentPlayer + 1) {
            return false;
        }
        return true;
    }

    /**
     * Retorna la celda cabeza de la alfombra a la que pertenece (x,y).
     * Retorna null si la celda está vacía o es independiente.
     */
    private static int[] getCabeza(int x, int y, int[][] carpetOrientation) {
        int ori = carpetOrientation[x][y];
        if (ori == 1 || ori == 2) return new int[]{x, y};
        if (ori == -1) {
            if (y > 0 && carpetOrientation[x][y - 1] == 1) return new int[]{x, y - 1};
            if (x > 0 && carpetOrientation[x - 1][y] == 2) return new int[]{x - 1, y};
        }
        return null;
    }

    /**
     * Cuando se cubre una celda que era la primera mitad de una alfombra previa,
     * convierte la segunda mitad huérfana en una celda "sola" (orientación 3).
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
     * Cuenta cuántas celdas contiguas pertenecen al mismo propietario (DFS).
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