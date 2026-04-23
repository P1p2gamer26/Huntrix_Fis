#  Marrakech Game — Arquitectura de Software

> Proyecto académico — Pontificia Universidad Javeriana  
> Ingeniería de Sistemas — Segunda Entrega

---

##  Equipo de desarrollo — Huntrix_Fis

| Nombre |

| Daniel Camilo Cedeño Chavarro |
| Julián Felipe Africano Preciado |
| Juliana Aguirre Ballesteros |
| Diego Alejandro Melgarejo |

---

##  ¿De qué trata el juego?

Marrakech es un juego de mesa de estrategia donde los jugadores compiten por dominar el mercado de un zoco marroquí. El personaje principal se llama **Assam**, y en cada turno los jugadores lo mueven por el tablero, colocan alfombras y gestionan sus monedas (Dirhams) intentando ganar más territorio y riqueza que los demás.

### Objetivo del juego

- Controlar la mayor cantidad de casillas del tablero con tus alfombras
- Tener más Dirhams que los otros jugadores al final
- Hacer que Assam caiga en tus alfombras para cobrar renta
- Tomar decisiones estratégicas en cada turno

### ¿Qué hay de nuevo en esta versión?

Para esta entrega expandimos el juego base con varias funcionalidades nuevas:

- **Modo multijugador online en tiempo real** — dos jugadores en distintas máquinas pueden jugar en la misma partida a través de una base de datos compartida
- **Chat en tiempo real** — los jugadores pueden hablar durante la partida
- **Sistema de lobby** — crear sala, compartir código, esperar jugadores e iniciar la partida
- **Ranking mensual** — tabla con las victorias de cada jugador
- **Sesión única** — no se puede abrir la misma cuenta en dos terminales al mismo tiempo
- **Pantalla de perfil** — muestra tus datos y victorias

---

##  Arquitectura del proyecto

El proyecto está organizado en una arquitectura por capas para separar responsabilidades y mantener el código ordenado:

```
com.marrakech.game
│
├── application/         → Lógica de aplicación (servicios)
├── domain/              → Modelo del dominio (reglas del juego)
│   └── models/          → Board, Player, Tile, Carpet, Assam
├── infrastructure/      → Persistencia y base de datos
│   ├── database/        → Conexión H2
│   ├── persistence/     → JugadorRepository
│   ├── PartidaRepository.java
│   └── ChatRepository.java
├── presentation/        → Interfaz gráfica
│   ├── controllers/     → AuthController, GameController
│   ├── views/           → Todas las pantallas
│   └── MusicaManager.java
└── resources/
    ├── images/          → Tablero, alfombras, assam, fondos
    ├── audio/           → Música del juego
    └── styles.css / game.css
```

### Capas en detalle

**Domain** — las entidades del juego: `Board`, `Player`, `Tile`, `Carpet`, `Assam`. Aquí vive la lógica de reglas.

**Application** — los servicios que orquestan los casos de uso: `GameEngine`, `StatsService`.

**Infrastructure** — todo lo que tiene que ver con datos: conexión a H2, repositorios de jugadores, partidas y chat.

**Presentation** — las pantallas de JavaFX y los controladores que conectan la UI con la lógica.

---

##  Pantallas del juego

| Pantalla | Descripción |
|----------|-------------|
| Welcome | Pantalla de bienvenida con opciones de crear cuenta o iniciar sesión |
| Register | Registro con validaciones en tiempo real (correo, apodo, contraseña) |
| Login | Inicio de sesión con detección de sesión activa en otro dispositivo |
| Menú principal | Acceso a jugar, reglas, configuración y perfil |
| Perfil | Datos del usuario, foto y victorias |
| Modo Online | Ranking mensual y botones para crear o unirse a partida |
| Crear partida | Configurar número de jugadores, dificultad y opciones |
| Unirse a partida | Ingresar código de sala |
| Sala de espera | Lobby con código de sala, lista de jugadores y botón de inicio |
| Juego | Tablero completo con chat lateral, controles y panel de jugadores |
| Fin de juego | Pantalla estilo árabe con resultado, ganador y tabla de puntajes |
| Configuración | Volumen, brillo, resolución y modo de pantalla |
| Reglas | Explicación del juego |

---

## Funcionalidades implementadas

-  Registro e inicio de sesión con validaciones visuales
-  Sesión única por cuenta (no permite dos sesiones simultáneas)
-  Sistema de lobby con polling en tiempo real
-  Tablero de juego con lógica completa (movimiento de Assam, colocación de alfombras, cobro de renta)
-  Soporte para 2, 3 y 4 jugadores
-  Sincronización del estado del juego entre dos terminales vía H2 compartido
-  Chat en tiempo real entre jugadores durante la partida
-  Sistema de alfombras con orientación (horizontal/vertical) y solapamiento correcto
-  Pantalla de fin de juego con resultado individual (ganaste/perdiste) y tabla de puntajes
-  Ranking mensual de victorias
-  Música por pantalla (menú, lobby, juego) con control de volumen
-  Configuración: brillo en tiempo real, cambio de resolución y modo de pantalla
-  Perfil de usuario con foto editable
-  Cierre de sesión automático al cerrar la aplicación

