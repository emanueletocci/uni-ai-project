package it.unisa.diem.ai.torcs.model;

import java.util.ArrayList;
import java.util.List;

public enum SensorFeature {
    SPEED_X("Speed X"),
    SPEED_Y("Speed Y"),
    ANGLE_TO_TRACK_AXIS("Angle to Track Axis"),
    TRACK_POSITION("Track Position"),
    TRACK_EDGE_SENSOR_4("Track Edge Sensor 4", 4),
    TRACK_EDGE_SENSOR_6("Track Edge Sensor 6", 6),
    TRACK_EDGE_SENSOR_8("Track Edge Sensor 8", 8),
    TRACK_EDGE_SENSOR_9("Track Edge Sensor 9", 9),
    TRACK_EDGE_SENSOR_10("Track Edge Sensor 10", 10),
    TRACK_EDGE_SENSOR_12("Track Edge Sensor 12", 12),
    TRACK_EDGE_SENSOR_14("Track Edge Sensor 14", 14);

    private final String displayName;
    private final Integer trackSensorIndex;

    SensorFeature(String displayName) {
        this.displayName = displayName;
        this.trackSensorIndex = null;
    }
    SensorFeature(String displayName, int trackSensorIndex) {
        this.displayName = displayName;
        this.trackSensorIndex = trackSensorIndex;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public Integer getTrackSensorIndex() {
        return trackSensorIndex;
    }
    public static List<Integer> getTrackSensorIndices() {
        List<Integer> indices = new ArrayList<>();
        for (SensorFeature feature : SensorFeature.values()) {
            if (feature.trackSensorIndex != null) {
                indices.add(feature.trackSensorIndex);
            }
        }
        return indices;
    }

    public static String csvHeader() {
        StringBuilder sb = new StringBuilder();
        for (SensorFeature f : SensorFeature.values()) {
            sb.append(f.name()).append(";");
        }
        sb.append("LABEL_CODE").append(";").append("LABEL_NAME");
        return sb.toString();
    }
}
