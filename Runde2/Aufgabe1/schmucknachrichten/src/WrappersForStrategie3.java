

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

// Represent Signature state representing depth i ;; (m; l1, ..., lC)
class Signature {
    final int m;
    final List<Integer> list; // Represent l1, l2, ..., lC
    // l1: amount of leaves on depth i + 1
    // l2: amount of leaves on depth i + 2 ...
    private final int C; // For convenience

    public Signature(int m, List<Integer> ls) {
        // Represents amount of leaves (final codewords), that are on depth <= i
        this.m = m;
        // Ensure immutability for use as key
        this.list = Collections.unmodifiableList(new ArrayList<>(ls));
        this.C = ls.size();
    }

    public int getC() {
        return C;
    }

    // 0-based index for lists (l1 is index 0)
    public int getL(int index) {
        return (index >= 0 && index < list.size()) ? list.get(index) : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Signature signature = (Signature) o;
        return m == signature.m && list.equals(signature.list);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m, list);
    }

    @Override
    public String toString() {
        return "(" + m + "; " + list.stream().map(String::valueOf).collect(Collectors.joining(", ")) + ")";
    }
}

// Result of frequency calculation
class FrequencyResult {
    final Map<Character, Long> counts;
    final List<Map.Entry<Character, Long>> sortedSymbols;

    public FrequencyResult(Map<Character, Long> counts, List<Map.Entry<Character, Long>> sortedSymbols) {
        this.counts = counts;
        this.sortedSymbols = sortedSymbols;
    }
}

// Result of characteristic vector calculation
class CharacteristicVectorResult {
    final int[] d; // d[0] is cost 1, d[C-1] to cost C
    final int C;

    public CharacteristicVectorResult(int[] d, int c) {
        this.d = d;
        this.C = c;
    }
}

// Hold info on predecessor to save how to get to a specific signature
class PredecessorInfo {
    final Signature previousSignature;
    final int q;

    public PredecessorInfo(Signature previousSignature, int q) {
        this.previousSignature = previousSignature;
        this.q = q;
    }
}

// Used in PriorityQueue for Dijkstra
// Wrapper to hold cost to get to a signature
class StateCost implements Comparable<StateCost> {
    // Cost needed to get to signature
    final long cost;
    final Signature signature;

    public StateCost(long cost, Signature signature) {
        this.cost = cost;
        this.signature = signature;
    }

    @Override
    public int compareTo(StateCost other) {
        return Long.compare(this.cost, other.cost);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        StateCost stateCost = (StateCost) o;
        return cost == stateCost.cost && Objects.equals(signature, stateCost.signature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cost, signature);
    }
}

// Used for sorting targets in code assignment
class TargetInfo implements Comparable<TargetInfo> {
    final int depth;
    final int symbolIndex; // Original index in sorted symbol list

    public TargetInfo(int depth, int symbolIndex) {
        this.depth = depth;
        this.symbolIndex = symbolIndex;
    }

    @Override
    public int compareTo(TargetInfo other) {
        int depthCompare = Integer.compare(this.depth, other.depth);
        if (depthCompare != 0) {
            return depthCompare;
        }
        return Integer.compare(this.symbolIndex, other.symbolIndex);
    }
}

// Hold final result
class OptimalCodingResult {
    final Map<Character, String> codeTable;
    final Map<Character, Long> frequencyMap;
    final long totalCost;
    final String errorMessage; // To indicate failures

    public OptimalCodingResult(Map<Character, String> codeTable, long totalCost, String error, Map<Character, Long> frequencyMap) {
        this.codeTable = codeTable;
        this.totalCost = totalCost;
        this.errorMessage = error;
        this.frequencyMap = frequencyMap;
    }

    public boolean hasError() {
        return errorMessage != null;
    }
}