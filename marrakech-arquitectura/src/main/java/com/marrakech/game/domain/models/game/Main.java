package hassam;

import hassam.logica.AlfombraService;
import hassam.logica.GanadorService;
import hassam.logica.MovimientoService;
import hassam.modelo.Jugador;
import hassam.modelo.Posicion;
import hassam.modelo.Tablero;
import hassam.ui.TableroUI;
import hassam.ui.TurnoUI;
import hassam.util.ArchivoUtil;
import hassam.util.Dado;

import java.util.Scanner;

/**
 * Punto de entrada del juego Hassam.
 * Orquesta el flujo principal usando los servicios y la UI de cada paquete.
 *
 *  Paquetes:
 *    hassam.modelo   → clases de datos (Jugador, Posicion, Tablero)
 *    hassam.logica   → reglas del juego (MovimientoService, AlfombraService, GanadorService)
 *    hassam.ui       → interacción con el usuario (TableroUI, TurnoUI)
 *    hassam.util     → utilidades (Dado, ArchivoUtil)
 */
public class Main {

    private static final int MAX_TURNOS = 20;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // ── Servicios y utilidades ─────────────────────────────────────────
        MovimientoService movimiento  = new MovimientoService();
        AlfombraService   alfombras   = new AlfombraService();
        GanadorService    ganadorSvc  = new GanadorService();
        TableroUI         tableroUI   = new TableroUI();
        TurnoUI           turnoUI     = new TurnoUI(scanner);
        Dado              dado        = new Dado();
        ArchivoUtil       archivo     = new ArchivoUtil();

        // ── Crear jugadores ────────────────────────────────────────────────
        Jugador[] jugadores = new Jugador[2];
        String[] colores = {"azul", "rojo"};

        for (int i = 0; i < 2; i++) {
            System.out.printf("Ingrese nombre del jugador %d (%s): ", i + 1, colores[i]);
            String nombre = scanner.nextLine().trim();
            jugadores[i] = new Jugador(nombre, colores[i]);
        }

        // ── Inicializar tablero y Hassam ───────────────────────────────────
        Tablero  tablero = new Tablero();
        Posicion hasam   = new Posicion(
                Tablero.FILAS    / 2,
                Tablero.COLUMNAS / 2,
                's'
        );

        System.out.println("\n¡Comienza el juego!\n");
        tableroUI.mostrar(tablero, hasam);

        // ── Bucle principal ────────────────────────────────────────────────
        int  turno   = 0;
        int  turnos  = 1;
        boolean fin  = false;

        while (!fin && turnos <= MAX_TURNOS) {
            Jugador actual = jugadores[turno];
            Jugador rival  = jugadores[(turno + 1) % 2];

            // 1. Lanzar dado y pedir giro
            int numeroDado = dado.lanzar();
            char opGiro    = turnoUI.pedirGiro(actual, hasam, numeroDado);

            // 2. Mover a Hassam
            movimiento.mover(hasam, opGiro, numeroDado);
            System.out.println();
            tableroUI.mostrar(tablero, hasam);

            // 3. Colocar alfombra (reintenta hasta que las coords sean válidas)
            boolean colocado = false;
            while (!colocado) {
                int[] coords = turnoUI.pedirCoordenadas(hasam);
                if (coords == null) {
                    System.out.println("Entrada inválida. Intenta de nuevo.");
                    continue;
                }
                colocado = alfombras.colocarAlfombra(
                        tablero, actual, hasam,
                        coords[0], coords[1], coords[2], coords[3],
                        turno
                );
                if (!colocado) {
                    System.out.println("Coordenadas fuera de rango o alfombra ya existente. Intenta de nuevo.");
                }
            }
            tableroUI.mostrar(tablero, hasam);

            // 4. Aplicar penalización de monedas
            int penalizacion = alfombras.aplicarPenalizacion(tablero, hasam, actual, rival, turno);
            turnoUI.mostrarPenalizacion(penalizacion);

            // 5. Verificar condición de fin
            if (ganadorSvc.juegoTerminado(jugadores[0], jugadores[1])) {
                fin = true;
            }

            // 6. Cambiar turno
            turno = (turno + 1) % 2;
            turnos++;
        }

        // ── Resultado final ────────────────────────────────────────────────
        String ganador = ganadorSvc.determinarGanador(tablero, jugadores[0], jugadores[1]);
        System.out.println("\n════════ FIN DEL JUEGO ════════");
        System.out.println("Ganador: " + ganador);
        System.out.println(jugadores[0]);
        System.out.println(jugadores[1]);

        archivo.guardarResultado(jugadores[0], jugadores[1], ganador);

        scanner.close();
    }
}
