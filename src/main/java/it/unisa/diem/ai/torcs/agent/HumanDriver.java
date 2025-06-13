package it.unisa.diem.ai.torcs.agent;

import it.unisa.diem.ai.torcs.model.*;
import it.unisa.diem.ai.torcs.model.Action;
import it.unisa.diem.ai.torcs.utils.FeatureExtractor;
import it.unisa.diem.ai.torcs.model.KeyInput;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import it.unisa.diem.ai.torcs.io.ContinuousCharReaderUI;
import it.unisa.diem.ai.torcs.utils.RadarVisualizer;

import javax.swing.*;

public class HumanDriver extends Controller {
    /* Costanti di cambio marcia */
    final int[] gearUp = { 5000, 6000, 6000, 6500, 7000, 0 };
    final int[] gearDown = { 0, 2500, 3000, 3000, 3500, 3500 };

    /* Constanti */
    final int stuckTime = 25;
    final float stuckAngle = (float) 0.523598775; // PI/6

    /* Costanti di accelerazione e di frenata */
    final float maxSpeedDist = 70;
    final float maxSpeed = 150;
    final float sin5 = (float) 0.08716;
    final float cos5 = (float) 0.99619;

    /* Costanti di sterzata */
    final float steerLock = (float) 0.785398;
    final float steerSensitivityOffset = (float) 80.0;
    final float wheelSensitivityCoeff = 1;

    /* Costanti del filtro ABS */
    final float wheelRadius[] = { (float) 0.3179, (float) 0.3179, (float) 0.3276, (float) 0.3276 };
    final float absSlip = (float) 2.0;
    final float absRange = (float) 3.0;
    final float absMinSpeed = (float) 3.0;

    /* Costanti da stringere */
    final float clutchMax = (float) 0.5;
    final float clutchDelta = (float) 0.05;
    final float clutchRange = (float) 0.82;
    final float clutchDeltaTime = (float) 0.02;
    final float clutchDeltaRaced = 10;
    final float clutchDec = (float) 0.01;
    final float clutchMaxModifier = (float) 1.3;
    final float clutchMaxTime = (float) 1.5;

    private int stuck = 0;

    // current clutch
    private float clutch = 0;


    private static final RadarVisualizer radar = new RadarVisualizer();
    static {
        SwingUtilities.invokeLater(ContinuousCharReaderUI::new);
        SwingUtilities.invokeLater(() -> RadarVisualizer.showRadar(radar));
    }
    private BufferedWriter writer;
    private final FeatureExtractor extractor;

    public HumanDriver() {
        try {
            writer = new BufferedWriter(new FileWriter("data/dataset.csv", true));
        } catch (IOException e) {
            e.printStackTrace();
        }
        extractor = new FeatureExtractor();
    }

    @Override
    public Action control(SensorModel sensors) {
        // 1. Leggi i comandi dalla tastiera (KeyInput)
        Action action = new Action();
        action.accelerate = KeyInput.up ? 1.0 : 0.0;
        action.brake = KeyInput.brake ? filterABS(sensors, 1f) : 0.0;
        action.steering = KeyInput.left ? 1.0 : (KeyInput.right ? -1.0 : 0.0);
        action.gear = KeyInput.down ? -1 : 1; // esempio: S = retromarcia, altrimenti avanti

        action.clutch = clutching(sensors, (float) action.clutch);
        action.gear = getGear(sensors);
        // 2. Estrai le feature dai sensori
        Feature feature = extractor.extractFeatures(sensors);

        // 3. Discretizza l'azione in una label
        Label label = Label.fromAction(action);

        Sample sample = new Sample(feature, label);

        // 4. Salva il sample sul csv
        try {
            writer.write(sample.toCSV());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return action;
    }

    @Override
    public void shutdown() {
        try {
            if (writer != null) writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reset() {
        // Eventuale logica di reset
    }

    private float filterABS(SensorModel sensors, float brake) {
        // Converte la velocità in m/s
        float speed = (float) (sensors.getSpeed() / 3.6);

        // Quando la velocità è inferiore alla velocità minima per l'abs non interviene in caso di frenata
        if (speed < absMinSpeed)
            return brake;

        // Calcola la velocità delle ruote in m/s
        float slip = 0.0f;
        for (int i = 0; i < 4; i++) {
            slip += sensors.getWheelSpinVelocity()[i] * wheelRadius[i];
        }

        // Lo slittamento è la differenza tra la velocità effettiva dell'auto e la velocità media delle ruote
        slip = speed - slip / 4.0f;

        // Quando lo slittamento è troppo elevato, si applica l'ABS
        if (slip > absSlip) {
            brake = brake - (slip - absSlip) / absRange;
        }

        // Controlla che il freno non sia negativo, altrimenti lo imposta a zero
        if (brake < 0)
            return 0;
        else
            return brake;
    }

    float clutching(SensorModel sensors, float clutch) {

        float maxClutch = clutchMax;

        // Controlla se la situazione attuale è l'inizio della gara
        if (sensors.getCurrentLapTime() < clutchDeltaTime && getStage() == Stage.RACE
                && sensors.getDistanceRaced() < clutchDeltaRaced)
            clutch = maxClutch;

        // Regolare il valore attuale della frizione
        if (clutch > 0) {
            double delta = clutchDelta;
            if (sensors.getGear() < 2) {

                // Applicare un'uscita più forte della frizione quando la marcia è una e la corsa è appena iniziata.
                delta /= 2;
                maxClutch *= clutchMaxModifier;
                if (sensors.getCurrentLapTime() < clutchMaxTime)
                    clutch = maxClutch;
            }

            // Controllare che la frizione non sia più grande dei valori massimi
            clutch = Math.min(maxClutch, clutch);

            // Se la frizione non è al massimo valore, diminuisce abbastanza rapidamente
            if (clutch != maxClutch) {
                clutch -= delta;
                clutch = Math.max((float) 0.0, clutch);
            }
            // Se la frizione è al valore massimo, diminuirla molto lentamente.
            else
                clutch -= clutchDec;
        }
        return clutch;
    }
    private int getGear(SensorModel sensors) {
        int gear = sensors.getGear();
        double rpm = sensors.getRPM();

        // Se la marcia è 0 (N) o -1 (R) restituisce semplicemente 1
        if (gear < 1)
            return 1;

        // Se il valore di RPM dell'auto è maggiore di quello suggerito
        // sale di marcia rispetto a quella attuale
        if (gear < 6 && rpm >= gearUp[gear - 1])
            return gear + 1;
        else

            // Se il valore di RPM dell'auto è inferiore a quello suggerito
            // scala la marcia rispetto a quella attuale
            if (gear > 1 && rpm <= gearDown[gear - 1])
                return gear - 1;
            else // Altrimenti mantenere l'attuale
                return gear;
    }
}
