class Binner {
    double min, max;
    int numBins;

    Binner(double min, double max, int numBins) {
        this.min = min;
        this.max = max;
        this.numBins = numBins;
    }

    int getBin(double value) {
        double binSize = (max - min) / numBins;
        int bin = (int) ((value - min) / binSize);
        if (bin >= numBins) bin = numBins - 1;
        return bin;
    }
}