package hassam.modelo;

/**
 * Representa la posición y dirección de Hassam en el tablero.
 * Equivalente al struct 'Posicion' en C++.
 *
 * Direcciones:
 *   's' = sur  (down)
 *   'n' = norte (up)
 *   'e' = este  (right)
 *   'o' = oeste (left)
 */
public class Posicion {

    private int  posX;
    private int  posY;
    private char direccion;

    public Posicion(int posX, int posY, char direccion) {
        this.posX      = posX;
        this.posY      = posY;
        this.direccion = direccion;
    }

    // Getters y setters
    public int  getPosX()               { return posX; }
    public int  getPosY()               { return posY; }
    public char getDireccion()          { return direccion; }

    public void setPosX(int posX)               { this.posX      = posX; }
    public void setPosY(int posY)               { this.posY      = posY; }
    public void setDireccion(char direccion)     { this.direccion = direccion; }

    @Override
    public String toString() {
        return String.format("Posicion(%d, %d) mirando al '%c'", posX, posY, direccion);
    }
}
