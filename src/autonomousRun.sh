#!/bin/bash

# Questo script serve a lanciare il client di guida automatica mediante classificatore KNN per il simulatore TORCS.

# Vai nella root (se lo script è lanciato altrove)
cd "$(dirname "$0")/.."

echo "Eseguo il client di guida autonoma per TORCS..."

# Esegui il programma Java
java -cp src/classes it.unisa.diem.ai.torcs.io.Client it.unisa.diem.ai.torcs.agent.AutonomousDriver host:100.106.34.171 port:3001

