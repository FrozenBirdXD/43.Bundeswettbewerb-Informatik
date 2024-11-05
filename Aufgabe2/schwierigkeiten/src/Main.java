import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Main {
    private int nNumKlausuren;
    private int mNumTotalTasks;
    private int kNumDesiredTasks;
    private List<Klausur> klausuren;
    private Klausur resultKlausur;

    public Main() {
        this.klausuren = new ArrayList<>();
    }

    public static void testNoConflicts() {
        System.out.println("Sorted Tasks no conflics - Result: ");
        Main main = new Main();
        try {
            main.readKlausurInput("schwierigkeiten/beispielaufgaben/schwierigkeiten0.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String, List<String>> graph = main.createDirectedGraphNoConflics();
        List<String> sortedGraph = main.sortGraphTopologicallyNoConflics(graph);
        main.getResultForKlausur(sortedGraph);
        System.out.println(main.getResultKlausur());
    }

    public static void testWithConflicts() {
        System.out.println("Sorted Tasks with conflics - Result: ");
        Main main = new Main();
        try {
            main.readKlausurInput("schwierigkeiten/beispielaufgaben/schwierigkeiten0.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String, Map<String, Integer>> graph = main.createDirectedGraphWithConflicts();
        List<String> sortedGraph = main.sortGraphTopologicallyWithConflicts(graph);
        main.getResultForKlausur(sortedGraph);
        System.out.println(main.getResultKlausur());
    }

    public static void main(String[] args) {
        testNoConflicts();
        testWithConflicts();
    }

    public void readKlausurInput(String filename) throws FileNotFoundException, IOException {
        File file = new File(filename);

        // Check if the file exists and is readable
        if (!file.exists() || !file.canRead() || !file.isFile()) {
            System.err.println("File cannot be read or does not exist: " + filename);
            System.exit(0);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            // Read first line
            String[] firstLine = reader.readLine().trim().split(" ");
            this.nNumKlausuren = Integer.parseInt(firstLine[0]);
            this.mNumTotalTasks = Integer.parseInt(firstLine[1]);
            this.kNumDesiredTasks = Integer.parseInt(firstLine[2]);

            // Read klausuren
            for (int i = 0; i < this.nNumKlausuren; i++) {
                String[] tasks = reader.readLine().trim().split(" < ");
                klausuren.add(new Klausur(Arrays.asList(tasks)));
            }

            // Read last line
            this.resultKlausur = new Klausur(Arrays.asList(reader.readLine().trim().split(" ")));
        }
    }

    // Create Graph
    public Map<String, List<String>> createDirectedGraphNoConflics() {
        // Adjazentliste
        Map<String, List<String>> graph = new HashMap<>();
        for (Klausur klausur : klausuren) {
            List<String> tasks = klausur.getTasks();
            for (int i = 0; i < tasks.size() - 1; i++) {
                graph.putIfAbsent(tasks.get(i), new ArrayList<>());
                graph.putIfAbsent(tasks.get(i + 1), new ArrayList<>());
                graph.get(tasks.get(i)).add(tasks.get(i + 1));
            }
        }
        return graph;
    }

    // Create graph and count how many times a relation is present
    public Map<String, Map<String, Integer>> createDirectedGraphWithConflicts() {
        Map<String, Map<String, Integer>> graph = new HashMap<>();

        for (Klausur klausur : klausuren) {
            List<String> tasks = klausur.getTasks();
            for (int i = 0; i < tasks.size() - 1; i++) {
                String easier = tasks.get(i);
                String harder = tasks.get(i + 1);

                // Add keys if absent
                graph.putIfAbsent(easier, new HashMap<>());
                graph.putIfAbsent(harder, new HashMap<>());

                // Update count realtion - in other words: update weight of Edge
                graph.get(easier).put(harder, graph.get(easier).getOrDefault(harder, 0) + 1);
                graph.get(harder).put(easier, graph.get(harder).getOrDefault(easier, 0) - 1);
            }
        }
        
        // print map
        /* for (Map.Entry<String,Map<String,Integer>> entry : graph.entrySet()) {
            System.out.println("Key: " + entry.getKey());
            System.out.println("Values: ");
            for (Map.Entry<String,Integer> e : entry.getValue().entrySet()) {
                System.out.println("\tKey: " + e.getKey());
                System.out.println("\tValues: " + e.getValue());
            }
        } */
        return graph;
    }

    // Sort graph topologically -> https://www.youtube.com/watch?v=4hAqEjj7IdE
    public List<String> sortGraphTopologicallyNoConflics(Map<String, List<String>> graph) {
        Map<String, Integer> inDegree = new HashMap<>();
        List<String> result = new ArrayList<>();

        // Fill in degree map
        for (String node : graph.keySet()) {
            inDegree.putIfAbsent(node, 0);
            for (String neighbors : graph.get(node)) {
                inDegree.put(neighbors, inDegree.getOrDefault(neighbors, 0) + 1);
            }
        }

        // Put every node with in degree of 0 into list - with no input edges
        List<String> currentNodes = new LinkedList<>();
        for (String node : inDegree.keySet()) {
            if (inDegree.get(node) == 0) {
                currentNodes.add(node);
            }
        }

        // Interate over all current Nodes with in degree of 0
        while (!currentNodes.isEmpty()) {
            String node = currentNodes.removeFirst();
            result.add(node);

            // Go over every neighbor of node
            for (String neighbor : graph.get(node)) {
                // Reduce in degree of neighbor by 1
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                // Add to current nodes if in degree is 0
                if (inDegree.get(neighbor) == 0) {
                    currentNodes.add(neighbor);
                }
            }
        }

        if (result.size() != graph.size()) {
            System.out.println("Error - Zyklus im Graph");
        }

        return result;
    }

    // TODO: FIX
    
    public List<String> sortGraphTopologicallyWithConflicts(Map<String, Map<String, Integer>> graph) {
        Map<String, Integer> inDegree = new HashMap<>();
        List<String> result = new ArrayList<>();

        // Fill in degree map based on easier than relation
        for (String node : graph.keySet()) {
            inDegree.putIfAbsent(node, 0);
            // Go over each entry of the Map returned by a node - contains neighbor nodes
            // and the amount of that relation
            for (Map.Entry<String, Integer> entry : graph.get(node).entrySet()) {
                String neighbor = entry.getKey();
                int weight = entry.getValue();

                // Just count easier than relation for the in degree map
                if (weight > 0) {
                    inDegree.put(neighbor, inDegree.getOrDefault(neighbor, 0) + 1);
                }
            }
        }

        // Put every node with in degree of 0 into list - with no input edges
        List<String> currentNodes = new LinkedList<>();
        for (String node : inDegree.keySet()) {
            if (inDegree.get(node) == 0) {
                currentNodes.add(node);
            }
        }

        // Interate over all current Nodes with in degree of 0
        while (!currentNodes.isEmpty()) {
            String node = currentNodes.removeFirst();
            result.add(node);

            // Go over every neighbor of node
            for (Map.Entry<String, Integer> entry : graph.get(node).entrySet()) {
                String neighbor = entry.getKey();
                int weight = entry.getValue();

                if (weight > 0) {
                    // Reduce in degree of neighbor by 1
                    inDegree.put(neighbor, inDegree.get(neighbor) - 1);

                    // Add to current nodes if in degree is 0
                    if (inDegree.get(neighbor) == 0) {
                        currentNodes.add(neighbor);
                    }
                }
            }
        }

        if (result.size() != graph.size()) {
            System.out.println("Error????? hää");
        }

        return result;
    }

    // Result
    public void getResultForKlausur(List<String> sortedGraph) {
        List<String> wanted = resultKlausur.getTasks();
        List<String> result = new ArrayList<>();
        for (String s : sortedGraph) {
            if (wanted.contains(s)) {
                result.add(s);
            }
        }
        resultKlausur.setTasks(result);
    }

    public Klausur getResultKlausur() {
        return this.resultKlausur;
    }
}
