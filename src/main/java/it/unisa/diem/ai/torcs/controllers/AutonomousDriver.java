package it.unisa.diem.ai.torcs.controllers;

import it.unisa.diem.ai.torcs.Sample;
import it.unisa.diem.ai.torcs.actions.Action;
import it.unisa.diem.ai.torcs.classifier.NearestNeighbor;
import it.unisa.diem.ai.torcs.sensors.SensorModel;
import it.unisa.diem.ai.torcs.utilities.FeatureNormalizer;

public class AutonomousDriver extends Controller {

    private final NearestNeighbor knn;
    private final Action action;

    public AutonomousDriver() {
        // Percorso del dataset da behavioral cloning
        knn = new NearestNeighbor("data/dataset.csv");
        action = new Action();
    }

    @Override
    public Action control(SensorModel sensors) {
        // Lettura sensori principali
        double angle = sensors.getAngleToTrackAxis();
        double position = sensors.getTrackPosition();
        double speed = sensors.getSpeed();
        double[] trackEdgeSensors = sensors.getTrackEdgeSensors();

        // Estrazione e normalizzazione delle feature
        double[] features = FeatureNormalizer.extractAndNormalizeFeatures(
                trackEdgeSensors,
                position,
                angle,
                speed
        );

        // Classificazione tramite KNN
        int predictedClass = knn.classify(new Sample(features), 5);

        /*
         * CLASSI
            0 = acceleraDritto
            1 = giraLeggeroSinistra
            2 = giraForteSinistra
            3 = giraLeggeroDestra
            4 = giraForteDestra
            5 = frena
            6 = retromarcia
            7 = mantieniVelocita
         */

        // Azione in base alla classe predetta (allineata al dataset semplificato)
        switch (predictedClass) {
            case 0: acceleraDritto(action); break;
            case 1: giraLeggeroSinistra(action); break;
            case 2: giraForteSinistra(action); break;
            case 3: giraLeggeroDestra(action); break;
            case 4: giraForteDestra(action); break;
            case 5: frena(action); break;
            case 6: retromarcia(action, sensors); break;
            default: mantieniVelocita(action); break;
        }


        // Cambio marcia automatico
        action.gear = getGear(sensors);

        // Safety: correzione in caso di uscita di pista
        if (Math.abs(position) > 0.8) {
            action.accelerate = 0.2f;
            action.steering = (position > 0) ? -1.0f : 1.0f;
            action.brake = 0.3f;
        }

        return action;
    }

    // Cambio marcia automatico
    private int getGear(SensorModel sensors) {
        final int[] gearUp = {5000, 6000, 6000, 6500, 7000, 0};
        final int[] gearDown = {0, 2500, 3000, 3000, 3500, 3500};
        int gear = sensors.getGear();
        double rpm = sensors.getRPM();

        if (gear < 1) return 1;
        if (gear < 6 && rpm >= gearUp[gear - 1]) return gear + 1;
        if (gear > 1 && rpm <= gearDown[gear - 1]) return gear - 1;
        return gear;
    }


    // Azioni di guida semplificate
// Dritto (accelera)
    private void acceleraDritto(Action action) {
        action.steering = 0.0;
        action.brake = 0.0;
        action.accelerate = 1.0;
    }

    // Gira leggermente a sinistra
    private void giraLeggeroSinistra(Action action) {
        action.steering = 0.3f;
        action.brake = 0.0;
        action.accelerate = 0.8f;
    }

    // Gira forte a sinistra
    private void giraForteSinistra(Action action) {
        action.steering = 0.7f;
        action.brake = 0.0;
        action.accelerate = 0.6f;
    }

    // Gira leggermente a destra
    private void giraLeggeroDestra(Action action) {
        action.steering = -0.3f;
        action.brake = 0.0;
        action.accelerate = 0.8f;
    }

    // Gira forte a destra
    private void giraForteDestra(Action action) {
        action.steering = -0.7f;
        action.brake = 0.0;
        action.accelerate = 0.6f;
    }

    // Frena (dritto)
    private void frena(Action action) {
        action.steering = 0.0;
        action.accelerate = 0.0;
        action.brake = 1.0;
    }

    // Retromarcia (con correzione angolo)
    private void retromarcia(Action action, SensorModel sensors) {
        action.gear = -1;
        action.accelerate = 0.3f;
        action.brake = 0.0;
        // Correggi la direzione in base all'angolo rispetto all'asse della pista
        action.steering = (float) (-sensors.getAngleToTrackAxis() / (Math.PI / 2));
    }

    // Nessuna azione / decelerazione
    private void mantieniVelocita(Action action) {
        action.accelerate = 0.5f;
        action.brake = 0.0;
        action.steering = 0.0;
    }



    @Override
    public void reset() {
        System.out.println("Restarting the race!");
    }

    @Override
    public void shutdown() {
        System.out.println("Bye bye!");
    }

    @Override
    public float[] initAngles() {
        float[] angles = new float[19];
        for (int i = 0; i < 5; i++) {
            angles[i] = -90 + i * 15;
            angles[18 - i] = 90 - i * 15;
        }
        for (int i = 5; i < 9; i++) {
            angles[i] = -20 + (i - 5) * 5;
            angles[18 - i] = 20 - (i - 5) * 5;
        }
        angles[9] = 0;
        return angles;
    }
}
