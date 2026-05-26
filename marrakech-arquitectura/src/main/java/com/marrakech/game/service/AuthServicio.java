package com.marrakech.game.service;

import java.io.File;

import com.marrakech.game.repository.IJugadorRepositorio;
import com.marrakech.game.repository.IPartidaRepositorio;

public class AuthServicio {

    private final IJugadorRepositorio jugadorRepo;
    private final IPartidaRepositorio partidaRepo;

    public AuthServicio(IJugadorRepositorio jugadorRepo, IPartidaRepositorio partidaRepo) {
        this.jugadorRepo = jugadorRepo;
        this.partidaRepo = partidaRepo;
    }

    public String registrarYLogin(String apodo, String correo, String password) {
        if (!validarEmail(correo)) return "EMAIL_INVALIDO";
        if (jugadorRepo.nombreExiste(apodo))  return "APODO_EXISTE";
        if (jugadorRepo.correoExiste(correo)) return "CORREO_EXISTE";
        if (!jugadorRepo.crearJugador(apodo, correo, password)) return "ERROR_BD";
        String resultado = jugadorRepo.loginJugador(apodo, password);
        if ("SESION_ACTIVA".equals(resultado)) return "SESION_ACTIVA";
        return resultado;
    }

    public String login(String apodo, String password) {
        return jugadorRepo.loginJugador(apodo, password);
    }

    public void cerrarSesion(String nombreUsuario) {
        jugadorRepo.cerrarSesion(nombreUsuario);
    }

    private boolean validarEmail(String email) {
        if (email == null || email.isEmpty()) return false;
        int arrobaCount = 0;
        int arrobaPos = -1;
        for (int i = 0; i < email.length(); i++) {
            if (email.charAt(i) == '@') {
                arrobaCount++;
                arrobaPos = i;
            }
        }
        return arrobaCount == 1 && arrobaPos > 0 && arrobaPos < email.length() - 1;
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