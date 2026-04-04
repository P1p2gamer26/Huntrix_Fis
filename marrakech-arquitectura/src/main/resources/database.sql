CREATE TABLE Jugador(
    id_jugador INT AUTO_INCREMENT PRIMARY KEY,
    nombre_usuario VARCHAR(50) UNIQUE NOT NULL,
    correo VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    fecha_registro TIMESTAMP NOT NULL,
    estado VARCHAR(15)
);

CREATE TABLE Estadisticas(
    id_estadistica INT AUTO_INCREMENT PRIMARY KEY,
    partidas_jugadas INT DEFAULT 0,
    partidas_ganadas INT DEFAULT 0,
    partidas_perdidas INT DEFAULT 0,
    total_monedas INT DEFAULT 0,
    ultima_actualizacion TIMESTAMP,
    id_jugador INT UNIQUE,
    FOREIGN KEY (id_jugador) REFERENCES Jugador (id_jugador)
);

CREATE TABLE Sala(
    id_sala INT AUTO_INCREMENT PRIMARY KEY,
    codigo_sala VARCHAR(20) UNIQUE NOT NULL,
    estado VARCHAR(15) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL,
    id_host INT NOT NULL,
    FOREIGN KEY (id_host) REFERENCES Jugador(id_jugador)
);

CREATE TABLE ConfiguracionPartida(
    id_config INT AUTO_INCREMENT PRIMARY KEY,
    max_jugadores INT NOT NULL CHECK (max_jugadores BETWEEN 2 AND 4),
    poderes_habilitados BOOLEAN NOT NULL,
    id_sala INT UNIQUE NOT NULL,
    FOREIGN KEY (id_sala) REFERENCES Sala(id_sala)
);

CREATE TABLE Partida(
    id_partida INT AUTO_INCREMENT PRIMARY KEY,
    estado VARCHAR(15) NOT NULL,
    fecha_inicio TIMESTAMP NOT NULL,
    fecha_fin TIMESTAMP,
    turno_actual INT,
    ultima_accion TIMESTAMP,
    id_sala INT UNIQUE NOT NULL,
    FOREIGN KEY (id_sala) REFERENCES Sala(id_sala)
);

CREATE TABLE Participacion(
    id_participacion INT AUTO_INCREMENT PRIMARY KEY,
    nombre_partida VARCHAR(50) NOT NULL,
    rol VARCHAR(15) NOT NULL,
    color VARCHAR(20) NOT NULL,
    listo BOOLEAN DEFAULT FALSE,
    estado_conexion VARCHAR(15) NOT NULL,
    monedas_actuales INT NOT NULL,
    alfombras_restantes INT NOT NULL,
    alfombras_colocadas INT DEFAULT 0,
    es_ganador BOOLEAN DEFAULT FALSE,
    fecha_ultimo_cambio TIMESTAMP,
    id_partida INT NOT NULL,
    id_jugador INT NOT NULL,
    FOREIGN KEY (id_partida) REFERENCES Partida(id_partida),
    FOREIGN KEY (id_jugador) REFERENCES Jugador(id_jugador)
);

CREATE TABLE Poder(
    id_poder INT AUTO_INCREMENT PRIMARY KEY,
    tipo VARCHAR(30) NOT NULL,
    fila INT NOT NULL,
    col INT NOT NULL,
    estado VARCHAR(15),
    fecha_creacion TIMESTAMP NOT NULL,
    id_partida INT NOT NULL,
    FOREIGN KEY (id_partida) REFERENCES Partida(id_partida)
);

CREATE TABLE EstadoPartida(
    id_estado INT AUTO_INCREMENT PRIMARY KEY,
    turno_numero INT NOT NULL,
    assam_fila INT NOT NULL,
    assam_col INT NOT NULL,
    assam_direccion VARCHAR(10),
    tablero_estado TEXT NOT NULL,
    fecha_guardado TIMESTAMP NOT NULL,
    id_partida INT NOT NULL,
    FOREIGN KEY (id_partida) REFERENCES Partida(id_partida)
);

CREATE TABLE Mensajechat(
    id_mensaje INT AUTO_INCREMENT PRIMARY KEY,
    texto VARCHAR(500) NOT NULL,
    fecha_hora TIMESTAMP NOT NULL,
    id_sala INT NOT NULL,
    id_jugador INT NOT NULL,
    FOREIGN KEY (id_sala) REFERENCES Sala(id_sala),
    FOREIGN KEY (id_jugador) REFERENCES Jugador(id_jugador)
);

