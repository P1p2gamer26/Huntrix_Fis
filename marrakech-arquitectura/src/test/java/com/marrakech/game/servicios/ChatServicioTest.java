package com.marrakech.game.servicios;

import com.marrakech.game.repositorio.IChatRepositorio;
import com.marrakech.game.repositorio.ChatRepositorio.Mensaje;

import org.junit.jupiter.api.*;
import org.mockito.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Pruebas unitarias para ChatServicio con 100 % de cobertura de ramas. */
class ChatServicioTest {

    @Mock private IChatRepositorio chatRepo;

    private ChatServicio svc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        svc = new ChatServicio(chatRepo);
    }

    // ── CASO COMPLEJO 1: inicializar crea la tabla y guarda el último ID ───────

    @Test
    void inicializar_creaTablaYGuardaUltimoId() {
        when(chatRepo.obtenerUltimoId("MRK-1")).thenReturn(5);

        svc.inicializar("MRK-1", "jugador1");

        verify(chatRepo).inicializarTabla();
        verify(chatRepo).obtenerUltimoId("MRK-1");
        assertEquals(5, svc.getUltimoMensajeId());
        assertEquals("MRK-1", svc.getPartidaId());
        assertEquals("jugador1", svc.getUsuarioActual());
    }

    // ── CASO COMPLEJO 2: cargarHistorial actualiza ultimoMensajeId ────────────

    @Test
    void cargarHistorial_actualizaUltimoIdConElUltimoDeLaLista() {
        svc.inicializar("MRK-2", "ana");
        List<Mensaje> historial = List.of(
            new Mensaje(1, "ana", "hola", "10:00"),
            new Mensaje(3, "bob", "hola", "10:01")
        );
        when(chatRepo.obtenerMensajes("MRK-2", 0)).thenReturn(historial);

        List<Mensaje> result = svc.cargarHistorial();

        assertEquals(2, result.size());
        assertEquals(3, svc.getUltimoMensajeId());
    }

    // ── CASO COMPLEJO 3: cargarHistorial con lista vacía no cambia el ID ──────

    @Test
    void cargarHistorial_listaVacia_noActualizaId() {
        when(chatRepo.obtenerUltimoId("MRK-3")).thenReturn(7);
        svc.inicializar("MRK-3", "user");
        when(chatRepo.obtenerMensajes("MRK-3", 0)).thenReturn(List.of());

        svc.cargarHistorial();

        // ultimoMensajeId debe seguir siendo 7 (el inicial)
        assertEquals(7, svc.getUltimoMensajeId());
    }

    // ── CASO COMPLEJO 4: enviar texto vacío no llama al repositorio ───────────

    @Test
    void enviar_textoVacio_noLlamaAlRepositorio() throws InterruptedException {
        svc.inicializar("MRK-4", "user");
        svc.enviar("");
        Thread.sleep(100); // pequeña espera por si el hilo se disparara
        verify(chatRepo, never()).enviarMensaje(any(), any(), any());
    }

    // ── CASO COMPLEJO 5: enviar texto válido llama al repositorio en hilo ─────

    @Test
    void enviar_textoValido_llamaAlRepositorio() throws InterruptedException {
        svc.inicializar("MRK-5", "player");
        svc.enviar("¡Hola a todos!");
        Thread.sleep(200); // dejar que el hilo del send termine
        verify(chatRepo).enviarMensaje("MRK-5", "player", "¡Hola a todos!");
    }

    // ── Cobertura adicional ───────────────────────────────────────────────────

    @Test
    void enviar_null_noLanzaExcepcion() {
        svc.inicializar("MRK-6", "user");
        assertDoesNotThrow(() -> svc.enviar(null));
    }

    @Test
    void detenerPolling_sinPollingActivo_noLanzaExcepcion() {
        assertDoesNotThrow(() -> svc.detenerPolling());
    }

    @Test
    void getChatRepo_retornaElRepositorioInyectado() {
        assertSame(chatRepo, svc.getChatRepo());
    }

    @Test
    void setUltimoMensajeId_actualizaCorrectamente() {
        svc.setUltimoMensajeId(99);
        assertEquals(99, svc.getUltimoMensajeId());
    }
}
