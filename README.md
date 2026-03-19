# Huntrix_Fis
Equipo de desarrollo

Proyecto académico desarrollado por estudiantes de ingeniería de sistemas Pontificia universidad Javeriana.

Formado por 

-Daniel Camilo Cedeño Chavarro 

-Julián Felipe Africano Preciado 

-Juliana Aguirre Ballesteros 

-Luna Valentina Leon Gonzales 

-Diego Alejandro Melgarejo 

Marrakesh Juego – Arquitectura de Software

Descripción

Marrakesh es una implementación digital del juego de mesa Marrakesh, desarrollada bajo principios de arquitectura por capas. El proyecto busca simular la dinámica estratégica del juego original y con nuevas modificaciones (poderes) asi permitiendo a los usuarios interactuar a través de una interfaz gráfica mientras se gestiona la lógica del juego y la persistencia de datos de forma desacoplada.

¿De qué trata el juego?

Marrakesh es un juego de estrategia donde los jugadores compiten por dominar el mercado colocando alfombras en un tablero. En cada turno los jugadores mueven a un personaje llamado Assam, colocan alfombras y gestionan monedas, intentando maximizar su territorio y riqueza, tambien se contara con la modificacion para extender el juego, se agregaron nuevos poderes y modificaciones para que el juego no sea tan monotono y se exploren diferentes modos de juego.

El objetivo es:

- Controlar la mayor cantidad de casillas del tablero
- Obtener más monedas que los demás jugadores
- Tomar decisiones estratégicas en cada turno
----------------------------------------------------------------------------
Arquitectura del Proyecto

El proyecto está estructurado en una arquitectura por capas, promoviendo separación de responsabilidades:

```
com.marrakech.game
│
├── application      → Lógica de aplicación (servicios)
├── domain           → Modelo del dominio (reglas del juego)
├── infrastructure   → Persistencia y acceso a datos
├── presentation     → Interfaz gráfica y controladores
└── resources        → Recursos (imágenes, estilos)

Domain

Contiene las entidades principales del juego:

* `Game`
* `Board`
* `Player`
* `Tile`
* `Carpet`
* `Assam`

Aquí se define la lógica central y reglas del juego.

----------------------------------------------------

Application

Gestiona los casos de uso del sistema:

* `GameEngine`: Controla el flujo del juego
* `StatsService`: Manejo de estadísticas

Infrastructure

Encargada de la persistencia:

* Conexión a base de datos
* Repositorios (`JdbcUserRepository`)

Presentation

Contiene la interfaz gráfica:

* Pantallas (Login, Registro, Lobby, Juego)
* Controladores que conectan la UI con la lógica del sistema

Resources

Incluye:

* Imágenes del juego (tablero, personajes, alfombras)
* Archivos de estilos
------------------------------------------------------------
Funcionalidades

* Registro e inicio de sesión de usuarios
* Sistema de lobby para gestión de partidas
* Representación gráfica del tablero
* Movimiento del personaje Assam
* Colocación de alfombras
* Sistema de turnos
* Persistencia de datos de usuarios

---
Tecnologías utilizadas

* Java
* Maven
* base de datos H2
* Git & GitHub

---


