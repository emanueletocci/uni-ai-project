#!/bin/bash

# Questo script serve a lanciare il client Java per il gioco TORCS.

# Vai nella root (se lo script è lanciato altrove)
cd "$(dirname "$0")/.."

# Esegui il programma Java
java -cp src/classes it.unisa.diem.ai.torcs.utils.BilanciamentoDataset

