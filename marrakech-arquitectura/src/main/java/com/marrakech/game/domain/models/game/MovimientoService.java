package hassam.logica;

import hassam.modelo.Posicion;
import hassam.modelo.Tablero;

/**
 * Contiene toda la lógica de movimiento de Hassam por el tablero.
 * Equivalente a la función 'direccion()' en C++.
 *
 * Hassam se mueve en espiral cuando llega al borde:
 *   - Si la fila/columna es par, gira en un sentido.
 *   - Si es impar, gira en el otro.
 */
public class MovimientoService {

    /**
     * Mueve a Hassam según la opción de giro y la cantidad de pasos.
     *
     * @param hasam  Posición actual de Hassam (se modifica en lugar)
     * @param op     'd' = girar derecha, 'z' = girar izquierda, 'n' = recto
     * @param pasos  Número de pasos a mover
     */
    public void mover(Posicion hasam, char op, int pasos) {
        int cont = pasos;

        switch (op) {
            case 'd' -> cont = girarDerecha(hasam, cont);
            case 'z' -> cont = girarIzquierda(hasam, cont);
            case 'n' -> cont = moverRecto(hasam, cont);
        }
    }

    // -------------------------------------------------------------------------
    // Giro a la derecha: rota 90° CW y luego avanza
    // -------------------------------------------------------------------------
    private int girarDerecha(Posicion h, int cont) {
        switch (h.getDireccion()) {
            case 's' -> { h.setDireccion('e'); cont = avanzarEste(h, cont);  }
            case 'e' -> { h.setDireccion('n'); cont = avanzarNorte(h, cont); }
            case 'n' -> { h.setDireccion('o'); cont = avanzarOeste(h, cont); }
            case 'o' -> { h.setDireccion('s'); cont = avanzarSur(h, cont);   }
        }
        return cont;
    }

    // -------------------------------------------------------------------------
    // Giro a la izquierda: rota 90° CCW y luego avanza
    // -------------------------------------------------------------------------
    private int girarIzquierda(Posicion h, int cont) {
        switch (h.getDireccion()) {
            case 's' -> { h.setDireccion('o'); cont = avanzarOeste(h, cont); }
            case 'o' -> { h.setDireccion('n'); cont = avanzarNorte(h, cont); }
            case 'n' -> { h.setDireccion('e'); cont = avanzarEste(h, cont);  }
            case 'e' -> { h.setDireccion('s'); cont = avanzarSur(h, cont);   }
        }
        return cont;
    }

    // -------------------------------------------------------------------------
    // Sin giro: avanza en la dirección actual
    // -------------------------------------------------------------------------
    private int moverRecto(Posicion h, int cont) {
        switch (h.getDireccion()) {
            case 's' -> cont = avanzarSur(h, cont);
            case 'e' -> cont = avanzarEste(h, cont);
            case 'n' -> cont = avanzarNorte(h, cont);
            case 'o' -> cont = avanzarOeste(h, cont);
        }
        return cont;
    }

    // -------------------------------------------------------------------------
    // Avance hacia el SUR (+posX). Al salir del borde, hace la espiral.
    // -------------------------------------------------------------------------
    private int avanzarSur(Posicion h, int cont) {
        while (cont > 0) {
            h.setPosX(h.getPosX() + 1);
            cont--;
            if (h.getPosX() > 6) {
                if (h.getPosY() == 6) {
                    // Esquina SE: rebotar hacia el oeste
                    h.setPosX(6); h.setPosY(6);
                    h.setDireccion('o');
                    h.setPosY(h.getPosY() - cont);
                    cont = 0;
                } else if (h.getPosY() % 2 == 0) {
                    h.setPosY(h.getPosY() + 1);
                    h.setDireccion('n');
                    h.setPosX(h.getPosX() - 1 - cont);
                    cont = 0;
                } else {
                    h.setPosY(h.getPosY() - 1);
                    h.setDireccion('n');
                    h.setPosX(h.getPosX() - 1 - cont);
                    cont = 0;
                }
            }
        }
        return cont;
    }

    // -------------------------------------------------------------------------
    // Avance hacia el NORTE (-posX). Al salir del borde, hace la espiral.
    // -------------------------------------------------------------------------
    private int avanzarNorte(Posicion h, int cont) {
        while (cont > 0) {
            h.setPosX(h.getPosX() - 1);
            cont--;
            if (h.getPosX() < 0) {
                if (h.getPosY() == 0) {
                    // Esquina NO: rebotar hacia el este
                    h.setPosX(0); h.setPosY(0);
                    h.setDireccion('e');
                    h.setPosY(h.getPosY() + cont);
                    cont = 0;
                } else if (h.getPosY() % 2 == 0) {
                    h.setPosY(h.getPosY() - 1);
                    h.setDireccion('s');
                    h.setPosX(h.getPosX() + 1 + cont);
                    cont = 0;
                } else {
                    h.setPosY(h.getPosY() + 1);
                    h.setDireccion('s');
                    h.setPosX(h.getPosX() + 1 + cont);
                    cont = 0;
                }
            }
        }
        return cont;
    }

    // -------------------------------------------------------------------------
    // Avance hacia el ESTE (+posY). Al salir del borde, hace la espiral.
    // -------------------------------------------------------------------------
    private int avanzarEste(Posicion h, int cont) {
        while (cont > 0) {
            h.setPosY(h.getPosY() + 1);
            cont--;
            if (h.getPosY() > 6) {
                if (h.getPosX() == 6) {
                    // Esquina SE: rebotar hacia el norte
                    h.setPosX(6); h.setPosY(6);
                    h.setDireccion('n');
                    h.setPosX(h.getPosX() - cont);
                    cont = 0;
                } else if (h.getPosX() % 2 == 0) {
                    h.setPosX(h.getPosX() + 1);
                    h.setDireccion('o');
                    h.setPosY(h.getPosY() - 1 - cont);
                    cont = 0;
                } else {
                    h.setPosX(h.getPosX() - 1);
                    h.setDireccion('o');
                    h.setPosY(h.getPosY() - 1 - cont);
                    cont = 0;
                }
            }
        }
        return cont;
    }

    // -------------------------------------------------------------------------
    // Avance hacia el OESTE (-posY). Al salir del borde, hace la espiral.
    // -------------------------------------------------------------------------
    private int avanzarOeste(Posicion h, int cont) {
        while (cont > 0) {
            h.setPosY(h.getPosY() - 1);
            cont--;
            if (h.getPosY() < 0) {
                if (h.getPosX() == 0) {
                    // Esquina NO: rebotar hacia el sur
                    h.setPosX(0); h.setPosY(0);
                    h.setDireccion('s');
                    h.setPosX(h.getPosX() + cont);
                    cont = 0;
                } else if (h.getPosX() % 2 == 0) {
                    h.setPosX(h.getPosX() - 1);
                    h.setDireccion('e');
                    h.setPosY(h.getPosY() + 1 + cont);
                    cont = 0;
                } else {
                    h.setPosX(h.getPosX() + 1);
                    h.setDireccion('e');
                    h.setPosY(h.getPosY() + 1 + cont);
                    cont = 0;
                }
            }
        }
        return cont;
    }
}
