import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Main {

    static int numBins = 5;

    public static void main(String[] args) {

        Path testPath = Paths.get("iris_test.txt");
        Path trainingPath = Paths.get("iris_training.txt");
        List<Vct> TrainingVectors = new ArrayList<>();
        List<Vct> TestVectors = new ArrayList<>();

        try {
            Files.lines(trainingPath).forEach(line -> TrainingVectors.add(fileOperations(line)));
            Files.lines(testPath).forEach(line -> TestVectors.add(fileOperations(line)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int iloscCech = TrainingVectors.get(0).getSize();

        // Oblicz min i max dla każdej cechy
        double[] minVals = new double[iloscCech];
        double[] maxVals = new double[iloscCech];


        for (Vct v : TrainingVectors) {
            for (int i = 0; i < iloscCech; i++) {
                minVals[i] = Math.min(minVals[i], v.getCoordinates()[i]);
                maxVals[i] = Math.max(maxVals[i], v.getCoordinates()[i]);
            }
        }

        // Tworzymy binery
        Binner[] binners = new Binner[iloscCech];
        for (int i = 0; i < iloscCech; i++) {
            binners[i] = new Binner(minVals[i], maxVals[i], numBins);
        }

        // Lista klas
        Set<String> klasy = new HashSet<>();
        for (Vct v : TrainingVectors) {
            klasy.add(v.getName());
        }

        // Przygotuj liczniki
        Map<String, Integer> licznikKlas = new HashMap<>();
        Map<String, int[][]> cechyBinCounts = new HashMap<>();


        for (var k : klasy) {
            licznikKlas.put(k, 0);
            cechyBinCounts.put(k, new int[iloscCech][numBins]);
        }

        // Wypełnij liczniki
        for (Vct v : TrainingVectors) {
            licznikKlas.put(v.getName(), licznikKlas.get(v.getName()) + 1);
            int[][] bins = cechyBinCounts.get(v.getName());
            for (int i = 0; i < iloscCech; i++) {
                int bin = binners[i].getBin(v.getCoordinates()[i]);
                bins[i][bin]++;
            }
        }

        // Wypisz prawdopodobieństwa
        for (var k : klasy) {
            System.out.println("Klasa: " + k);
            int[][] bins = cechyBinCounts.get(k);
            int totalInClass = licznikKlas.get(k);

            for (int i = 0; i < iloscCech; i++) {
                System.out.print("Cechy " + i + ": ");
                for (int j = 0; j < numBins; j++) {
                    double prawdopodobienstwo = (double) bins[i][j] / totalInClass;
                    System.out.print(prawdopodobienstwo + " | ");
                }
                System.out.println();
            }
            System.out.println();
        }

        // Testowanie
        int prawidlowe = 0;
        List<String> listaKlas = new ArrayList<>(klasy);
        Collections.sort(listaKlas);

        int[][] macierzOmylek = new int[listaKlas.size()][listaKlas.size()];
        Map<String, Integer> classToIndex = new HashMap<>();
        for (int i = 0; i < listaKlas.size(); i++) {
            classToIndex.put(listaKlas.get(i), i);
        }

        for (Vct v : TestVectors) {
            String predicted = predict(v, binners, cechyBinCounts, licznikKlas, klasy, numBins);
            if (predicted.equals(v.getName())) {
                prawidlowe++;
            }
            int actualIdx = classToIndex.get(v.getName());
            int predictedIdx = classToIndex.get(predicted);
            macierzOmylek[actualIdx][predictedIdx]++;
        }

        double skutecznosc = 100.0 * prawidlowe / TestVectors.size();
        System.out.print("Skutecznosc:"+ skutecznosc);

        // Macierz omyłek
        System.out.println("\nMacierz omyłek:");
        System.out.print("         ");
        for (String c : listaKlas) {
            System.out.printf("%15s", c);
        }
        System.out.println();
        for (int i = 0; i < listaKlas.size(); i++) {
            System.out.printf("%10s", listaKlas.get(i));
            for (int j = 0; j < listaKlas.size(); j++) {
                System.out.printf("%15d", macierzOmylek[i][j]);
            }
            System.out.println();
        }
    }

    private static Vct fileOperations(String line) {
        String[] row = line.split("\t");

        for (int i = 0; i < row.length; i++) {
            row[i] = row[i].replace(',', '.');
        }

        String name = row[row.length - 1];

        Vct vct = new Vct(name, row.length - 1);
        vct.setCoordinates(
                Arrays.stream(row, 0, row.length - 1)
                        .mapToDouble(Double::parseDouble)
                        .toArray()
        );
        return vct;
    }

    private static String predict(Vct v, Binner[] binners, Map<String, int[][]> cechyBinCounts,
                                  Map<String, Integer> classCounts, Set<String> classes, int numBins) {
        double bestProb = -1;
        String bestClass = null;

        for (var k : classes) {
            double prawdopodobienstwo = (double) classCounts.get(k) / classCounts.values().stream().mapToInt(Integer::intValue).sum();
            int[][] bins = cechyBinCounts.get(k);
            int totalInClass = classCounts.get(k);

            for (int i = 0; i < v.getSize(); i++) {
                int bin = binners[i].getBin(v.getCoordinates()[i]);
                int count = bins[i][bin];

                double featureProb;
                if (count == 0) {
                    featureProb = 1.0 / (totalInClass + numBins);  // Wygładzanie tylko dla zera
                } else {
                    featureProb = (double) count / totalInClass;   // Normalne liczenie
                }

                prawdopodobienstwo *= featureProb; // Mnożymy prawdopodobieństwa
            }

            if (bestClass == null || prawdopodobienstwo > bestProb) {
                bestProb = prawdopodobienstwo;
                bestClass = k;
            }
        }

        return bestClass;
    }
}

