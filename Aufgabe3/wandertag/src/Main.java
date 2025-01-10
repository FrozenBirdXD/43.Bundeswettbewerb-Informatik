import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

public class Main {
    // Class to represent one persons distance wishes
    public static class Wish {
        int min;
        int max;

        public Wish(int min, int max) {
            this.min = min;
            this.max = max;
        }
    }

    // Represents end result
    public static class Result {
        int maxParticipants;
        List<Integer> distances;
        List<Set<Integer>> participants;

        public Result(int maxParticipants, List<Integer> distances, List<Set<Integer>> participants) {
            this.maxParticipants = maxParticipants;
            this.distances = distances;
            this.participants = participants;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Maximale Teilnehmerzahl: " + maxParticipants + "\n");
            sb.append("Beste Streckenlängen in m: " + distances + "\n");
            for (int i = 0; i < distances.size(); i++) {
                sb.append("Strecke " + distances.get(i) + " m: Teilnehmer " + participants.get(i) + "\n");
            }
            return sb.toString();
        }
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        String input = null;
        try {
            // Read input from file
            input = Files.readString(Path.of("./wandertag/beispielaufgaben/wandern5.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<Wish> wishes = parseInput(input);

        // Calculaate 3 best distances for max amount of participants
        Result bestDistances = calculateBestDistances(wishes);

        System.out.println(bestDistances);
        System.out
                .println("Ausführungszeit in Sekunden: " + (double) (System.currentTimeMillis() - start) / (long) 1000);
    }

    /**
     * Method to parse input file with distance wishes by possible participants
     * 
     * @param input - Inputfile as String
     * @return List<Wish> - List of participants' distance wishes
     */
    public static List<Wish> parseInput(String input) {
        List<Wish> wishes = new ArrayList<>();
        // Save every line in Array of Strings
        String[] lines = input.split("\n");
        int totalCountWishes = Integer.parseInt(lines[0].trim());

        // For every line split at space and save as a Wish object
        for (int i = 1; i <= totalCountWishes; i++) {
            String[] parts = lines[i].trim().split(" ");
            int min = Integer.parseInt(parts[0]);
            int max = Integer.parseInt(parts[1]);
            wishes.add(new Wish(min, max));
        }
        return wishes;
    }

    /**
     * Calculate 3 distances for max amount of participants
     * 
     * @param wishes - List of distance wishes
     * @return Result - 3 best distances for max amount of participants
     */
    public static Result calculateBestDistances(List<Wish> wishes) {
        // 1.
        // Get all possible distances, just checking every min & max wish
        // Put into set for unique values
        Set<Integer> possibleDistancesSet = new HashSet<>();
        for (Wish wish : wishes) {
            possibleDistancesSet.add(wish.min);
            possibleDistancesSet.add(wish.max);
        }

        // Convert to list to sort
        List<Integer> possibleDistances = new ArrayList<>(possibleDistancesSet);
        Collections.sort(possibleDistances);

        // 2.
        // Create BitSets
        // Map: Key - Distance; Value - Participants (Bitset)
        Map<Integer, BitSet> participantsPerDistance = new HashMap<>();
        for (int i = 0; i < possibleDistances.size(); i++) {
            int distance = possibleDistances.get(i);
            BitSet participant = new BitSet();
            for (int j = 0; j < wishes.size(); j++) {
                Wish w = wishes.get(j);
                if (w.min <= distance && distance <= w.max) {
                    // Set Bit to true if participating
                    participant.set(j);
                }
            }
            participantsPerDistance.put(distance, participant);
        }

        // 3.
        // Declare and initialize atomic variables for use in parallel
        AtomicInteger maxCountParticipants = new AtomicInteger(0);
        AtomicReference<List<Integer>> bestDistances = new AtomicReference<>(null);
        AtomicReference<List<Set<Integer>>> bestParticipantDistribution = new AtomicReference<>(null);

        // Parallelize outer i loop with Integer Stream
        IntStream.range(0, possibleDistances.size() - 2).parallel().forEach(i -> {
            BitSet iWish = participantsPerDistance.get(possibleDistances.get(i));

            // J loop
            for (int j = i + 1; j < possibleDistances.size() - 1; j++) {
                BitSet jWish = participantsPerDistance.get(possibleDistances.get(j));

                // Create union of i and j participants
                BitSet ijUnion = (BitSet) iWish.clone();
                ijUnion.or(jWish);

                for (int k = j + 1; k < possibleDistances.size(); k++) {
                    BitSet kWish = participantsPerDistance.get(possibleDistances.get(k));

                    // Create union of i, j, and k participants
                    BitSet participantSetUnion = (BitSet) ijUnion.clone();
                    participantSetUnion.or(kWish);

                    int totalParticipants = participantSetUnion.cardinality();

                    // Update best result
                    // synchronized since multiple threads might try to right at the same time
                    synchronized (maxCountParticipants) {
                        if (totalParticipants > maxCountParticipants.get()) {
                            // Current best total participants
                            maxCountParticipants.set(totalParticipants);
                            // Current best distances
                            bestDistances.set(
                                    Arrays.asList(possibleDistances.get(i), possibleDistances.get(j),
                                            possibleDistances.get(k)));

                            // 4.
                            // Convert BitSet to Set<Integer>
                            List<Set<Integer>> currentDistribution = Arrays.asList(
                                    convertBitSetToSet(iWish),
                                    convertBitSetToSet(jWish),
                                    convertBitSetToSet(kWish));
                            bestParticipantDistribution.set(currentDistribution);
                        }
                    }
                }
            }
        });

        return new Result(maxCountParticipants.get(), bestDistances.get(), bestParticipantDistribution.get());
    }

    /**
     * Helper method to convert bitset to human readable Integer set
     * 
     * @param bitSet
     * @return Set<Integer>
     */
    private static Set<Integer> convertBitSetToSet(BitSet bitSet) {
        Set<Integer> set = new HashSet<>();
        bitSet.stream().forEach((x) -> set.add(x));
        return set;
    }
}
