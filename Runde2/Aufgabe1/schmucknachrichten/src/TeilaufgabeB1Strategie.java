import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

// Helper class to pair digits with their costs for sorting
class DigitCost implements Comparable<DigitCost> {
    int digit;
    int cost;

    public DigitCost(int digit, int cost) {
        this.digit = digit;
        this.cost = cost;
    }

    @Override
    public int compareTo(DigitCost other) {
        // Sort by cost ascending, then by digit ascending as tie-breaker
        int costCompare = Integer.compare(this.cost, other.cost);
        if (costCompare != 0) {
            return costCompare;
        }
        return Integer.compare(this.digit, other.digit);
    }
}

// Implements n-ary Huffman coding
public class TeilaufgabeB1Strategie {
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
        List<Integer> diameters = inputWrapper.diameters;

        if (numColors < 2) {
            System.out.println("Error: Die Anzahl der Farben muss mindestens 2 sein");
            return;
        }

        // 2. Calculate character frequency
        Map<Character, Long> frequencyMap = buildFrequencyMap(text);

        // Edgecase with text containing only one unique character
        if (frequencyMap.size() == 1) {
            Map<Character, String> codeTable = new HashMap<>();
            // Assign single digit code: here '0'
            codeTable.put(frequencyMap.keySet().iterator().next(), "0");

            System.out.println("\nCodetabelle:");
            printCodeTable(codeTable, frequencyMap, diameters);
            System.out.println("\nGesamtlänge der Botschaft (Anzahl Perlen): " + text.length());

        } else {
            // 3. Build n-ary Huffman tree
            Node root = buildHuffmanTree(frequencyMap, numColors);

            // 4. Generate Huffman codes
            Map<Character, String> codeTable = new HashMap<>();
            generateCodesCostAware(root, "", codeTable, diameters);

            // 5. Calculate total cost of encoded message
            long totalLength = calculateTotalCost(codeTable, frequencyMap, diameters);

            // 6. Output result
            System.out.println(
                    "\nCodetabelle:\n(Jede Ziffer steht für eine Perlenfarbe z.B. könnte '0' rot bedeuten und '1' blau)");
            printCodeTable(codeTable, frequencyMap, diameters);
            System.out.println("\nGesamtlänge der Botschaft " + totalLength + " (Anzahl in Perlen) bzw. "
                    + totalLength / 10.0 + "cm");
        }
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

    // 3. Build n-ary Huffman tree and returns root Node
    private static Node buildHuffmanTree(Map<Character, Long> frequencyMap, int n) {
        // PriorityQueue for nodes (sorted by frequency, lower frequency first)
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>();

        // Create leaf nodes (nodes with characters) and add to priority queue
        for (Map.Entry<Character, Long> entry : frequencyMap.entrySet()) {
            priorityQueue.add(new Node(entry.getKey(), entry.getValue()));
        }

        int numSymbols = priorityQueue.size(); // Number of unique symbols (leaf nodes)

        // Determine how many nodes should be merged in the first step
        int r;
        if (numSymbols <= 1) { // Handle edge case: if 0 or 1 symbol, no merging needed
            r = numSymbols;
        } else {
            // Formula: find smallest valid r so that:
            // After merging r nodes -> remaining nodes + 1 new node
            // -> total node count allows full n-ary merges
            int remainder = (numSymbols - 2) % (n - 1);
            r = remainder + 2; // Ensures 2 <= r <= n
        }

        // Build Huffman tree
        while (priorityQueue.size() > 1) {
            int nodesToMerge = (priorityQueue.size() == numSymbols) ? r : n; // Use r only for first merge if needed
            nodesToMerge = Math.min(nodesToMerge, priorityQueue.size()); // Cannot merge more nodes than available

            if (nodesToMerge < 2) {
                // Should not happen if n > 1
                System.out.println("irgendwas ist broken");
                break;
            }

            List<Node> children = new ArrayList<>();
            long mergedFrequency = 0;

            // Extract nodes (num of nodesToMerge) with lowest frequencies
            for (int i = 0; i < nodesToMerge; i++) {
                Node node = priorityQueue.poll();
                children.add(node);
                mergedFrequency += node.frequency;
            }

            // Create new internal node with new values
            Node internalNode = new Node(children, mergedFrequency);

            // Add new internal node back to priority queue
            priorityQueue.add(internalNode);
        }

        // Last node in queue is root of Huffman tree
        return priorityQueue.poll();
    }

    // 4. Generate Huffman codes with cost-aware digit assignment
    private static void generateCodesCostAware(Node node, String currentCode, Map<Character, String> codeTable,
            List<Integer> costs) {
        // Base case
        if (node == null) {
            return;
        }

        // If it is a leaf, it represents a character, then assign a code
        if (node.isLeaf()) {
            if (node.character != null) {
                codeTable.put(node.character, currentCode.isEmpty() ? "0" : currentCode);
            }
            // Leaf is reached, stop recursion
            return;
        }

        List<Node> children = node.children;

        // 1. Sort children by frequency - Descending (made copy to not modify the main
        // list, not sure if needed)
        List<Node> sortedChildren = new ArrayList<>(children);
        sortedChildren.sort(Comparator.comparingLong(Node::getFrequency).reversed());

        // 2. Prepare digits sorted by cost - Ascending
        // DigitCost is a wrapper to map each digit with its corresponding cost
        List<DigitCost> digitCosts = new ArrayList<>();

        for (int i = 0; i < costs.size(); i++) {
            digitCosts.add(new DigitCost(i, costs.get(i)));
        } 
        // Sort by cost ascending, for this input not needed as digits are already sorted
        Collections.sort(digitCosts);

        // 3. Assign cheapest digits to most frequent children
        for (int i = 0; i < sortedChildren.size(); i++) {
            Node child = sortedChildren.get(i);
            int assignedDigit = digitCosts.get(i).digit; // i-th cheapest digit
            generateCodesCostAware(child, currentCode + assignedDigit, codeTable, costs);
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

    // Print code table in a readable format
    private static void printCodeTable(Map<Character, String> codeTable, Map<Character, Long> frequencyMap,
            List<Integer> diameters) {
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
                length += diameters.get(digit);
            }

            System.out.println("  '" + c + "': " + code + " (Freq: "
                    + frequencyMap.get(character) + ", Länge: " + length + ")");
        }
        System.out.println("}");
    }
}