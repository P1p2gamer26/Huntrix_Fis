package com.marrakech.game.service;

import com.marrakech.game.repository.IJugadorRepositorio;
import com.marrakech.game.repository.IPartidaRepositorio;
import com.marrakech.game.repository.PartidaRepositorio.RankingEntry;

import org.junit.jupiter.api.*;
import org.mockito.*;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Pruebas unitarias para AuthServicio con 100 % de cobertura de ramas. */
class AuthServicioTest {

    @Mock private IJugadorRepositorio jugadorRepo;
    @Mock private IPartidaRepositorio partidaRepo;

    private AuthServicio authSvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authSvc = new AuthServicio(jugadorRepo, partidaRepo);
    }

    // ── CASO COMPLEJO 1: registrarYLogin con apodo duplicado ──────────────────

    @Test
    void registrar_apodoDuplicado_retornaApodoExiste() {
        when(jugadorRepo.nombreExiste("Jamal")).thenReturn(true);

        String resultado = authSvc.registrarYLogin("Jamal", "j@test.com", "pass123");

        assertEquals("APODO_EXISTE", resultado);
        verify(jugadorRepo, never()).crearJugador(any(), any(), any());
    }

    // ── CASO COMPLEJO 2: registrarYLogin con correo duplicado ─────────────────

    @Test
    void registrar_correoDuplicado_retornaCorreoExiste() {
        when(jugadorRepo.nombreExiste("Nuevo")).thenReturn(false);
        when(jugadorRepo.correoExiste("dup@test.com")).thenReturn(true);

        String resultado = authSvc.registrarYLogin("Nuevo", "dup@test.com", "pass123");

        assertEquals("CORREO_EXISTE", resultado);
        verify(jugadorRepo, never()).crearJugador(any(), any(), any());
    }

    // ── CASO COMPLEJO 3: login con sesión ya activa en otro dispositivo ────────

    @Test
    void login_sesionActiva_retornaCentinela() {
        when(jugadorRepo.loginJugador("Pedro", "clave")).thenReturn("SESION_ACTIVA");

        String resultado = authSvc.login("Pedro", "clave");

        assertEquals("SESION_ACTIVA", resultado);
    }

    // ── CASO COMPLEJO 4: login con credenciales incorrectas ───────────────────

    @Test
    void login_credencialesIncorrectas_retornaNull() {
        when(jugadorRepo.loginJugador("NoExiste", "wrong")).thenReturn(null);

        String resultado = authSvc.login("NoExiste", "wrong");

        assertNull(resultado);
    }

    // ── CASO COMPLEJO 5: getVictorias busca en el ranking y filtra por usuario ─

    @Test
    void getVictorias_usuarioConVictorias_retornaConteoCorrect() {
        when(partidaRepo.obtenerRanking()).thenReturn(List.of(
            new RankingEntry("Maria", 5),
            new RankingEntry("Pedro", 3)
        ));

        int victorias = authSvc.getVictorias("Maria");

        assertEquals(5, victorias);
    }

    // ── Cobertura adicional ───────────────────────────────────────────────────

    @Test
    void registrar_exitoso_retornaNombreJugador() {
        when(jugadorRepo.nombreExiste("Ana")).thenReturn(false);
        when(jugadorRepo.correoExiste("ana@test.com")).thenReturn(false);
        when(jugadorRepo.crearJugador("Ana", "ana@test.com", "seg123")).thenReturn(true);
        when(jugadorRepo.loginJugador("Ana", "seg123")).thenReturn("Ana");

        String resultado = authSvc.registrarYLogin("Ana", "ana@test.com", "seg123");

        assertEquals("Ana", resultado);
        verify(jugadorRepo).crearJugador("Ana", "ana@test.com", "seg123");
    }

    @Test
    void registrar_errorBD_retornaErrorBD() {
        when(jugadorRepo.nombreExiste("Ana")).thenReturn(false);
        when(jugadorRepo.correoExiste("ana@test.com")).thenReturn(false);
        when(jugadorRepo.crearJugador("Ana", "ana@test.com", "seg123")).thenReturn(false);

        String resultado = authSvc.registrarYLogin("Ana", "ana@test.com", "seg123");

        assertEquals("ERROR_BD", resultado);
        verify(jugadorRepo, never()).loginJugador(any(), any());
    }

    @Test
    void login_exitoso_retornaNombreUsuario() {
        when(jugadorRepo.loginJugador("Luis", "pass")).thenReturn("Luis");

        String resultado = authSvc.login("Luis", "pass");

        assertEquals("Luis", resultado);
    }

    @Test
    void cerrarSesion_delegaAlRepositorio() {
        authSvc.cerrarSesion("Carlos");
        verify(jugadorRepo).cerrarSesion("Carlos");
    }

    @Test
    void getCorreo_delegaAlRepositorio() {
        when(jugadorRepo.getCorreo("Laura")).thenReturn("laura@test.com");
        assertEquals("laura@test.com", authSvc.getCorreo("Laura"));
    }

    @Test
    void getFechaRegistro_delegaAlRepositorio() {
        when(jugadorRepo.getFechaRegistro("Jorge")).thenReturn("2025-01-10");
        assertEquals("2025-01-10", authSvc.getFechaRegistro("Jorge"));
    }

    @Test
    void getVictorias_usuarioSinVictorias_retornaCero() {
        when(partidaRepo.obtenerRanking()).thenReturn(List.of());
        assertEquals(0, authSvc.getVictorias("Desconocido"));
    }

    @Test
    void obtenerIdJugador_delegaAlRepositorio() {
        when(jugadorRepo.obtenerIdJugador("Sofia")).thenReturn(42);
        assertEquals(42, authSvc.obtenerIdJugador("Sofia"));
    }

    @Test
    void guardarFoto_delegaAlRepositorio() {
        File f = mock(File.class);
        when(jugadorRepo.guardarFoto("Diego", f)).thenReturn(true);
        assertTrue(authSvc.guardarFoto("Diego", f));
    }

    @Test
    void getFoto_delegaAlRepositorio() {
        byte[] bytes = {1, 2, 3};
        when(jugadorRepo.getFoto("Elena")).thenReturn(bytes);
        assertArrayEquals(bytes, authSvc.getFoto("Elena"));
    }
}
