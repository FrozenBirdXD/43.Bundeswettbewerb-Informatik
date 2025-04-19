import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

// Node for Huffman tree
class Node implements Comparable<Node> {
    Character character; // null for internal nodes
    long frequency;
    List<Node> children;
    boolean isLeaf;

    // For leaf nodes
    public Node(Character character, long frequency) {
        this.character = character;
        this.frequency = frequency;
        this.children = new ArrayList<>();
        this.isLeaf = true;
    }

    // For internal nodes
    public Node(List<Node> children, long frequency) {
        this.character = null;
        this.frequency = frequency;
        this.children = children;
        this.isLeaf = false;
    }

    @Override
    public int compareTo(Node other) {
        // Compare based on frequency, lower frequency first
        return Long.compare(this.frequency, other.frequency);
    }

    public boolean isLeaf() {
        return isLeaf;
    }
}

// Implements n-ary Huffman coding
public class TeilaufgabeA {
    public static void main(String[] args) {
        String input;
        try {
            // Read input from file
            input = Files.readString(Path.of("beispielaufgaben/a/schmuck01.txt"));
        } catch (IOException e) {
            System.out.println("Error: InputFile not found");
            return;
        }
        InputWrapper inputWrapper = parseInput(input);

        // Usable input from file
        int numColors = inputWrapper.numColors;
        String text = inputWrapper.text;

        if (numColors < 2) {
            System.out.println("Error: Die Anzahl der Farben muss mindestens 2 sein");
            return;
        }

        // 1. Calculate character frequency
        Map<Character, Long> frequencyMap = buildFrequencyMap(text);

        // Edgecase with text containing only one unique character
        if (frequencyMap.size() == 1) {
            Map<Character, String> codeTable = new HashMap<>();
            // Assign single digit code: here '0'
            codeTable.put(frequencyMap.keySet().iterator().next(), "0");

            System.out.println("\nOptimierte Codetabelle:");
            printCodeTable(codeTable, frequencyMap);
            System.out.println("\nGesamtlänge der Botschaft (Anzahl Perlen): " + text.length());

        } else {
            // 2. Build n-ary Huffman tree
            Node root = buildHuffmanTree(frequencyMap, numColors);

            // 3. Generate Huffman codes
            Map<Character, String> codeTable = new HashMap<>();
            generateCodes(root, "", codeTable, numColors);

            // 4. Calculate total length of encoded message
            long totalLength = calculateTotalLength(text, codeTable);

            // 5. Output result
            System.out.println(
                    "\nOptimierte Codetabelle: (Jede Ziffer steht für eine Perlenfarbe z.B. könnte '0' rot bedeuten und '1' blau)");
            printCodeTable(codeTable, frequencyMap);
            System.out.println("\nGesamtlänge der Botschaft (Anzahl Perlen): " + totalLength);
        }
    }

    private static InputWrapper parseInput(String input) {
        // Save every line in array of Strings
        String[] lines = input.split("\n");
        return new InputWrapper(Integer.parseInt(lines[0].trim()), lines[2]);
    }

    // Calculate character frequency from input text
    private static Map<Character, Long> buildFrequencyMap(String text) {
        Map<Character, Long> frequencyMap = new HashMap<>();
        for (char character : text.toCharArray()) {
            frequencyMap.put(character, frequencyMap.getOrDefault(character, 0L) + 1);
        }
        return frequencyMap;
    }

    // Build n-ary Huffman tree (allows not just 2 children per node, but n
    // children)
    private static Node buildHuffmanTree(Map<Character, Long> frequencyMap, int n) {
        // PriorityQueue for nodes (sorted by frequency, lower frequency first)
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>();

        // Create leaf nodes (nodes with characters) and add to priority queue
        for (Map.Entry<Character, Long> entry : frequencyMap.entrySet()) {
            priorityQueue.add(new Node(entry.getKey(), entry.getValue()));
        }

        int numSymbols = priorityQueue.size(); // Number of unique symbols (leaf nodes)

        // Determine how many nodes should be merged in the first step
        // Need to build a tree so that the total number of nodes
        // N satisfies: (N - 1) % (n - 1) == 0
        // (https://compression.ru/download/articles/huff/huffman_1952_minimum-redundancy-codes.pdf)
        int r;
        if (numSymbols <= 1) { // Handle edge case: if 0 or 1 symbol, no merging needed
            r = numSymbols;
        } else {
            // Formula: find smallest valid r so that:
            // After merging r nodes → remaining nodes + 1 new node
            // → total node count allows full n-ary merges
            int remainder = (numSymbols - 2) % (n - 1);
            r = remainder + 2; // Ensures 2 <= r <= n
        }

        // Build Huffman tree
        while (priorityQueue.size() > 1) {
            int nodesToMerge = (priorityQueue.size() == numSymbols) ? r : n; // Use r only for first merge if needed
            nodesToMerge = Math.min(nodesToMerge, priorityQueue.size()); // Cannot merge more nodes than available

            if (nodesToMerge < 2) {
                // Should not happen if n > 1
                System.out.println("Hää, irgendwas ist broken");
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

    // Generate Huffman codes by traversing tree with recursion
    private static void generateCodes(Node node, String currentCode, Map<Character, String> codeTable, int numColors) {
        // Base case
        if (node == null) {
            return;
        }

        // If it is a leaf, it represents a character, then assign a code
        if (node.isLeaf()) {
            if (node.character != null) {
                codeTable.put(node.character, currentCode);
            }
            // Leaf is reached, no need for more recursion
            return;
        }

        // Assign codes 0 until k-1 to children
        for (int i = 0; i < node.children.size(); i++) {
            generateCodes(node.children.get(i), currentCode + i, codeTable, numColors);
        }
    }

    // Calculate total length of encoded message
    private static long calculateTotalLength(String text, Map<Character, String> codeTable) {
        long totalLength = 0;
        // Interate over every character in text to encode
        for (char character : text.toCharArray()) {
            String code = codeTable.get(character);
            if (code != null) {
                totalLength += code.length();
            }
        }
        return totalLength;
    }

    // Print code table in a readable format
    private static void printCodeTable(Map<Character, String> codeTable, Map<Character, Long> frequencyMap) {
        if (codeTable.isEmpty()) {
            System.out.println("{}");
            return;
        }
        List<Character> sortedKeys = new ArrayList<>(codeTable.keySet());
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
                case '"' -> c = "\\\"";
                default -> String.valueOf(character);
            };
            System.out.println("  '" + c + "': " + codeTable.get(character) + " (Freq: "
                    + frequencyMap.get(character) + ")");
        }
        System.out.println("}");
    }
}