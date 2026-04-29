#!/bin/bash
# Buscar el jar de H2 en el repositorio local de Maven
H2_JAR=$(find ~/.m2/repository/com/h2database -name "h2-*.jar" | head -1)

if [ -z "$H2_JAR" ]; then
    echo "No se encontró H2. Compilando primero para descargarlo..."
    cd marrakech-arquitectura && mvn dependency:resolve -q && cd ..
    H2_JAR=$(find ~/.m2/repository/com/h2database -name "h2-*.jar" | head -1)
fi

echo "Usando H2: $H2_JAR"
echo "Servidor H2 corriendo en puerto 9092..."
echo "Presiona Ctrl+C para detenerlo"
java -cp "$H2_JAR" org.h2.tools.Server -tcp -tcpAllowOthers -tcpPort 9092 -baseDir .
