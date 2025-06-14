#!/bin/bash
# Vai nella root (se lo script è lanciato altrove)
cd "$(dirname "$0")/.."

echo "Clipping driver dataset..."
java -cp src/classes it.unisa.diem.ai.torcs.utils.ClipBalancedDataset data/driver_dataset.csv data/driver_dataset_balanced.csv

echo "Clipping recovery dataset..."
java -cp src/classes it.unisa.diem.ai.torcs.utils.ClipBalancedDataset data/recovery_dataset.csv data/recovery_dataset_balanced.csv

