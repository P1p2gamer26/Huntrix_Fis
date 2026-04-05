package hassam.modelo;

/**
 * Representa a un jugador del juego Hassam.
 * Equivalente al struct 'jugadores' en C++.
 */
public class Jugador {

    private String nombre;
    private int monedas;
    private String colorTapete;
    private int tapetes;

    public Jugador(String nombre, String colorTapete) {
        this.nombre       = nombre;
        this.colorTapete  = colorTapete;
        this.monedas      = 20;
        this.tapetes      = 10;
    }

    // Getters y setters
    public String getNombre()               { return nombre; }
    public int    getMonedas()              { return monedas; }
    public String getColorTapete()          { return colorTapete; }
    public int    getTapetes()              { return tapetes; }

    public void setMonedas(int monedas)     { this.monedas  = monedas; }
    public void setTapetes(int tapetes)     { this.tapetes  = tapetes; }

    public void agregarMonedas(int cantidad)  { this.monedas += cantidad; }
    public void restarMonedas(int cantidad)   { this.monedas -= cantidad; }
    public void usarTapete()                  { this.tapetes--; }

    @Override
    public String toString() {
        return String.format("Jugador: %s | Color: %s | Monedas: %d | Tapetes: %d",
                nombre, colorTapete, monedas, tapetes);
    }
}
