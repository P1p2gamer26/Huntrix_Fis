package com.marrakech.game.service;

import com.marrakech.game.repository.IJugadorRepositorio;
import com.marrakech.game.repository.IPartidaRepositorio;

import java.io.File;

/**
 * Casos de uso relacionados con autenticación y perfil de jugadores.
 * Recibe sus dependencias por constructor (inyección de dependencias).
 */
public class AuthServicio {

    private final IJugadorRepositorio jugadorRepo;
    private final IPartidaRepositorio partidaRepo;

    public AuthServicio(IJugadorRepositorio jugadorRepo, IPartidaRepositorio partidaRepo) {
        this.jugadorRepo = jugadorRepo;
        this.partidaRepo = partidaRepo;
    }

    /**
     * Registra un nuevo jugador y abre sesión inmediatamente.
     * @return "APODO_EXISTE", "CORREO_EXISTE" o el nombre del jugador si todo fue bien.
     */
    public String registrarYLogin(String apodo, String correo, String password) {
        if (jugadorRepo.nombreExiste(apodo))  return "APODO_EXISTE";
        if (jugadorRepo.correoExiste(correo)) return "CORREO_EXISTE";
        jugadorRepo.crearJugador(apodo, correo, password);
        jugadorRepo.loginJugador(apodo, password);
        return apodo;
    }

    /**
     * Autentica un jugador existente.
     * @return null=credenciales malas, "SESION_ACTIVA"=sesión duplicada, nombre=éxito.
     */
    public String login(String apodo, String password) {
        String resultado = jugadorRepo.loginJugador(apodo, password);
        if (resultado == null)               return null;
        if ("SESION_ACTIVA".equals(resultado)) return "SESION_ACTIVA";
        return resultado;
    }

    public void cerrarSesion(String nombreUsuario) {
        jugadorRepo.cerrarSesion(nombreUsuario);
    }

    public String getCorreo(String nombreUsuario) {
        return jugadorRepo.getCorreo(nombreUsuario);
    }

    public String getFechaRegistro(String nombreUsuario) {
        return jugadorRepo.getFechaRegistro(nombreUsuario);
    }

    public int getVictorias(String nombreUsuario) {
        return partidaRepo.obtenerRanking().stream()
            .filter(r -> r.usuario.equals(nombreUsuario))
            .mapToInt(r -> r.victorias)
            .findFirst()
            .orElse(0);
    }

    public int obtenerIdJugador(String nombre) {
        return jugadorRepo.obtenerIdJugador(nombre);
    }

    public boolean guardarFoto(String nombreUsuario, File archivoImagen) {
        return jugadorRepo.guardarFoto(nombreUsuario, archivoImagen);
    }

    public byte[] getFoto(String nombreUsuario) {
        return jugadorRepo.getFoto(nombreUsuario);
    }
}
