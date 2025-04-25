import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

// Implements Golin/Rote algorithm for optimal prefix-free codes with unqual letter cost
public class TeilaufgabeB3Strategie {

    public static void main(String[] args) {
        String input;
        try {
            // 1. Read input from file
            input = Files.readString(Path.of("beispielaufgaben/b/schmuck8.txt"));
        } catch (IOException e) {
            System.out.println("Error: InputFile not found");
            return;
        }
        InputWrapper inputWrapper = parseInput(input);

        // Usable input from file
        String text = inputWrapper.text;
        List<Integer> diameters = inputWrapper.diameters;

        // Start timing
        long startTime = System.nanoTime();
        // 2. Solve problem
        OptimalCodingResult result = solveOptimalCode(text, diameters);
        // End Timing
        long endTime = System.nanoTime();
        System.out.println("------------------------------");
        long durationMs = (endTime - startTime) / 1000000;
        System.out.println("Execution time: " + durationMs + " ms");
        System.out.println("------------------------------");

        // 3. Print results
        if (result.hasError()) {
            System.out.println("Could not find optimal code: " + result.errorMessage);
        } else {
            printCodeTable(result.codeTable, result.frequencyMap, diameters);
            System.out.println("\nGesamtlänge der Botschaft " + result.totalCost + " (Anzahl in Perlen) bzw. "
                    + result.totalCost / 10.0 + "cm\n");
            // 4. Encode
            String encodedText = encodeText(text, result.codeTable);
            System.out.println("Kodierte Nachricht: " + encodedText);
        }
    }

