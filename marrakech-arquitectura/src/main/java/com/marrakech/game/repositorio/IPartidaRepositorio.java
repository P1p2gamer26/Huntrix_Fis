package com.marrakech.game.repositorio;

import com.marrakech.game.repositorio.PartidaRepositorio.Partida;
import com.marrakech.game.repositorio.PartidaRepositorio.RankingEntry;

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
}
