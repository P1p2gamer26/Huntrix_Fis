package com.marrakech.game.service;

public class GestionJuegoServicio {

    // ── Reliquias ─────────────────────────────────────────────────────────────

    /** Identificadores de cada reliquia con su descripción y leyenda. */
    public enum Reliquia {
        BRUJULA_MERCADER ("[B]", "Brújula del Mercader",
            "Elige cuántos pasos moverá Assam (1-6)",
            "Los mercaderes más astutos del zoco conocen los caminos secretos del desierto. "
            + "Esta brújula, forjada con la arena de las dunas doradas, te permite controlar "
            + "el destino de Assam y decidir exactamente cuántas casillas avanzará."),
        CALIZ_DORADO     ("[C]", "Cáliz Dorado",
            "Paga solo la mitad al aterrizar en alfombra ajena",
            "Una copa bañada en oro del mismísimo Palacio del Sultán. Quien la posee negocia "
            + "con la autoridad del visir: cuando debas pagar por pisar una alfombra rival, "
            + "tu deuda se reduce a la mitad."),
        ALFOMBRA_SULTAN  ("[A]", "Alfombra del Sultán",
            "Coloca una segunda alfombra extra en tu turno",
            "Tejida con hilos de luna por los genios del zoco, esta alfombra bendita te permite "
            + "desplegar dos alfombras en un mismo turno, expandiendo tu presencia en el mercado "
            + "con la rapidez de un vendaval del desierto.");

        public final String icono;
        public final String nombre;
        public final String descripcion;
        public final String leyenda;
        Reliquia(String icono, String nombre, String descripcion, String leyenda) {
            this.icono = icono; this.nombre = nombre;
            this.descripcion = descripcion; this.leyenda = leyenda;
        }
    }

    /** Máximo de veces que puede aparecer cada reliquia en una partida. */
    private static final int MAX_APARICIONES = 3;

    /** Cuántas veces ha aparecido cada reliquia (índice = ordinal de Reliquia). */
    private final int[] aparicionesReliquia = new int[Reliquia.values().length];

    /** Posición de cada reliquia en el tablero: [reliquia][0]=col, [1]=row, -1 si no está. */
    private final int[][] posicionReliquia = new int[Reliquia.values().length][2];

    /** Inventario de reliquias por jugador: inventarioReliquias[jugador][reliquia] = true si la tiene. */
    private boolean[][] inventarioReliquias;

    private final java.util.Random rng = new java.util.Random();

    /** Jugadores eliminados por quedarse sin monedas (solo aplica en 3-4 jugadores). */
    private boolean[] eliminado;

    private void inicializarReliquias() {
        for (int i = 0; i < Reliquia.values().length; i++) {
            aparicionesReliquia[i] = 0;
            posicionReliquia[i][0] = -1;
            posicionReliquia[i][1] = -1;
        }
        inventarioReliquias = new boolean[numPlayers][Reliquia.values().length];
        eliminado = new boolean[numPlayers];
    }

    /**
     * Intenta hacer aparecer una reliquia aleatoria en el tablero.
     * Se llama al inicio de cada turno si los poderes están activados.
     * Solo aparece si: no está ya en el tablero, no ha llegado a MAX_APARICIONES,
     * y la probabilidad aleatoria lo permite (33% por turno).
     * @return la reliquia que apareció, o null si no apareció ninguna.
     */
    public Reliquia intentarAparecerReliquia(int assamX, int assamY) {
        if (rng.nextInt(3) != 0) return null;

        Reliquia[] todas = Reliquia.values();
        int[] orden = {0, 1, 2};
        for (int i = 2; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = orden[i]; orden[i] = orden[j]; orden[j] = tmp;
        }

        for (int idx : orden) {
            if (posicionReliquia[idx][0] != -1) continue;
            if (aparicionesReliquia[idx] >= MAX_APARICIONES) continue;

            for (int intento = 0; intento < 20; intento++) {
                int c = rng.nextInt(7), r = rng.nextInt(7);

                // No aparecer sobre Assam
                if (c == assamX && r == assamY) continue;

                // No aparecer sobre otra reliquia
                boolean ocupada = false;
                for (int otra = 0; otra < Reliquia.values().length; otra++) {
                    if (posicionReliquia[otra][0] == c && posicionReliquia[otra][1] == r) {
                        ocupada = true;
                        break;
                    }
                }
                if (ocupada) continue;

                posicionReliquia[idx][0] = c;
                posicionReliquia[idx][1] = r;
                aparicionesReliquia[idx]++;
                return todas[idx];
            }
        }
        return null;
    }

