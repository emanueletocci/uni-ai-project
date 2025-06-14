#!/bin/bash

# Questo script serve a visualizzare la distribuzione delle features/samples nel dataset specificato nella classe

# Vai nella root (se lo script è lanciato altrove)
cd "$(dirname "$0")/.."

# Esegui il programma Java
java -cp src/classes it.unisa.diem.ai.torcs.io.Client it.unisa.diem.ai.torcs.agent.AutonomousDriver host:localhost port:3001