---

##  Base de datos

Usamos **H2** en modo servidor TCP compartido para que dos instancias de la aplicación puedan conectarse a la misma base de datos en tiempo real.

El servidor H2 se levanta automáticamente en `Main.java` en el puerto `9092` con la opción `-tcpAllowOthers` para que cualquier instancia en la misma máquina pueda conectarse.

### Tablas principales

| Tabla | Descripción |
|-------|-------------|
| `Jugador` | Usuarios registrados, contraseñas, sesión activa, foto |
| `partidas` | Salas de juego con estado (ESPERA / INICIADA) |
| `partida_jugadores` | Relación de jugadores por partida |
| `estado_juego` | Estado serializado del tablero por turno |
| `chat_mensajes` | Mensajes del chat en tiempo real |
| `ranking` | Victorias por usuario |

### Cómo correr la base de datos

La base de datos se crea automáticamente al correr el proyecto. Si hay errores por columnas viejas, borra los archivos `marrakechdb.mv.db` y `marrakechdb.trace.db` de la carpeta `marrakech-arquitectura/` y vuelve a correr.

---

##  Cómo correr el proyecto

### Requisitos

- Java 17
- Maven 3.8+
- Git

### Pasos

```bash
# 1. Clonar el repositorio
git clone https://github.com/P1p2gamer26/Huntrix_Fis.git

# 2. Entrar a la carpeta del proyecto
cd Huntrix_Fis/marrakech-arquitectura

# 3. Compilar y correr
mvn javafx:run
```

### Para jugar en dos terminales (multijugador)

1. Abrir una terminal, ir a `marrakech-arquitectura/` y correr `mvn clean javafx:run`
2. Abrir otra terminal (o ventana), ir a la misma carpeta y correr `mvn javafx:run` (sin `clean` para no recompilar)
3. Crear cuenta en cada terminal con usuarios distintos
4. En la primera, crear una partida y copiar el código de sala
5. En la segunda, unirse con ese código
6. El host presiona "Iniciar partida" y ambas pantallas entran al tablero al mismo tiempo

---

##  Música utilizada

El proyecto usa música libre de derechos de autor. Las pistas se descargaron de YouTube y se usan exclusivamente con fines académicos y no comerciales.

| Pista | Uso en el juego | Fuente |
|-------|----------------|--------|
| Música ambiente árabe/marroquí | Menú principal y lobby | [YouTube — Moroccan ambient music](https://www.youtube.com/watch?v=dPRxIFYhFIA&t=2s) |
| Música ambiente 2 | Menú alternativo | [YouTube — misma fuente](https://www.youtube.com/watch?v=dPRxIFYhFIA&t=2s) |
| Música de partida | Durante el juego | [YouTube — Marrakech game music](https://www.youtube.com/watch?v=60VE1Gt83AE&list=RD60VE1Gt83AE&start_radio=1) |

> **Nota de derechos de autor:** Toda la música incluida en este repositorio se usa únicamente con fines educativos y académicos, sin ningún ánimo de lucro. Los derechos pertenecen a sus respectivos autores. Si eres el propietario de alguna de estas piezas y deseas que sea removida, por favor contáctanos.

---

##  Tecnologías utilizadas

| Tecnología | Uso |
|-----------|-----|
| **Java 17** | Lenguaje principal |
| **JavaFX 17** | Interfaz gráfica |
| **Maven** | Gestión de dependencias y compilación |
| **H2 Database** | Base de datos embebida en modo TCP compartido |
| **Git & GitHub** | Control de versiones y colaboración |

---

##  Estructura de ramas

| Rama | Descripción |
|------|-------------|
| `fix` | Rama principal de Juliana — diseño, estética y funcionalidades de UI |
| `probando-logica-vieja` | Rama de Diego — lógica del juego y multijugador |
| `main` | Rama de entrega oficial |
| `DB` | Rama de daniel-base de datos |
| `develop` | Rama de julian africano-implementacion |
| `screen` | Rama de desarrollo de pantallas |


---

##  Estado del proyecto — Segunda entrega

- [x] Arquitectura por capas implementada
- [x] Juego local funcional (2-4 jugadores)
- [x] Multijugador online funcional (2 jugadores, misma red)
- [x] Chat en tiempo real
- [x] Sistema de cuentas con sesión única
- [x] Ranking de victorias
- [x] Configuración de audio, brillo y pantalla
- [x] Pantallas completas con diseño árabe consistente
- [ ] Poderes especiales (en desarrollo para próxima entrega)
- [ ] Multijugador en red externa (pendiente)

---

*Pontificia Universidad Javeriana — Ingeniería de Sistemas — Arquitectura de Software 2026*
