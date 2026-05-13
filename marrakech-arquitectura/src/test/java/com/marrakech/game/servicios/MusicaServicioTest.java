package com.marrakech.game.servicios;

import com.marrakech.game.presentation.MusicaManager;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class MusicaServicioTest {

    private MusicaServicio svc;

    @BeforeEach
    void setUp() {
        resetManager();
        svc = new MusicaServicio();
    }

    private static void resetManager() {
        try {
            var field = MusicaManager.class.getDeclaredField("instancia");
            field.setAccessible(true);
            field.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo resetear MusicaManager", e);
        }
    }

    @Test
    void reproducir_trackMenu_noLanzaExcepcion() {
        svc.reproducir(MusicaServicio.Track.MENU);
    }

    @Test
    void reproducir_trackLobby_noLanzaExcepcion() {
        svc.reproducir(MusicaServicio.Track.LOBBY);
    }

    @Test
    void reproducir_trackJuego_noLanzaExcepcion() {
        svc.reproducir(MusicaServicio.Track.JUEGO);
    }

    @Test
    void detener_noLanzaExcepcion() {
        svc.reproducir(MusicaServicio.Track.MENU);
        svc.detener();
    }

    @Test
    void detenerSinReproducir_noLanzaExcepcion() {
        svc.detener();
    }

    @Test
    void setVolumen_getVolumen_roundTrip() {
        svc.setVolumen(0.75);
        assertEquals(0.75, svc.getVolumen(), 0.0001);
    }

    @Test
    void setVolumen_porEncimaDe1_clampaA1() {
        svc.setVolumen(1.5);
        assertEquals(1.0, svc.getVolumen(), 0.0001);
    }

    @Test
    void setVolumen_porDebajoDe0_clampaA0() {
        svc.setVolumen(-0.5);
        assertEquals(0.0, svc.getVolumen(), 0.0001);
    }

    @Test
    void setVolumen_enValorExacto1_seMantiene() {
        svc.setVolumen(1.0);
        assertEquals(1.0, svc.getVolumen(), 0.0001);
    }

    @Test
    void setVolumen_enValorExacto0_seMantiene() {
        svc.setVolumen(0.0);
        assertEquals(0.0, svc.getVolumen(), 0.0001);
    }

    @Test
    void reproducirDespuesDeDetener_noLanzaExcepcion() {
        svc.reproducir(MusicaServicio.Track.MENU);
        svc.detener();
        svc.reproducir(MusicaServicio.Track.JUEGO);
    }

    @Test
    void reproducirMismoTrackDosVeces_noLanzaExcepcion() {
        svc.reproducir(MusicaServicio.Track.MENU);
        svc.reproducir(MusicaServicio.Track.MENU);
    }

    @Test
    void volumenPorDefecto_es0_5() {
        assertEquals(0.5, svc.getVolumen(), 0.0001);
    }

    @Test
    void setVolumen_0_33_getVolumen_0_33() {
        svc.setVolumen(0.33);
        assertEquals(0.33, svc.getVolumen(), 0.0001);
    }

    @Test
    void reproducir_todosLosTracksEnSecuencia_noLanzaExcepcion() {
        svc.reproducir(MusicaServicio.Track.MENU);
        svc.reproducir(MusicaServicio.Track.LOBBY);
        svc.reproducir(MusicaServicio.Track.JUEGO);
    }
}
