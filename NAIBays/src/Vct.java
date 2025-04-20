import java.util.Arrays;

public class Vct {

    private String Name;
    private int size;
    private double coordinates[];


    public Vct(String name, int size) {
        this.Name = name.trim();
        this.size = size;
        coordinates = new double[size];
    }


    public double[] getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(double[] coordinates) {
        this.coordinates = coordinates;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        this.Name = name;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return Arrays.toString(coordinates) + " " + Name;
    }
}
