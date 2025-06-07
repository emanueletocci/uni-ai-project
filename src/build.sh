#!/bin/bash
# Vai nella directory src
cd "$(dirname "$0")"

# Crea la cartella di destinazione se non esiste
mkdir -p classes

# Compila tutti i file .java ricorsivamente da main/java
javac -d classes $(find main/java -name "*.java")

echo "Compilazione completata. I file .class sono stati salvati nella cartella 'classes'."
