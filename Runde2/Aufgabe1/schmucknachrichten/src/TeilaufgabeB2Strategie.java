import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

// DigitCost in Strategie 1, just renamed
class Perle implements Comparable<Perle> {
    int digit;
    double cost;

    Perle(double cost, int digit) {
        this.cost = cost;
        this.digit = digit;
    }

    @Override
    public int compareTo(Perle o) {
        return Double.compare(this.cost, o.cost);
    }
}

class Symbol implements Comparable<Symbol> {
    Character character;
    double probability;
    // Cumulative prob for bin assignment
    double cumulativeProbStart; // P_{i-1} in paper
    double cumulativeProbEnd; // P_i in paper

    Symbol(Character c, double p) {
        this.character = c;
        this.probability = p;
    }

    @Override
    public int compareTo(Symbol other) {
        // Sort descending, like assumed by paper
        int probCompare = Double.compare(other.probability, this.probability);
        if (probCompare != 0)
            return probCompare;
        // Secondary sort with character for stable order
        return Character.compare(this.character, other.character);
    }
}

public class TeilaufgabeB2Strategie {
    private static final double TOLERANCE = 1e-9; // For floating point number comparisions

    // Implements most of core functionality of the algorithm by Golin/Li
    //https://doi.org/10.48550/arXiv.0705.0253
    public static void main(String[] args) {
        String input;
        try {
            // 1. Read input from file
            input = Files.readString(Path.of("beispielaufgaben/b/schmuck9.txt"));
        } catch (IOException e) {
            System.out.println("Error: InputFile not found");
            return;
        }
        InputWrapper inputWrapper = parseInput(input);

        // Usable input from file
        int numColors = inputWrapper.numColors;
        String text = inputWrapper.text;
        List<Perle> sortedDiameters = new ArrayList<>();
        double[] diameters = inputWrapper.diameters.stream().mapToDouble(x -> x.doubleValue()).toArray();
        int index = 0;
        for (int i : inputWrapper.diameters) {
            sortedDiameters.add(new Perle(i, index));
            index++;
        }

        if (numColors < 2) {
            System.out.println("Error: Die Anzahl der Farben muss mindestens 2 sein");
            return;
        }

        // 2. Calculate character frequency
        Map<Character, Long> frequencyMap = buildFrequencyMap(text);
        int numCharacters = frequencyMap.size();

        long totalFrequency = text.length();
        List<Symbol> symbols = new ArrayList<>(numCharacters);
        double cumulativeP = 0.0;

        // Create List of Symbols sorted by frequency - descending
        frequencyMap.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue())) // descending frequency
                .forEach(entry -> {
                    Symbol si = new Symbol(entry.getKey(), (double) entry.getValue() / totalFrequency); // probability < 1 and > 0
                    symbols.add(si);
                });

        // Calcuate cumulative probability for sorted list
        cumulativeP = 0.0;
        for (Symbol si : symbols) {
            si.cumulativeProbStart = cumulativeP;
            cumulativeP += si.probability;
            si.cumulativeProbEnd = cumulativeP;
        }
        // Adjust last probability to 1.0
        if (!symbols.isEmpty()) {
            symbols.get(symbols.size() - 1).cumulativeProbEnd = 1.0;
        }

        Map<Character, String> codeTable = new HashMap<>();

        // Find c, characteristic root: sum(2^(-c * diameter_i)) = 1
        double cRoot = findCharacteristicRoot(diameters);
        System.out.printf("Charakteristische Wurzel c = %.6f\n", cRoot);

        // 2. Recursive encoding method
        if (!symbols.isEmpty()) {
            // Give index range of list [l, r] -> [0, n-1]
            generateCodesGolinLi(symbols, 0, numCharacters - 1, "", codeTable, sortedDiameters, cRoot);
        }

        System.out.println("\nCodetabelle (Golin/Li):");
        printCodeTable(codeTable, frequencyMap, diameters);

        long totalLength = calculateTotalCost(codeTable, frequencyMap, inputWrapper.diameters);
        System.out.println("\nGesamtlänge der Botschaft " + totalLength + " (Anzahl in Perlen) bzw. "
                + totalLength / 10.0 + "cm");
    }

    // 1. Read input
    private static InputWrapper parseInput(String input) {
        // Save every line in array of Strings
        String[] lines = input.split("\n");
        List<Integer> list = new ArrayList<>();
        for (String s : lines[1].split(" ")) {
            list.add(Integer.valueOf(s));
        }
        return new InputWrapper(Integer.parseInt(lines[0].trim()), lines[2], list);
    }

    // 2. Calculate character frequency from input text
    private static Map<Character, Long> buildFrequencyMap(String text) {
        Map<Character, Long> frequencyMap = new HashMap<>();
        for (char character : text.toCharArray()) {
            frequencyMap.put(character, frequencyMap.getOrDefault(character, 0L) + 1);
        }
        return frequencyMap;
    }

    // 3. Find Characteristic root c for equation: sum(2^(-c*d_i)) = 1
    private static double findCharacteristicRoot(double[] diameters) {
        // Define function f(c) = sum(2^(-c*d_i)) - 1
        Function<Double, Double> f = (c) -> {
            double sum = 0.0;
            for (double d : diameters) {
                sum += Math.pow(2.0, -c * d);
            }
            return sum - 1.0;
        };

        // Find c, numberic approach with bisection
        double low = 0.0;
        double high = 10.0; // Assumption: c not too big
        // Check limits
        if (f.apply(TOLERANCE) < 0)
            return Double.NaN; // No positiv root possible

        // I randomly chose 100 iterations, probably good enough
        for (int i = 0; i < 100; i++) {
            double mid = low + (high - low) / 2.0;
            double f_mid = f.apply(mid);

            if (Math.abs(f_mid) < TOLERANCE) {
                return mid;
            } else if (f_mid > 0) { // Root on the right
                low = mid;
            } else { // Root on the left
                high = mid;
            }
        }
        // Return best value
        return low + (high - low) / 2.0;
    }

    // 4. Recursive method for code generation with pseudocode from Golin/Li as basis
    // (Fig.6 in paper)
    // U is the current prefix code
    private static void generateCodesGolinLi(List<Symbol> symbols, int l, int r,
            String U, Map<Character, String> codeTable,
            List<Perle> sortedDiameters, double cRoot) {

        // 0. Base case, only one symbol in [l, r]
        if (l == r) {
            Symbol sym = symbols.get(l);
            codeTable.put(sym.character, U);
            return;
        }
        if (l > r)
            return;

        // 1. Create initial bins Im*
        int k = sortedDiameters.size();
        // List of List with original indexes
        List<List<Integer>> initialBins = new ArrayList<>(k);
        for (int i = 0; i < k; i++) {
            initialBins.add(new ArrayList<>());
        }

        double P_start = symbols.get(l).cumulativeProbStart; // P_{l-1}
        double P_end = symbols.get(r).cumulativeProbEnd; // P_r
        // Total weight of this recursion step (w(v))
        double w = P_end - P_start;

        // No more weight to distribute
        if (w <= TOLERANCE)
            return;

        // L in paper
        double currentL = P_start;

        // Calculate edges Lm*, Rm* (relative limits inside of w) and assign symbols
        // Index m is Bin m+1 in Paper (0 to k-1 here)
        for (int m = 0; m < k; m++) {
            Perle diamInfo = sortedDiameters.get(m);
            double binWidth = w * Math.pow(2.0, -cRoot * diamInfo.cost);
            double currentR = currentL + binWidth;

            // Find symbols with middle point in [currentL, currentR)
            for (int i = l; i <= r; i++) {
                Symbol sym = symbols.get(i);
                // Centerpoint s_i = P_{i-1} + p_i / 2
                // Wahrscheinlichkeitsmittelpunkt, Heuristik
                double midPoint = sym.cumulativeProbStart + sym.probability / 2.0;

                // Check if fits to Bin m
                if (midPoint >= currentL - TOLERANCE && midPoint < currentR - TOLERANCE) {
                    // Add original index to Bin m
                    initialBins.get(m).add(i);
                }
            }
            // Update for next Bin
            currentL = currentR;
        }

        // 2. Very simplified version of left shift from paper
        // Just creates the final bin lists and skips empty bins
        List<List<Integer>> finalBins = new ArrayList<>();
        // Follow index of next available symbol
        int firstItemIndex = l;
        for (int m = 0; m < k; m++) {
            List<Integer> currentBinContent = initialBins.get(m);
            if (currentBinContent.isEmpty()) {
                // Bin is empty: Geht next availalbe symbol
                if (firstItemIndex <= r) {
                    // (In paper: find bin where the symbol was originally -> very complex, I did
                    // not manage to implement that correctly :(, so here just add next element to
                    // bin to fill it)
                    finalBins.add(Collections.singletonList(firstItemIndex));
                    firstItemIndex++;
                } else {
                    // No more elements to fill bin -> bin stays empty (ignored, very sad I know)
                }
            } else {
                List<Integer> effectiveBinContent = new ArrayList<>();
                for (int originalIndex : currentBinContent) {
                    // Just add if not already used by shifting
                    if (originalIndex >= firstItemIndex) {
                        effectiveBinContent.add(originalIndex);
                    }
                }
                // If bin empty because of skipping, but there are still elements, then just use
                // next
                if (effectiveBinContent.isEmpty() && firstItemIndex <= r) {
                    finalBins.add(Collections.singletonList(firstItemIndex));
                    firstItemIndex++;
                } else if (!effectiveBinContent.isEmpty()) {
                    finalBins.add(effectiveBinContent);
                    // Set index for next element after last of this bin
                    firstItemIndex = effectiveBinContent.get(effectiveBinContent.size() - 1) + 1;
                }
                // Otherwise bin stays empty
            }
        }
        // Check if all elements were assigned
        if (firstItemIndex <= r) {
            // Elements are still there, that are not inside a bin -> problem somewhere
            // Add to last non empty bin
            System.out.println(
                    "Irgendwas broken, eyyy ich kann nicht mehr, Elemente [" + firstItemIndex + " ... " + r + "] nicht zugeordnet");
            if (!finalBins.isEmpty()) {
                List<Integer> lastBin = finalBins.get(finalBins.size() - 1);
                for (int i = firstItemIndex; i <= r; i++) {
                    lastBin.add(i);
                }
            } else {
                System.out.println("FATAL");
                // Fill first bin, emergency lol
                List<Integer> onlyBin = new ArrayList<>();
                for (int i = l; i <= r; i++)
                    onlyBin.add(i);
                finalBins.add(onlyBin);
            }
        }

        // 3. Right shift
        // in paper: if all in bin 1 then move last element to bin 2
        // Here checking this
        if (finalBins.size() == 1 && finalBins.get(0).size() == (r - l + 1) && k > 1) {
            List<Integer> firstBin = finalBins.get(0);
            // Only makes sense if more than 1 element in bin
            if (firstBin.size() > 1) {
                // Remove last index
                int lastElementIndex = firstBin.remove(firstBin.size() - 1);
                List<Integer> secondBin = new ArrayList<>();
                secondBin.add(lastElementIndex);
                // Only add if space
                if (finalBins.size() < k) {
                    finalBins.add(secondBin);
                } else {
                    System.out.println("No space in second bin");
                    firstBin.add(lastElementIndex); // Revert
                }
            }
        }

        // 4. Recursion for every non empty final bin
        for (int m = 0; m < finalBins.size(); m++) {
            List<Integer> currentBinIndices = finalBins.get(m);
            if (!currentBinIndices.isEmpty()) {
                // First index in bin
                int bin_l = currentBinIndices.get(0);
                // Last index
                int bin_r = currentBinIndices.get(currentBinIndices.size() - 1);
                // Find color / index for this bin
                // in paper quite complex so here the m-th non empty bin gets the m-th cheapest
                // color
                int colorIndex = sortedDiameters.get(m).digit;

                // Recursive call
                generateCodesGolinLi(symbols, bin_l, bin_r, U + colorIndex, codeTable, sortedDiameters, cRoot);
            }
        }
    }

    // 5. Calculate total cost of encoded message
    private static long calculateTotalCost(Map<Character, String> codeTable, Map<Character, Long> frequencyMap,
            List<Integer> costs) {
        long totalCost = 0;

        // Iterate over each character
        for (Map.Entry<Character, Long> entry : frequencyMap.entrySet()) {
            char character = entry.getKey();
            long frequency = entry.getValue();
            String code = codeTable.get(character);

            long costOfCode = 0;
            for (char digitChar : code.toCharArray()) {
                int digit = Character.getNumericValue(digitChar);
                costOfCode += costs.get(digit);
            }
            totalCost += frequency * costOfCode;
        }
        return totalCost;
    }

    // 6. Print code table in a readable format
    private static void printCodeTable(Map<Character, String> codeTable, Map<Character, Long> frequencyMap,
            double[] diameters) {
        if (codeTable.isEmpty()) {
            System.out.println("{}");
            return;
        }
        List<Character> sortedKeys = new ArrayList<>(codeTable.keySet());
        // Sorting is optional
        // Sort by length of code
        sortedKeys.sort((a, b) -> Integer.compare(codeTable.get(a).length(), codeTable.get(b).length()));
        // Sort by frequency
        sortedKeys.sort((a, b) -> Long.compare(frequencyMap.get(b), frequencyMap.get(a)));

        System.out.println("{");
        for (Character character : sortedKeys) {
            // Handle special characters for printing
            String c;
            c = switch (character) {
                case ' ' -> "␣";
                case '"' -> c = "\"";
                default -> String.valueOf(character);
            };
            String code = codeTable.get(character);
            int length = 0;
            for (char digitChar : code.toCharArray()) {
                int digit = Character.getNumericValue(digitChar);
                length += diameters[digit];
            }

            System.out.println("  '" + c + "': " + code + " (Freq: "
                    + frequencyMap.get(character) + ", Länge: " + length + ")");
        }
        System.out.println("}");
    }
}