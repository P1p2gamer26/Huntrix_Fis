package com.marrakech.game.repository;

import com.marrakech.game.repository.PartidaRepositorio.Partida;
import com.marrakech.game.repository.PartidaRepositorio.RankingEntry;

import java.util.List;

/** Contrato de persistencia para partidas y ranking. */
public interface IPartidaRepositorio {
    String crearPartida(String nombreCreador, int maxJugadores, boolean poderes,
                        boolean rapida, String dificultad);
    boolean unirsePartida(String id, String nombreJugador);
    void iniciarPartida(String id);
    Partida obtenerPartida(String id);
    List<Partida> listarPartidas();
    List<RankingEntry> obtenerRanking();
    void registrarVictoria(String usuario);
    void salirPartida(String id, String usuario);
    void abandonarPartida(String id);
}
