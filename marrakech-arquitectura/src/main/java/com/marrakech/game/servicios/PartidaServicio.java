package com.marrakech.game.servicios;

import com.marrakech.game.repositorio.IPartidaRepositorio;
import com.marrakech.game.repositorio.PartidaRepositorio.Partida;
import com.marrakech.game.repositorio.PartidaRepositorio.RankingEntry;

import java.util.List;

/**
 * Casos de uso relacionados con gestión de partidas online.
 * Centraliza la lógica que antes estaba dispersa en AuthController y PartidaRepositorio.
 */
public class PartidaServicio {

    private final IPartidaRepositorio partidaRepo;

    public PartidaServicio(IPartidaRepositorio partidaRepo) {
        this.partidaRepo = partidaRepo;
    }

    public String crearPartida(String creador, int maxJugadores,
                               boolean poderes, boolean rapida, String dificultad) {
        return partidaRepo.crearPartida(creador, maxJugadores, poderes, rapida, dificultad);
    }

    /**
     * Une al jugador a la sala indicada.
     * @return false si el código no existe o la sala está llena.
     */
    public boolean unirsePartida(String codigo, String nombreJugador) {
        return partidaRepo.unirsePartida(codigo, nombreJugador);
    }

    public void iniciarPartida(String id) {
        partidaRepo.iniciarPartida(id);
    }

    public Partida obtenerPartida(String id) {
        return partidaRepo.obtenerPartida(id);
    }

    public List<Partida> listarPartidas() {
        return partidaRepo.listarPartidas();
    }

    public List<RankingEntry> obtenerRanking() {
        return partidaRepo.obtenerRanking();
    }

    public void registrarVictoria(String usuario) {
        partidaRepo.registrarVictoria(usuario);
    }

    public void limpiarSalasViejas() {
        for (Partida p : partidaRepo.listarPartidas()) {
            if ("INICIADA".equalsIgnoreCase(p.estado)) {
                // cleanup delegado al repositorio cuando se implemente delete
            }
        }
    }
}
