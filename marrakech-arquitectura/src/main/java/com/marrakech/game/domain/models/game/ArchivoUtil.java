package hassam.util;

import hassam.modelo.Jugador;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Maneja la escritura del resultado final en un archivo de texto.
 * Equivalente al bloque de ofstream al final de main() en C++.
 */
public class ArchivoUtil {

    private static final String ARCHIVO = "resultado.txt";

    /**
     * Guarda el resultado de la partida en "resultado.txt".
     *
     * @param jugador0  Jugador azul
     * @param jugador1  Jugador rojo
     * @param ganador   Nombre del ganador (o "Empate")
     */
    public void guardarResultado(Jugador jugador0, Jugador jugador1, String ganador) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ARCHIVO))) {

            String sep = "          ";

            pw.printf("nombre: %s%snombre: %s%n",
                    jugador0.getNombre(), sep, jugador1.getNombre());
            pw.printf("color: %s%scolor: %s%n",
                    jugador0.getColorTapete(), sep, jugador1.getColorTapete());
            pw.printf("alfombras: %d%salfombras: %d%n",
                    jugador0.getTapetes(), sep, jugador1.getTapetes());
            pw.printf("monedas: %d%smonedas: %d%n",
                    jugador0.getMonedas(), sep, jugador1.getMonedas());
            pw.println("el ganador es: " + ganador);

            System.out.println("Resultado guardado en " + ARCHIVO);

        } catch (IOException e) {
            System.err.println("Error al guardar el resultado: " + e.getMessage());
        }
    }
}
