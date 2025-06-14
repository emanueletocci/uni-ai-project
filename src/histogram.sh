#!/bin/bash

# Questo script serve a lanciare il client Java per il gioco TORCS.

# Vai nella root (se lo script è lanciato altrove)
cd "$(dirname "$0")/.."


# Esegui il programma Java

echo "Mostro distribuzione features..."

java -cp src/classes it.unisa.diem.ai.torcs.utils.MultiHistogramFromSamples
