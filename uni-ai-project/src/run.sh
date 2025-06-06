#!/bin/bash

# Questo script serve a lanciare il client Java per il gioco TORCS.

# Vai nella directory src (se lo script è lanciato altrove)
cd "$(dirname "$0")"

# Esegui il programma Java
java -cp classes it.unisa.diem.ai.torcs.Client it.unisa.diem.ai.torcs.SimpleDriver host:localhost port:3001 verbose:on
