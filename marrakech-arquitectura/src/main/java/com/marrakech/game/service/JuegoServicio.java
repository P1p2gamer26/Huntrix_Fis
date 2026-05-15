package com.marrakech.game.service;

import com.marrakech.game.service.CarpetValidador;

public final class JuegoServicio {

    private JuegoServicio() {}

    public static boolean juegoTerminado(int[] rugs) {
        for (int r : rugs) if (r > 0) return false;
        return true;
    }

    public static int calcularGanador(int numPlayers, int[] money, int[][] tileOwner) {
        int[] enTablero = new int[numPlayers];
        for (int r = 0; r < 7; r++)
            for (int c = 0; c < 7; c++)
                if (tileOwner[c][r] > 0) enTablero[tileOwner[c][r] - 1]++;

        int win = 0;
        for (int i = 1; i < numPlayers; i++)
            if (money[i] > money[win] || (money[i] == money[win] && enTablero[i] > enTablero[win]))
                win = i;
        return win;
    }

    public static int calcularPago(int assamX, int assamY, int currentPlayerIdx, int[][] tileOwner) {
        int owner = tileOwner[assamX][assamY];
        if (owner == 0 || owner == currentPlayerIdx + 1) return 0;
        return CarpetValidador.contarContiguas(assamX, assamY, owner, tileOwner);
    }

    public static boolean esMiTurno(boolean modoMultijugador, int currentPlayerIdx, int miIndice) {
        return !modoMultijugador || currentPlayerIdx == miIndice;
    }

    public static int siguienteTurno(int currentPlayerIdx, int numPlayers) {
        return (currentPlayerIdx + 1) % numPlayers;
    }

    public static boolean esAdyacenteA(int x, int y, int assamX, int assamY) {
        return Math.abs(assamX - x) + Math.abs(assamY - y) == 1;
    }

    public static boolean esAlfombraAdyacente(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2) == 1;
    }

    public static boolean noEsAssam(int x, int y, int assamX, int assamY) {
        return !(x == assamX && y == assamY);
    }

    public static int aplicarPago(int assamX, int assamY, int currentPlayerIdx,
                                   int[][] tileOwner, int[] money) {
        int pago = calcularPago(assamX, assamY, currentPlayerIdx, tileOwner);
        if (pago > 0) {
            int dueno = tileOwner[assamX][assamY];
            money[currentPlayerIdx] = Math.max(0, money[currentPlayerIdx] - pago);
            money[dueno - 1] += pago;
        }
        return pago;
    }

    public static int contarAlfombrasEnTablero(int playerIdx, int[][] tileOwner) {
        int count = 0;
        for (int r = 0; r < 7; r++)
            for (int c = 0; c < 7; c++)
                if (tileOwner[c][r] == playerIdx + 1) count++;
        return count;
    }

    public static int fasePostDado(int rugsRestantes) {
        return rugsRestantes > 0 ? 1 : 0;
    }
}