    // Main solver method
    private static OptimalCodingResult solveOptimalCode(String text, List<Integer> alphabetCosts) {
        // 1. Preprocessing step
        System.out.println("1. Preprocessing");
        // 1a. Calculate symbol frequencies
        FrequencyResult freqResult = calculateFrequencies(text);
        if (freqResult.sortedSymbols.isEmpty()) {
            return new OptimalCodingResult(Collections.emptyMap(), 0, null, null);
        }

        // Num of unique symbols k
        int k = freqResult.sortedSymbols.size();
        // Frequency counts
        long[] freqCounts = freqResult.sortedSymbols.stream().mapToLong(Map.Entry::getValue).toArray();

        // 1b. Precompute cumulative frequency counts (suffix sums)
        // Needed for edge cost calculation in O(1)
        long[] cumFreq = new long[k + 1];
        // cumFreq[i] = sum of frequencies from index i to n-1 (i.e., P_{i+1} + ... +
        // P_n)
        for (int i = k - 1; i >= 0; i--) {
            cumFreq[i] = cumFreq[i + 1] + freqCounts[i];
        }

        // 1c. Calculate characteristic vector 'd' and maximum cost 'C'
        CharacteristicVectorResult charVecResult = calculateCharacteristicVector(alphabetCosts);
        int[] d = charVecResult.d;
        int C = charVecResult.C;
        if (C == 0) {
            System.err.println("Error: No valid alphabet costs provided (C=0).");
            return new OptimalCodingResult(null, Long.MAX_VALUE, "Invalid alphabet costs", null);
        }
        int r = alphabetCosts.size();

        // 2. Build Zustandsgraph
        // Explore signatures to find min cost path
        // Simulate building optimal code tree top-down

        // 2a. Init DP table (OPT) and Predecessor table (Pred)
        // Saves minimal cost to get to a signature from S0
        Map<Signature, Long> opt = new HashMap<>();
        // Saves the current best way to get to a signature
        Map<Signature, PredecessorInfo> pred = new HashMap<>();

        // Calculate initial signature S0
        // List full of 0
        List<Integer> initialLsRaw = new ArrayList<>(Collections.nCopies(C, 0));
        for (int i = 0; i < d.length; ++i) {
            // Set initial to values of characteristic vector
            initialLsRaw.set(i, d[i]); // d has length C
        }
        // Signature of "root"
        Signature initialSigRaw = new Signature(0, initialLsRaw);
        Signature S0 = reduceSignature(initialSigRaw, k);
        opt.put(S0, 0L);

        // Target sig: all Symbols in the tree have a codeword
        Signature finalState = new Signature(k, Collections.nCopies(C, 0));

        // Priority Queue for Dijkstra like (process states with lowest cost first)
        PriorityQueue<StateCost> pq = new PriorityQueue<>();
        pq.add(new StateCost(0L, S0));

        System.out.println("   k = " + k + ", C = " + C + ", r=" + r + ", S0 = " + S0);
        System.out.println("2a. Running Dynamic Programming (Dijkstra-like)");
        long processedCount = 0;
        // Store the actual final signature, to begin null, check after dp, if still
        // null
        Signature actualFinalSig = null;
        // Store the final cost to get from S0 to final State
        long finalCostFound = Long.MAX_VALUE;

        // 2b. Main DP loop
        while (!pq.isEmpty()) {
            // Get state with smallest cost from queue
            StateCost current = pq.poll();
            long currentCost = current.cost;
            Signature currentSig = current.signature;
            processedCount++;

            // If found a shorter path to a signature already, skip
            if (currentCost > opt.getOrDefault(currentSig, Long.MAX_VALUE)) {
                continue;
            }

            // Check if reached target state
            if (currentSig.equals(finalState)) {
                if (currentCost < finalCostFound) {
                    finalCostFound = currentCost;
                    actualFinalSig = currentSig;
                    System.out.println("   Reached target finalState = " + currentSig + " with cost " + currentCost);
                    break;
                }
            }

            // Small optimization
            if (actualFinalSig != null && currentCost >= finalCostFound)
                continue;

            // Logging
            if (processedCount % 30000 == 0) {
                System.out.println("   Processed " + processedCount + " states (pop ops), PQ size " + pq.size());
            }

            // --- Generate next states
            // Get data from current signature
            int m = currentSig.m;
            int l1 = currentSig.getL(0);

            // Calculate cost added for expansion step (edge weight in state graph)
            long edgeCost = (m < k) ? cumFreq[m] : 0;
            long newBaseCost = currentCost + edgeCost;

            // Iterate through possible expansions q
            for (int q = 0; q <= l1; q++) {
                // Get signature for an expansion
                Signature nextSigRaw = getNextSigRaw(currentSig, q, d, C);
                Signature nextSignature = reduceSignature(nextSigRaw, k);

                // Update if a better path is found
                if (newBaseCost < opt.getOrDefault(nextSignature, Long.MAX_VALUE)) {
                    // Better cost was found to get to a signature
                    opt.put(nextSignature, newBaseCost);
                    // Store how to get to that signature
                    pred.put(nextSignature, new PredecessorInfo(currentSig, q));
                    // Add a new state to explore to pq
                    pq.add(new StateCost(newBaseCost, nextSignature));
                }
            }
        }

        System.out.println("2b. DP Finished. Processed " + processedCount + " states");
        System.out.println("   Total states in OPT table: " + opt.size());

        // 3. Get result and check if finalState was reached
        if (actualFinalSig == null) {
            // Check if anystate (n, 0, ..., 0) was reached if finalState was not hit
            // directly
            boolean foundEquivalent = false;
            // Iterate over all signatures in opt and check if state was reached that is
            // equivalent
            for (Map.Entry<Signature, Long> entry : opt.entrySet()) {
                Signature sig = entry.getKey();
                // Check if m == n and all l_i == 0
                if (sig.m == k && sig.list.stream().allMatch(l -> l == 0)) {
                    if (entry.getValue() < finalCostFound) {
                        finalCostFound = entry.getValue();
                        actualFinalSig = sig;
                        foundEquivalent = true;
                    }
                }
            }
            if (foundEquivalent) {
                System.out.println(
                        "   Reached equivalent final state " + actualFinalSig + " with cost " + finalCostFound);
            } else {
                System.out.println("Error: Target signature finalState not reached");
                return new OptimalCodingResult(null, Long.MAX_VALUE, "Target state not reachable", null);
            }
        }

        // Optimal cost found by DP
        long minTotalCost = finalCostFound;
        System.out.println("3. Minimum Total Cost (Sum Freq*Depth): " + minTotalCost);

        // 4. Reconstruct code
        System.out.println("4. Reconstructing Code");
        // Reconstruct path and determine leaf depths
        // Simulates the optimal tree construction process based on pred map
        Map<Integer, Long> leafDepthsCounts = getLeafDepthsFromPath(actualFinalSig, pred, S0, k, d, alphabetCosts);

        // Error
        if (leafDepthsCounts == null) {
            System.out.println("  Error:( Failed to reconstruct leaf depths");
            return new OptimalCodingResult(null, minTotalCost, "Leaf depth reconstruction failed", null);
        }

        // 5b. Create actual code table using determined dpeths
        Map<Character, String> codeTable = buildCodeFromDepths(freqResult.sortedSymbols, leafDepthsCounts,
                alphabetCosts);

        if (codeTable == null) {
            System.err.println("   Failed to assign codewords from depths.");
            return new OptimalCodingResult(null, minTotalCost, "Codeword assignment failed", null);
        }

        return new OptimalCodingResult(codeTable, minTotalCost, null, freqResult.counts);
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

    // 2. Calculate frequencies from text
    private static FrequencyResult calculateFrequencies(String text) {
        Map<Character, Long> frequencyMap = new HashMap<>();
        for (char character : text.toCharArray()) {
            frequencyMap.put(character, frequencyMap.getOrDefault(character, 0L) + 1);
        }

        // Sort symbols by frequency (descending)
        List<Map.Entry<Character, Long>> sortedSymbols = new ArrayList<>(frequencyMap.entrySet());
        sortedSymbols.sort((e1, e2) -> {
            int freqCompare = Long.compare(e2.getValue(), e1.getValue()); // Descending frequency
            if (freqCompare != 0) {
                return freqCompare;
            }
            return Character.compare(e1.getKey(), e2.getKey()); // Ascending character tiebreaker
        });

        return new FrequencyResult(frequencyMap, sortedSymbols);
    }

    // Calculate characteristic vector d and max cost C
    private static CharacteristicVectorResult calculateCharacteristicVector(List<Integer> costs) {
        if (costs == null || costs.isEmpty()) {
            return new CharacteristicVectorResult(new int[0], 0);
        }
        // Find max C
        int maxC = 0;
        for (int cost : costs) {
            if (cost > 0) {
                maxC = Math.max(maxC, cost);
            }
        }
        if (maxC == 0) {
            return new CharacteristicVectorResult(new int[0], 0);
        }

        // Count occurrences of every positive cost up to maxC
        int[] dCounts = new int[maxC + 1]; // dCounts[i] store count for cost i
        for (int cost : costs) {
            if (cost > 0) {
                dCounts[cost]++;
            }
        }
        // Create final 'd' array which starts from cost 1
        int[] dResult = Arrays.copyOfRange(dCounts, 1, maxC + 1);
        return new CharacteristicVectorResult(dResult, maxC);
    }

    // Apply reduce operation to signature
    // Ensures that signature represents at most n leaves
    private static Signature reduceSignature(Signature signature, int n) {
        int m = signature.m;
        int C = signature.getC();

        // Ensure m is not exceeding n to begin with
        int mPrime = Math.min(m, n);
        List<Integer> lsPrime = new ArrayList<>(Collections.nCopies(C, 0)); // Init with zeros
        long currentSum = mPrime; // Had overflow problem with int

        // Iterate through levels l_1, l_2 ..., l_C
        for (int j = 0; j < C; j++) { // j maps to l_{j+1}
            int l_j_plus_1 = signature.getL(j); // ls.get(j)

            // Ensure not exceeding n leaves
            long remainingCapacity = n - currentSum;
            // Get new count
            int l_prime_j_plus_1 = (int) Math.max(0, Math.min(l_j_plus_1, remainingCapacity));

            lsPrime.set(j, l_prime_j_plus_1);
            currentSum += l_prime_j_plus_1;
        }
        return new Signature(mPrime, lsPrime);
    }

    // Calculate next raw signature before reduction
    private static Signature getNextSigRaw(Signature signature, int q, int[] d, int C) {
        int m = signature.m;
        int dLen = d.length; // d represent costs 1 to C

        int l1 = signature.getL(0); // l1 is at index 0
        int mPrime = m + l1 - q;
        List<Integer> lsPrime = new ArrayList<>(Collections.nCopies(C, 0)); // Initit with zeros

        // 1. Shift l's
        for (int k = 0; k < C - 1; k++) { // k from 0 to C-2 relates to l'_1 to l'_{C-1}
            lsPrime.set(k, signature.getL(k + 1));
        }

        // 2. Add new leaves by expanding q nodes
        // Based on characteristic vector
        for (int k = 0; k < C; k++) { // k from 0 to C-1 relates to d_1 to d_C
            if (k < dLen) { // Check bounds for d (d has length C)
                int currentVal = lsPrime.get(k);
                lsPrime.set(k, currentVal + (q * d[k]));
            }
        }
        return new Signature(mPrime, lsPrime);
    }

    // Calculate cost of one codeword
    private static long calculateCost(String codewordStr, List<Integer> costs) {
        long cost = 0;
        int r = costs.size();
        for (int i = 0; i < codewordStr.length(); i++) {
            char digitChar = codewordStr.charAt(i);
            int digit = Character.getNumericValue(digitChar);
            if (digit >= 0 && digit < r) {
                cost += costs.get(digit);
            } else {
                // Invalid
                System.out.println(
                        "Warning: Invalid digit '" + digitChar + "' in codeword '" + codewordStr
                                + "', at this point might aswell just quit");
                return Long.MAX_VALUE;
            }
        }
        return cost;
    }

    // ------------- Reconstruction of tree and Code Assignment

    // Step 4
    // Trace back path from finalSig to S0 and simulate tree construction to find
    // leaf depths
    // Then simulates tree construction process (forward) along this optimal path to
    // find exact depth at wich each of the n leaves is finalized
    private static Map<Integer, Long> getLeafDepthsFromPath(
            Signature finalSig, Map<Signature, PredecessorInfo> pred, Signature S0,
            int n, int[] characVector, List<Integer> alphabetCosts) {
        System.out.println("   Reconstructing path");
        List<PredecessorInfo> path = new ArrayList<>();
        Signature currentSignature = finalSig;

        // 4a. Backtrack from final state to S0 with pred map
        while (!currentSignature.equals(S0)) {
            PredecessorInfo info = pred.get(currentSignature);
            if (info == null) {
                System.out.println(
                        "   Error: Cannot backtrack from signature " + currentSignature + "; failed");
                return null;
            }
            // Add info about step to current Sign
            path.add(info);
            currentSignature = info.previousSignature;
        }
        // Path currently in reverse order (final -> S0), reverse here for forward
        // simulation
        Collections.reverse(path);

        if (path.isEmpty() && S0.equals(finalSig)) {
            // Handle n=0 or n=1 case where path might be empty
            return (S0.m == n) ? new HashMap<>() : null;
        }

        // 4b. --- Simulate forward construction
        // Store final result
        Map<Integer, Long> leavesFinishedAtDepth = new HashMap<>();
        // Track currently open end (potentiall leaves) and their depths during
        // simulation
        Map<Integer, Long> activeLeaves = new HashMap<>();

        // Init active leaves based on initial state S0 (after reduction)
        // Need to know wich children root has according to S0
        Signature unreducedS0 = new Signature(0, Arrays.stream(characVector).boxed().collect(Collectors.toList()));
        if (S0.equals(unreducedS0)) {
            for (int cost : alphabetCosts) {
                if (cost > 0)
                    activeLeaves.put(cost, activeLeaves.getOrDefault(cost, 0L) + 1);
            }
        } else {
            Map<Integer, Long> tempActive = new HashMap<>();
            for (int cost : alphabetCosts) {
                if (cost > 0)
                    tempActive.put(cost, tempActive.getOrDefault(cost, 0L) + 1);
            }
            long currentTotal = 0;
            List<Integer> sortedDepths = new ArrayList<>(tempActive.keySet());
            Collections.sort(sortedDepths);
            for (int depth : sortedDepths) {
                long countAtDepth = tempActive.get(depth);
                long canKeep = n - currentTotal;
                long keep = Math.min(countAtDepth, canKeep);
                if (keep > 0) {
                    activeLeaves.put(depth, keep);
                    currentTotal += keep;
                }
                if (currentTotal >= n)
                    break;
            }
        }

        // Simulate forward along path
        int currentLevel = 0; // Track implicit level of signatures
        for (PredecessorInfo info : path) {
            // Optimal expansion for this step
            int q = info.q;
            int simDepth = currentLevel + 1;

            long numActiveAtSimDepth = activeLeaves.getOrDefault(simDepth, 0L);

            // Determine how many nodes at simDepth expand or finalize
            long nodesToExpand = Math.min(q, numActiveAtSimDepth);
            long nodesToFinalize = numActiveAtSimDepth - nodesToExpand;

            // Update active leaves
            activeLeaves.remove(simDepth);

            // Add children of expanded nodes to active leaves
            for (int i = 0; i < nodesToExpand; i++) {
                for (int cost : alphabetCosts) {
                    if (cost > 0)
                        activeLeaves.put(simDepth + cost, activeLeaves.getOrDefault(simDepth + cost, 0L) + 1);
                }
            }

            // Simulate Reduction
            long currentFinishedCount = leavesFinishedAtDepth.values().stream().mapToLong(Long::longValue).sum();
            long currentActiveCount = activeLeaves.values().stream().mapToLong(Long::longValue).sum();
            long potentialTotal = currentFinishedCount + currentActiveCount + nodesToFinalize;
            // How man leaves to remove to stay in n
            long leavesToRemove = Math.max(0, potentialTotal - n);

            // Remove deepest leaves first
            long removedCount = 0;
            if (leavesToRemove > 0) {
                List<Integer> sortedActiveDepths = new ArrayList<>(activeLeaves.keySet());
                sortedActiveDepths.sort(Collections.reverseOrder()); // Deepest first, -> takes most of runtime when
                                                                     // timecomplexity is viewed
                for (int depth : sortedActiveDepths) {
                    // Removed enough
                    if (removedCount >= leavesToRemove)
                        break; //
                    long canRemoveAtDepth = activeLeaves.get(depth);
                    long removeNow = Math.min(leavesToRemove - removedCount, canRemoveAtDepth);
                    activeLeaves.put(depth, activeLeaves.get(depth) - removeNow);
                    if (activeLeaves.get(depth) == 0) {
                        activeLeaves.remove(depth);
                    }
                    removedCount += removeNow;
                }
            }

            long nodesFinalizedThisStep = Math.max(0, nodesToFinalize - (leavesToRemove - removedCount));

            if (nodesFinalizedThisStep > 0) {
                leavesFinishedAtDepth.put(simDepth,
                        leavesFinishedAtDepth.getOrDefault(simDepth, 0L) + nodesFinalizedThisStep);
            }
            // Move to next level
            currentLevel++;
        }

        // After loop any remaining active leaves are finalized leaves
        for (Map.Entry<Integer, Long> entry : activeLeaves.entrySet()) {
            leavesFinishedAtDepth.put(entry.getKey(),
                    leavesFinishedAtDepth.getOrDefault(entry.getKey(), 0L) + entry.getValue());
        }

        // Verify
        long totalLeavesReconstructed = leavesFinishedAtDepth.values().stream().mapToLong(Long::longValue).sum();
        if (totalLeavesReconstructed != n) {
            System.err.println(
                    "   Warning: Reconstructed " + totalLeavesReconstructed + " leaves, but expected " + n);
        }

        return leavesFinishedAtDepth;
    }

    // 5. Construct prefix free code
    public static Map<Character, String> buildCodeFromDepths(
            List<Map.Entry<Character, Long>> symbolFreqs,
            Map<Integer, Long> depthsCounts,
            List<Integer> costs) {
        if (symbolFreqs == null || symbolFreqs.isEmpty() || depthsCounts == null || depthsCounts.isEmpty()) {
            return Collections.emptyMap();
        }

        int n = symbolFreqs.size();
        int r = costs.size();
        List<Character> symbols = symbolFreqs.stream().map(Map.Entry::getKey).collect(Collectors.toList());

        // Create and sort targets
        // Every entry represents one required leaf: tragetDepth, symbolIndex
        List<TargetInfo> targets = new ArrayList<>();
        int symbolIdx = 0;
        int maxTargetDepth = 0;
        List<Integer> sortedDepths = new ArrayList<>(depthsCounts.keySet());
        Collections.sort(sortedDepths);

        for (int depth : sortedDepths) {
            // How many leaves are needed at this depth
            long count = depthsCounts.get(depth);
            maxTargetDepth = Math.max(maxTargetDepth, depth);
            for (int i = 0; i < count; i++) {
                if (symbolIdx < n) {
                    targets.add(new TargetInfo(depth, symbolIdx));
                    symbolIdx++;
                } else {
                    System.out.println("Error: More depths specified than symbols");
                    break;
                }
            }
            if (symbolIdx >= n)
                break;
        }

        // Sort by depth, then symbol index
        Collections.sort(targets);

        // Generate and assing codes greedily
        Map<Character, String> codeMap = new HashMap<>();
        List<String> assignedCodesList = new ArrayList<>(); // For prefix checks

        // Use queue for BFS like generation of candiate codewords
        Deque<String> generatorQueue = new ArrayDeque<>();
        // Start with single digit codes (children of root)
        for (int i = 0; i < r; i++) {
            generatorQueue.offerLast(String.valueOf(i));
        }
        // Pool to store codewords that have wrong cost for current target but match
        // maybe later
        Map<Integer, Deque<String>> candidatePool = new HashMap<>();

        // Maximum generation limit to prevent infinite loops
        int minCost = Integer.MAX_VALUE;
        for (int cost : costs) {
            if (cost > 0) {
                minCost = Math.min(minCost, cost);
            }
        }
        if (minCost == Integer.MAX_VALUE) {
            minCost = 1; // Avoid durch null teilen
        }

        // Estimate max length needed plus 2 buffer
        int worstCaseLen = (maxTargetDepth > 0) ? (maxTargetDepth / minCost + 2) : 1;
        // Estimate of max nodes in full tree of that length and set cap
        long MaxGeneratedNodes = (long) Math.min(10000000, Math.max(n * 10L, (long) Math.pow(r, worstCaseLen + 1)));
        long generatedCount = 0;

        // Iterate through targets and assing next available code
        for (TargetInfo target : targets) {
            int targetDepth = target.depth;
            int targetSymbolIndex = target.symbolIndex;
            char targetSymbol = symbols.get(targetSymbolIndex);
            boolean foundCodeForTarget = false;

            // 1. Check candidate pool
            Deque<String> poolDeque = candidatePool.get(targetDepth);
            if (poolDeque != null && !poolDeque.isEmpty()) {
                // Temp store codes that fail prefix check
                Deque<String> skipped = new ArrayDeque<>();
                while (!poolDeque.isEmpty()) {
                    String candidate = poolDeque.pollFirst();
                    boolean isPrefixFree = true;
                    // Check if it acually is prefix free with already assigned codes
                    for (String assigned : assignedCodesList) {
                        // Not prefix free
                        if (candidate.startsWith(assigned) || assigned.startsWith(candidate)) {
                            isPrefixFree = false;
                            break;
                        }
                    }
                    if (isPrefixFree) {
                        // Found usable code
                        codeMap.put(targetSymbol, candidate);
                        assignedCodesList.add(candidate);
                        foundCodeForTarget = true;
                        break;
                    } else {
                        // If fails prefix, but aside for a little
                        skipped.offerLast(candidate);
                    }
                }
                // Put skipped back into pool (maybe used later)
                while (!skipped.isEmpty()) {
                    poolDeque.offerLast(skipped.pollFirst());
                }
            }

            // 2. Generate new codes if needed
            // If no fitting code was in pool, create new candidates
            if (!foundCodeForTarget) {
                while (generatedCount < MaxGeneratedNodes) {
                    String codeword;
                    // Problem
                    if (generatorQueue.isEmpty()) {
                        System.out.println("Error: Code generator queue empty????");
                        return null;
                    }
                    // Dequeue next shortest prefix
                    String current = generatorQueue.pollFirst();
                    for (int i = 0; i < r; i++) {
                        generatorQueue.offerLast(current + i);
                    }
                    // Use dequeued item as current candidate
                    codeword = current;

                    generatedCount++;
                    long currentCost = calculateCost(codeword, costs);
                    // Invalid cost
                    if (currentCost == Long.MAX_VALUE) {
                        continue;
                    }
                    // Check if cost matches target depth
                    if (currentCost == targetDepth) {
                        boolean isPrefixFree = true;
                        // Check if prefix free
                        for (String assigned : assignedCodesList) {
                            if (codeword.startsWith(assigned) || assigned.startsWith(codeword)) {
                                isPrefixFree = false;
                                break;
                            }
                        }
                        if (isPrefixFree) {
                            // Found code
                            codeMap.put(targetSymbol, codeword);
                            assignedCodesList.add(codeword);
                            foundCodeForTarget = true;
                            break;
                        } else {
                            // Correct cost but not prefix free, maybe used later -> put in pool
                            candidatePool.computeIfAbsent((int) currentCost, k -> new ArrayDeque<>())
                                    .offerLast(codeword);
                        }
                    } else {
                        candidatePool.computeIfAbsent((int) currentCost, k -> new ArrayDeque<>()).offerLast(codeword);
                    }
                }

                if (!foundCodeForTarget && generatedCount >= MaxGeneratedNodes) {
                    System.out.println(
                            "Error: Max generation limit (" + MaxGeneratedNodes + ") reached for depth " + targetDepth);
                    return null;
                }
            }

            if (!foundCodeForTarget) {
                System.out.println(
                        "FATAL: Failed to find code for symbol " + targetSymbol + "' depth " + targetDepth);
                return null;
            }

        }

        // Final check if all symbols received codes
        if (codeMap.size() == n) {
            return codeMap;
        } else {
            System.out.println(
                    "Error: Assignment loop finished but only assigned " + codeMap.size() + "/" + n + " codes");
            return null;
        }
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

    private static String encodeText(String text, Map<Character, String> codeTable) {
        StringBuilder encodedTextBuilder = new StringBuilder();
        // Iterate over each character
        for (int i = 0; i < text.length(); i++) {
            char symbol = text.charAt(i);
            String codeword = codeTable.get(symbol);
            encodedTextBuilder.append(codeword);
        }
        return encodedTextBuilder.toString();
    }
}