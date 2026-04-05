package hassam.ui;

import hassam.modelo.Jugador;
import hassam.modelo.Posicion;
import hassam.modelo.Tablero;

import java.util.Scanner;

/**
 * Maneja toda la entrada del usuario durante un turno.
 * Equivalente a las funciones 'turno()' y la parte interactiva de 'alfombras()' en C++.
 */
public class TurnoUI {

    private final Scanner scanner;

    public TurnoUI(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Muestra la información del turno y pide la opción de giro.
     *
     * @param jugador  El jugador activo
     * @param hasam    Posición actual de Hassam
     * @param dado     Resultado del dado (ya calculado fuera)
     * @return El carácter de dirección: 'd', 'z' o 'n'
     */
    public char pedirGiro(Jugador jugador, Posicion hasam, int dado) {
        System.out.println("─────────────────────────────────");
        System.out.println("Turno de: " + jugador.getNombre());
        System.out.println("Color: "    + jugador.getColorTapete());
        System.out.println("Tapetes: "  + jugador.getTapetes());
        System.out.println("Monedas: "  + jugador.getMonedas());
        System.out.println("Hassam mira al: " + hasam.getDireccion());
        System.out.println("Número del dado: " + dado);

        char op;
        do {
            System.out.print("¿Desea girar? (d = derecha, z = izquierda, n = recto): ");
            String entrada = scanner.nextLine().trim().toLowerCase();
            op = entrada.isEmpty() ? ' ' : entrada.charAt(0);
        } while (op != 'd' && op != 'z' && op != 'n');

        return op;
    }

    /**
     * Pide las coordenadas para colocar una alfombra y las devuelve como int[]{f1,c1,f2,c2}.
     * Las coordenadas se piden en base 1 (como el usuario las ve) y se devuelven en base 0.
     *
     * @param hasam  Posición actual de Hassam (para mostrar referencia)
     * @return arreglo {fila1, col1, fila2, col2} en base 0, o null si el input es inválido
     */
    public int[] pedirCoordenadas(Posicion hasam) {
        System.out.println();
        System.out.printf("Hassam está en fila %d, columna %d.%n",
                hasam.getPosX() + 1, hasam.getPosY() + 1);
        System.out.println("Coloca una alfombra en dos celdas adyacentes a Hassam.");

        try {
            System.out.print("Primera celda  (fila columna): ");
            String[] linea1 = scanner.nextLine().trim().split("\\s+");
            System.out.print("Segunda celda  (fila columna): ");
            String[] linea2 = scanner.nextLine().trim().split("\\s+");

            if (linea1.length < 2 || linea2.length < 2) return null;

            int f1 = Integer.parseInt(linea1[0]) - 1;
            int c1 = Integer.parseInt(linea1[1]) - 1;
            int f2 = Integer.parseInt(linea2[0]) - 1;
            int c2 = Integer.parseInt(linea2[1]) - 1;

            return new int[]{f1, c1, f2, c2};
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Muestra el resultado de la penalización de monedas. */
    public void mostrarPenalizacion(int cantidad) {
        if (cantidad > 0) {
            System.out.println("Hassam cayó sobre tu alfombra. Monedas transferidas: " + cantidad);
        } else {
            System.out.println("No se restan monedas.");
        }
    }
}
