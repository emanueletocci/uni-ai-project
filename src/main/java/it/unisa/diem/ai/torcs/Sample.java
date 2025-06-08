package it.unisa.diem.ai.torcs;

/**
 * Sample class represents a single data point in the dataset.
 * It contains features and a class label, which can be used for classification tasks.
 * The class also provides methods to calculate the distance between samples.
 */
public class Sample {
    public double[] features;
    public int cls; // intero che rappresenta la classe del campione (1=accelera, 2=frena)

    /*
    Constructor to initialize the Sample with a given set of features and class label.
    This is typically used when building the dataset.
    */
    public Sample(double[] features, int cls) {
        this.features = features;
        this.cls = cls;
    }

    /*
    Constructor to initialize the Sample with a given set of features without a class label.
    This is used when classifying a new sample.
    */
    public Sample(double[] features) {
        this.features = features;
        this.cls = -1; // Default class value
    }

    /*
    Constructor to initialize the Sample from a CSV line.
    Assumes the last value in the CSV is the class label.
    */
    public Sample(String line) {
        String[] parts = line.split(";");
        int n = parts.length;
        features = new double[n - 1];
        for (int i = 0; i < n - 1; i++) {
            features[i] = Double.parseDouble(parts[i].trim());
        }
        this.cls = Integer.parseInt(parts[n - 1].trim());
    }

    /*
    Method to calculate the Euclidean distance between this sample and another sample.
    */
    public double distance(Sample other) {
        double sum = 0;
        for (int i = 0; i < this.features.length; i++) {
            sum += Math.pow(this.features[i] - other.features[i], 2);
        }
        return Math.sqrt(sum);
    }
}