    /**
     * Comprueba si Assam está sobre una reliquia y la recoge si el jugador
     * no la tiene ya. Devuelve la reliquia recogida o null.
     */
    public Reliquia intentarRecogerReliquia(int assamX, int assamY) {
        for (int idx = 0; idx < Reliquia.values().length; idx++) {
            if (posicionReliquia[idx][0] == assamX && posicionReliquia[idx][1] == assamY) {
                if (!inventarioReliquias[currentPlayerIdx][idx]) {
                    inventarioReliquias[currentPlayerIdx][idx] = true;
                    posicionReliquia[idx][0] = -1;
                    posicionReliquia[idx][1] = -1;
                    return Reliquia.values()[idx];
                }
                // Jugador ya la tiene, la reliquia se queda en el tablero
            }
        }
        return null;
    }

    /** Devuelve true si el jugador tiene la reliquia indicada. */
    public boolean tieneReliquia(int jugadorIdx, Reliquia r) {
        if (inventarioReliquias == null) return false;
        return inventarioReliquias[jugadorIdx][r.ordinal()];
    }

    /** Consume (elimina del inventario) una reliquia del jugador actual. */
    public void consumirReliquia(Reliquia r) {
        if (inventarioReliquias != null)
            inventarioReliquias[currentPlayerIdx][r.ordinal()] = false;
    }

    /** Posición actual de una reliquia en el tablero ({col,row} o {-1,-1} si no está). */
    public int[] getPosicionReliquia(Reliquia r) { return posicionReliquia[r.ordinal()]; }

    /** Inventario completo de un jugador (array de boolean por ordinal de Reliquia). */
    public boolean[] getInventarioJugador(int jugadorIdx) {
        if (inventarioReliquias == null) return new boolean[Reliquia.values().length];
        return inventarioReliquias[jugadorIdx];
    }

    // ─────────────────────────────────────────────────────────────────────────

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
    private boolean partidaRapida = false;
    private final int[][] tileOwner = new int[7][7];
    private final int[][] carpetOrientation = new int[7][7];
    private int currentPlayerIdx;
    private int currentPhase;
    private int firstCarpetX = -1, firstCarpetY = -1;

    public void iniciarJuego(int n) { iniciarJuego(n, false); }

    public void iniciarJuego(int n, boolean partidaRapida) {
        this.numPlayers = n;
        this.money = new int[n];
        this.rugs = new int[n];
        this.partidaRapida = partidaRapida;
        int monedas = partidaRapida ? 10 : 20;
        int alfombras = partidaRapida ? 8 : 15;
        for (int i = 0; i < n; i++) { money[i] = monedas; rugs[i] = alfombras; }
        for (int r = 0; r < 7; r++)
            for (int c = 0; c < 7; c++) {
                tileOwner[c][r] = 0;
                carpetOrientation[c][r] = 0;
            }
        currentPlayerIdx = 0;
        currentPhase = 0;
        firstCarpetX = -1; firstCarpetY = -1;
        inicializarReliquias();
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
        if (pago > 0 && !esEliminado(currentPlayerIdx)) {
            int dueno = tileOwner[assamX][assamY];
            if (dueno > 0 && !esEliminado(dueno - 1)) {
                int pagoReal = Math.min(pago, money[currentPlayerIdx]);
                money[currentPlayerIdx] = Math.max(0, money[currentPlayerIdx] - pago);
                money[dueno - 1] += pagoReal;
            }
        }
        return pago;
    }

    public void pasarTurno() {
        currentPlayerIdx = siguienteTurnoValido(currentPlayerIdx);
        currentPhase = 0;
        firstCarpetX = -1; firstCarpetY = -1;
    }

