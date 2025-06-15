#!/bin/bash

# Questo script serve a visualizzare la distribuzione delle features/samples nel dataset specificato nella classe

# Vai nella root (se lo script è lanciato altrove)
cd "$(dirname "$0")/.."

# Esegui il programma Java

echo "Mostro distribuzione features..."
java -cp src/classes it.unisa.diem.ai.torcs.utils.debugging.MultiHistogramFromSamples
