package com.marrakech.game.service;

public class AssamNavigator {

    private AssamNavigator() {}

    public static int[][] computePath(int pasos, int startX, int startY, int startDir) {
        int[][] path = new int[pasos + 1][3];
        path[0][0] = startX;
        path[0][1] = startY;
        path[0][2] = startDir;

        for (int p = 0; p < pasos; p++) {
            int x   = path[p][0];
            int y   = path[p][1];
            int dir = path[p][2];

            int nx = x, ny = y, ndir = dir;

            switch (dir) {
                case 0: ny--; break; // norte
                case 1: nx++; break; // este
                case 2: ny++; break; // sur
                case 3: nx--; break; // oeste
            }

            if (nx < 0 || nx > 6 || ny < 0 || ny > 6) {
                int[] rebote = calcularRebote(x, y, dir);
                nx   = rebote[0];
                ny   = rebote[1];
                ndir = rebote[2];
            }

            path[p + 1][0] = nx;
            path[p + 1][1] = ny;
            path[p + 1][2] = ndir;
        }
        return path;
    }

    private static int[] calcularRebote(int x, int y, int dir) {

        // ── Esquina superior izquierda (0,0) — ESPECIAL ───────────────────
        if (x == 0 && y == 0) {
            if (dir == 0) return new int[]{0, 0, 1}; // salía norte → mira este
            if (dir == 3) return new int[]{0, 0, 2}; // salía oeste → mira sur
        }

        // ── Esquina inferior derecha (6,6) — ESPECIAL ─────────────────────
        if (x == 6 && y == 6) {
            if (dir == 2) return new int[]{6, 6, 3}; // salía sur  → mira oeste
            if (dir == 1) return new int[]{6, 6, 0}; // salía este → mira norte
        }

        // ── Borde norte (y=0): sale por arriba ────────────────────────────
        // pares: (1,0)↔(2,0), (3,0)↔(4,0), (5,0)↔(6,0)
        if (y == 0 && dir == 0) {
            if (x == 1) return new int[]{2, 0, 2};
            if (x == 2) return new int[]{1, 0, 2};
            if (x == 3) return new int[]{4, 0, 2};
            if (x == 4) return new int[]{3, 0, 2};
            if (x == 5) return new int[]{6, 0, 2};
            if (x == 6) return new int[]{5, 0, 2};
        }

        // ── Borde este (x=6): sale por la derecha ─────────────────────────
        // pares: (6,0)↔(6,1), (6,2)↔(6,3), (6,4)↔(6,5)
        if (x == 6 && dir == 1) {
            if (y == 0) return new int[]{6, 1, 3};
            if (y == 1) return new int[]{6, 0, 3};
            if (y == 2) return new int[]{6, 3, 3};
            if (y == 3) return new int[]{6, 2, 3};
            if (y == 4) return new int[]{6, 5, 3};
            if (y == 5) return new int[]{6, 4, 3};
        }

        // ── Borde sur (y=6): sale por abajo ───────────────────────────────
        // pares: (5,6)↔(4,6), (3,6)↔(2,6), (1,6)↔(0,6)
        if (y == 6 && dir == 2) {
            if (x == 5) return new int[]{4, 6, 0};
            if (x == 4) return new int[]{5, 6, 0};
            if (x == 3) return new int[]{2, 6, 0};
            if (x == 2) return new int[]{3, 6, 0};
            if (x == 1) return new int[]{0, 6, 0};
            if (x == 0) return new int[]{1, 6, 0};
        }

        // ── Borde oeste (x=0): sale por la izquierda ──────────────────────
        // pares: (0,6)↔(0,5), (0,4)↔(0,3), (0,2)↔(0,1)
        if (x == 0 && dir == 3) {
            if (y == 6) return new int[]{0, 5, 1};
            if (y == 5) return new int[]{0, 6, 1};
            if (y == 4) return new int[]{0, 3, 1};
            if (y == 3) return new int[]{0, 4, 1};
            if (y == 2) return new int[]{0, 1, 1};
            if (y == 1) return new int[]{0, 2, 1};
        }

        // Fallback
        return new int[]{
            Math.max(0, Math.min(6, x)),
            Math.max(0, Math.min(6, y)),
            dir
        };
    }

    // Métodos de compatibilidad con tests existentes
    public static int borderIndex(int x, int y) {
        if (y == 0 && x < 6) return x;
        if (x == 6 && y < 6) return 6 + y;
        if (y == 6 && x > 0) return 12 + (6 - x);
        if (x == 0 && y > 0) return 18 + (6 - y);
        return -1;
    }

    public static int[] borderPos(int idx) {
        idx = ((idx % 24) + 24) % 24;
        if (idx < 6)  return new int[]{idx, 0};
        if (idx < 12) return new int[]{6, idx - 6};
        if (idx < 18) return new int[]{6 - (idx - 12), 6};
        return new int[]{0, 6 - (idx - 18)};
    }

    public static int borderDir(int idx) {
        idx = ((idx % 24) + 24) % 24;
        if (idx < 6)  return 1;
        if (idx < 12) return 2;
        if (idx < 18) return 3;
        return 0;
    }
}