    /**
     * Busca el siguiente jugador que no esté eliminado.
     * Si todos están eliminados menos uno, devuelve ese mismo (el juego termina aparte).
     */
    private int siguienteTurnoValido(int desde) {
        for (int i = 1; i <= numPlayers; i++) {
            int candidato = (desde + i) % numPlayers;
            if (!esEliminado(candidato)) return candidato;
        }
        return (desde + 1) % numPlayers; // fallback
    }

    /**
     * Comprueba si el jugador idx quedó sin monedas y lo elimina si aplica.
     * En 2 jugadores no se elimina — se termina la partida directamente.
     * Devuelve true si fue eliminado ahora.
     */
    public boolean verificarEliminacion(int jugadorIdx) {
        if (eliminado == null) eliminado = new boolean[numPlayers];
        if (eliminado[jugadorIdx]) return false;
        if (money[jugadorIdx] <= 0) {
            money[jugadorIdx] = 0;
            if (numPlayers > 2) {
                eliminado[jugadorIdx] = true;
                // NO tocar rugs — las alfombras colocadas siguen en el tablero
                // solo se impide que coloque más poniendo rugs a 0
                rugs[jugadorIdx] = 0;
                return true;
            }
        }
        return false;
    }

    /** Devuelve true si el jugador fue eliminado. */
    public boolean esEliminado(int jugadorIdx) {
        return eliminado != null && eliminado[jugadorIdx];
    }

    /**
     * En 2 jugadores: termina si alguno llega a 0 monedas O si se agotan rugs.
     * En 3-4 jugadores: termina si solo queda 1 jugador activo O si se agotan rugs.
     */
    public boolean juegoTerminado() {
        if (numPlayers == 2) {
            if (money[0] <= 0 || money[1] <= 0) return true;
        } else {
            int activos = 0;
            for (int i = 0; i < numPlayers; i++)
                if (!esEliminado(i)) activos++;
            if (activos <= 1) return true;
        }
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
            firstCarpetX, firstCarpetY, carpetOrientation,
            posicionReliquia, inventarioReliquias != null
                ? inventarioReliquias
                : new boolean[numPlayers][Reliquia.values().length],
            eliminado);
    }

    public void aplicarEstado(String raw) {
        EstadoJuegoServicio.EstadoDB est = EstadoJuegoServicio.parsearEstado(raw);
        if (est == null) return;

        String[] secciones = est.tableroJson.split(";");
        if (secciones.length < 7) return;

        if (eliminado == null) eliminado = new boolean[numPlayers];

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

        // ── Reliquias: posiciones ─────────────────────────────────────────────
        if (secciones.length > 8) {
            String[] rels = secciones[8].split("/");
            for (int i = 0; i < rels.length && i < posicionReliquia.length; i++) {
                String[] coords = rels[i].split(",");
                if (coords.length >= 2) {
                    posicionReliquia[i][0] = Integer.parseInt(coords[0]);
                    posicionReliquia[i][1] = Integer.parseInt(coords[1]);
                }
            }
        }

        // ── Reliquias: inventarios por jugador ────────────────────────────────
        if (secciones.length > 9 && inventarioReliquias != null) {
            String[] jugadores = secciones[9].split("/");
            for (int j = 0; j < jugadores.length && j < numPlayers; j++) {
                String[] bits = jugadores[j].split(",");
                for (int r = 0; r < bits.length && r < Reliquia.values().length; r++)
                    inventarioReliquias[j][r] = "1".equals(bits[r]);
            }
        }

        // ── Eliminados ────────────────────────────────────────────────────────
        if (secciones.length > 10 && eliminado != null) {
            String[] bits = secciones[10].split(",");
            for (int i = 0; i < bits.length && i < eliminado.length; i++)
                eliminado[i] = "1".equals(bits[i]);
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
    public boolean isPartidaRapida() { return partidaRapida; }
    public void setCurrentPlayerIdx(int idx) { this.currentPlayerIdx = idx; }
    public void setCurrentPhase(int phase) { this.currentPhase = phase; }
    public void setNumPlayers(int n) { this.numPlayers = n; }
}