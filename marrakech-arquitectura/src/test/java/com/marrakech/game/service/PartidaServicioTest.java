package com.marrakech.game.service;

import com.marrakech.game.repository.IPartidaRepositorio;
import com.marrakech.game.repository.PartidaRepositorio.Partida;
import com.marrakech.game.repository.PartidaRepositorio.RankingEntry;

import org.junit.jupiter.api.*;
import org.mockito.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Pruebas unitarias para PartidaServicio con 100 % de cobertura de ramas. */
class PartidaServicioTest {

    @Mock private IPartidaRepositorio partidaRepo;

    private PartidaServicio svc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        svc = new PartidaServicio(partidaRepo);
    }

    // ── CASO COMPLEJO 1: unirse cuando la sala está llena ─────────────────────

    @Test
    void unirse_salaLlena_retornaFalse() {
        // La sala ya tiene 2 jugadores y maxJugadores es 2
        Partida salaLlena = new Partida("MRK-1", "Sala", 2, false, false, "NORMAL",
                                        List.of("Host", "Invitado"), "ESPERANDO");
        when(partidaRepo.obtenerPartida("MRK-1")).thenReturn(salaLlena);
        when(partidaRepo.unirsePartida("MRK-1", "Tercero")).thenReturn(false);

        boolean resultado = svc.unirsePartida("MRK-1", "Tercero");

        assertFalse(resultado);
    }

    // ── CASO COMPLEJO 2: unirse con código inexistente ────────────────────────

    @Test
    void unirse_codigoInexistente_retornaFalse() {
        when(partidaRepo.unirsePartida("XXXXX", "Jugador")).thenReturn(false);

        boolean resultado = svc.unirsePartida("XXXXX", "Jugador");

        assertFalse(resultado);
    }

    // ── CASO COMPLEJO 3: crear partida y verificar que el ID no sea nulo ──────

    @Test
    void crear_partidaNueva_retornaIdValido() {
        when(partidaRepo.crearPartida("Host", 3, true, "DIFICIL"))
            .thenReturn("MRK-ABCD");

        String id = svc.crearPartida("Host", 3, true, "DIFICIL");

        assertEquals("MRK-ABCD", id);
        verify(partidaRepo).crearPartida("Host", 3, true, "DIFICIL");
    }

    // ── CASO COMPLEJO 4: obtenerRanking con múltiples jugadores ordenados ──────

    @Test
    void obtenerRanking_listaOrdenada_correcta() {
        List<RankingEntry> mockRanking = List.of(
            new RankingEntry("Lider", 10),
            new RankingEntry("Segundo", 7),
            new RankingEntry("Tercero", 3)
        );
        when(partidaRepo.obtenerRanking()).thenReturn(mockRanking);

        List<RankingEntry> ranking = svc.obtenerRanking();

        assertEquals(3, ranking.size());
        assertEquals("Lider", ranking.get(0).usuario);
        assertEquals(10, ranking.get(0).victorias);
    }

    // ── CASO COMPLEJO 5: iniciarPartida delega al repositorio ─────────────────

    @Test
    void iniciar_partida_delegaAlRepositorio() {
        svc.iniciarPartida("MRK-TEST");
        verify(partidaRepo).iniciarPartida("MRK-TEST");
    }

    // ── Cobertura adicional ───────────────────────────────────────────────────

    @Test
    void obtenerPartida_delegaAlRepositorio() {
        Partida mock = new Partida("MRK-X", "X", 2, false, false, "NORMAL",
                                   List.of("Host"), "ESPERANDO");
        when(partidaRepo.obtenerPartida("MRK-X")).thenReturn(mock);

        Partida result = svc.obtenerPartida("MRK-X");

        assertNotNull(result);
        assertEquals("MRK-X", result.id);
    }

    @Test
    void listarPartidas_delegaAlRepositorio() {
        when(partidaRepo.listarPartidas()).thenReturn(List.of());
        assertTrue(svc.listarPartidas().isEmpty());
    }

    @Test
    void registrarVictoria_delegaAlRepositorio() {
        svc.registrarVictoria("Campeón");
        verify(partidaRepo).registrarVictoria("Campeón");
    }

    @Test
    void unirse_exitoso_retornaTrue() {
        when(partidaRepo.unirsePartida("MRK-OK", "Jugador")).thenReturn(true);
        assertTrue(svc.unirsePartida("MRK-OK", "Jugador"));
    }

    @Test
    void limpiarSalasViejas_cubreForYIf() {
        IPartidaRepositorio repo = org.mockito.Mockito.mock(IPartidaRepositorio.class);

        var p1 = new com.marrakech.game.repository.PartidaRepositorio.Partida(
                "MRK-0001", "Sala 1", 4, true, false, "Normal",
                java.util.List.of("Jugador1"), "INICIADA"
        );

        var p2 = new com.marrakech.game.repository.PartidaRepositorio.Partida(
                "MRK-0002", "Sala 2", 4, false, false, "Normal",
                java.util.List.of("Jugador2"), "ESPERANDO"
        );

        org.mockito.Mockito.when(repo.listarPartidas()).thenReturn(java.util.List.of(p1, p2));

        PartidaServicio s = new PartidaServicio(repo);
        s.limpiarSalasViejas();

        org.mockito.Mockito.verify(repo).listarPartidas();
    }
}

