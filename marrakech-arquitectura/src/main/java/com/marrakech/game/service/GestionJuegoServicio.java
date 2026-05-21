package com.marrakech.game.service;

public class GestionJuegoServicio {

    public enum ResultadoTipo { INVALIDO, SIN_SEGUNDA_OPCION, ESPERA_SEGUNDA, ALFOMBRA_COLOCADA, JUEGO_TERMINADO, ALFOMBRA_COMPLETA }

    public static class ResultadoClick {
        public final ResultadoTipo tipo;
        public final int x1, y1;
        public ResultadoClick(ResultadoTipo tipo, int x1, int y1) {
            this.tipo = tipo; this.x1 = x1; this.y1 = y1;
        }
        public static ResultadoClick invalido() { return new ResultadoClick(ResultadoTipo.INVALIDO, -1, -1); }
    }

    private int numPlayers;
    private int[] money;
    private int[] rugs;
    private final int[][] tileOwner = new int[7][7];
    private final int[][] carpetOrientation = new int[7][7];
    private int currentPlayerIdx;
    private int currentPhase;
    private int firstCarpetX = -1, firstCarpetY = -1;

    public void iniciarJuego(int n) {
        this.numPlayers = n;
        this.money = new int[n];
        this.rugs = new int[n];
        for (int i = 0; i < n; i++) { money[i] = 30; rugs[i] = 15; }
        for (int r = 0; r < 7; r++)
            for (int c = 0; c < 7; c++) {
                tileOwner[c][r] = 0;
                carpetOrientation[c][r] = 0;
            }
        currentPlayerIdx = 0;
        currentPhase = 0;
        firstCarpetX = -1; firstCarpetY = -1;
    }

    public ResultadoClick procesarClick(int x, int y, int assamX, int assamY) {
        if (currentPhase == 1) {
            boolean enRango = CarpetValidador.estaEnRango3x3(x, y, assamX, assamY);
            boolean noAssam = JuegoServicio.noEsAssam(x, y, assamX, assamY);
            if (enRango && noAssam && CarpetValidador.tiene2daOpcionValida(x, y, assamX, assamY)) {
                firstCarpetX = x; firstCarpetY = y;
                currentPhase = 2;
                return new ResultadoClick(ResultadoTipo.ESPERA_SEGUNDA, x, y);
            }
            if (enRango && noAssam) return new ResultadoClick(ResultadoTipo.SIN_SEGUNDA_OPCION, -1, -1);
            return ResultadoClick.invalido();
        }

        if (currentPhase == 2 && firstCarpetX >= 0) {
            boolean adj     = JuegoServicio.esAlfombraAdyacente(firstCarpetX, firstCarpetY, x, y);
            boolean noAssam = JuegoServicio.noEsAssam(x, y, assamX, assamY);
            boolean dentro  = CarpetValidador.esCarpetValida(firstCarpetX, firstCarpetY, x, y);
            boolean noTapa  = CarpetValidador.noTapaAlfombraCompleta(
                                firstCarpetX, firstCarpetY, x, y,
                                currentPlayerIdx, tileOwner, carpetOrientation);

            if (adj && noAssam && dentro && noTapa) {
                int player = currentPlayerIdx + 1;
                CarpetValidador.repararAlfombraAfectada(firstCarpetX, firstCarpetY, carpetOrientation);
                CarpetValidador.repararAlfombraAfectada(x, y, carpetOrientation);
                tileOwner[firstCarpetX][firstCarpetY] = player;
                tileOwner[x][y] = player;

                int topCol = Math.min(firstCarpetX, x);
                int topRow = Math.min(firstCarpetY, y);
                int botCol = Math.max(firstCarpetX, x);
                int botRow = Math.max(firstCarpetY, y);
                boolean horiz = (y == firstCarpetY);
                carpetOrientation[firstCarpetX][firstCarpetY] = 0;
                carpetOrientation[x][y] = 0;
                carpetOrientation[topCol][topRow] = horiz ? 2 : 1;
                carpetOrientation[botCol][botRow] = -1;

                rugs[currentPlayerIdx]--;
                firstCarpetX = -1; firstCarpetY = -1;

                boolean terminado = juegoTerminado();
                return new ResultadoClick(
                    terminado ? ResultadoTipo.JUEGO_TERMINADO : ResultadoTipo.ALFOMBRA_COLOCADA,
                    -1, -1);
            }

            if (adj && noAssam && dentro && !noTapa)
                return new ResultadoClick(ResultadoTipo.ALFOMBRA_COMPLETA, -1, -1);

            return ResultadoClick.invalido();
        }

        return ResultadoClick.invalido();
    }

