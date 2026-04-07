package hassam.util;

import java.util.Random;

/**
 * Simula el lanzamiento de un dado de 6 caras.
 * Equivalente al bloque de random_device en C++.
 */
public class Dado {

    private final Random random;

    public Dado() {
        this.random = new Random();
    }

    /** Devuelve un valor entre 1 y 6 (inclusive). */
    public int lanzar() {
        return random.nextInt(6) + 1;
    }
}
