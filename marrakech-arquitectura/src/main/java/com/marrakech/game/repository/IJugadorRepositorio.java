package com.marrakech.game.repository;

import java.io.File;

/** Contrato de persistencia para jugadores. */
public interface IJugadorRepositorio {
    /** Retorna true si el jugador fue creado, false si hubo un error de BD. */
    boolean crearJugador(String nombre, String correo, String password);
    boolean correoExiste(String correo);
    boolean nombreExiste(String nombre);
    /** Retorna null (credenciales malas), "SESION_ACTIVA" o el nombre del jugador. */
    String loginJugador(String apodo, String password);
    void cerrarSesion(String nombreUsuario);
    int obtenerIdJugador(String nombre);
    String getCorreo(String nombreUsuario);
    String getFechaRegistro(String nombreUsuario);
    boolean guardarFoto(String nombreUsuario, File archivoImagen);
    byte[] getFoto(String nombreUsuario);
}
