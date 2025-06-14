#!/bin/bash

# Questo script serve a lanciare il client di guida manuale per il simulatore TORCS.

# Vai nella root (se lo script è lanciato altrove)
cd "$(dirname "$0")/.."


# Esegui il programma Java
echo "Eseguo il client di guida manuale per TORCS..."

java -cp src/classes it.unisa.diem.ai.torcs.io.Client it.unisa.diem.ai.torcs.agent.HumanDriver host:localhost port:3001