    public int aplicarPago(int assamX, int assamY) {
        int pago = JuegoServicio.calcularPago(assamX, assamY, currentPlayerIdx, tileOwner);
        if (pago > 0) {
            int dueno = tileOwner[assamX][assamY];
            money[currentPlayerIdx] = Math.max(0, money[currentPlayerIdx] - pago);
            money[dueno - 1] += pago;
        }
        return pago;
    }

    public void pasarTurno() {
        currentPlayerIdx = JuegoServicio.siguienteTurno(currentPlayerIdx, numPlayers);
        currentPhase = 0;
        firstCarpetX = -1; firstCarpetY = -1;
    }

    public boolean juegoTerminado() {
        return JuegoServicio.juegoTerminado(rugs);
    }

    public int calcularGanador() {
        return JuegoServicio.calcularGanador(numPlayers, money, tileOwner);
    }

    public int contarAlfombrasEnTablero(int playerIdx) {
        return JuegoServicio.contarAlfombrasEnTablero(playerIdx, tileOwner);
    }

    public int fasePostDado() {
        return JuegoServicio.fasePostDado(rugs[currentPlayerIdx]);
    }

    public boolean esMiTurno(boolean modoMultijugador, int miIndice) {
        return JuegoServicio.esMiTurno(modoMultijugador, currentPlayerIdx, miIndice);
    }

    public String serializarEstado(int assamX, int assamY, int assamDir) {
        return EstadoJuegoServicio.serializarEstado(
            numPlayers, money, rugs, tileOwner, currentPlayerIdx, currentPhase,
            firstCarpetX, firstCarpetY, carpetOrientation);
    }

    public void aplicarEstado(String raw) {
        EstadoJuegoServicio.EstadoDB est = EstadoJuegoServicio.parsearEstado(raw);
        if (est == null) return;

        String[] secciones = est.tableroJson.split(";");
        if (secciones.length < 7) return;

        String[] ms = secciones[0].split(",");
        String[] rs = secciones[1].split(",");
        for (int i = 0; i < numPlayers && i < ms.length; i++) {
            money[i] = Integer.parseInt(ms[i]);
            rugs[i]  = Integer.parseInt(rs[i]);
        }

        int[][] owner = new int[7][7];
        String[] filas = secciones[2].split("/");
        for (int row = 0; row < 7 && row < filas.length; row++) {
            String[] celdas = filas[row].split(",");
            for (int col = 0; col < 7 && col < celdas.length; col++)
                owner[col][row] = Integer.parseInt(celdas[col]);
        }
        for (int r = 0; r < 7; r++)
            for (int c = 0; c < 7; c++)
                tileOwner[c][r] = owner[c][r];

        currentPlayerIdx = Integer.parseInt(secciones[3]);
        currentPhase     = Integer.parseInt(secciones[4]);
        firstCarpetX = Integer.parseInt(secciones[5]);
        firstCarpetY = Integer.parseInt(secciones[6]);

        if (secciones.length > 7) {
            int[][] orient = new int[7][7];
            String[] ofilas = secciones[7].split("/");
            for (int row = 0; row < 7 && row < ofilas.length; row++) {
                String[] oceldas = ofilas[row].split(",");
                for (int col = 0; col < 7 && col < oceldas.length; col++)
                    orient[col][row] = Integer.parseInt(oceldas[col]);
            }
            for (int r = 0; r < 7; r++)
                for (int c = 0; c < 7; c++)
                    carpetOrientation[c][r] = orient[c][r];
        }
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public int getNumPlayers() { return numPlayers; }
    public int[] getMoney() { return money; }
    public int[] getRugs() { return rugs; }
    public int[][] getTileOwner() { return tileOwner; }
    public int[][] getCarpetOrientation() { return carpetOrientation; }
    public int getCurrentPlayerIdx() { return currentPlayerIdx; }
    public int getCurrentPhase() { return currentPhase; }
    public int getFirstCarpetX() { return firstCarpetX; }
    public int getFirstCarpetY() { return firstCarpetY; }

    public void setCurrentPlayerIdx(int idx) { this.currentPlayerIdx = idx; }
    public void setCurrentPhase(int phase) { this.currentPhase = phase; }
    public void setNumPlayers(int n) { this.numPlayers = n; }
}