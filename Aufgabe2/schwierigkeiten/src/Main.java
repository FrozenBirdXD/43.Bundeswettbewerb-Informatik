import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class Main {
    private int nNumKlausuren;
    private int mNumTotalTasks;
    private int kNumDesiredTasks;
    private List<Klausur> klausuren;
    private Klausur resultKlausur;

    public Main() {
        this.klausuren = new ArrayList<>();
    }

    public static void main(String[] args) {
        Main main = new Main();
        try {
            main.readKlausurInput("schwierigkeiten/beispielaufgaben/schwierigkeiten1.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String, List<String>> graph = main.createDirectedGraphNoConflics();
        List<String> sortedGraph = main.sortGraphTopologicallyNoConflics(graph);
        main.getResultForKlausur(sortedGraph);
        System.out.println(main.getResultKlausur());
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

    // TODO: This wont work, because still have to account for num of input connections -> https://www.youtube.com/watch?v=4hAqEjj7IdE
    // Sort graph topologically using depth for search
    public List<String> sortGraphTopologicallyNoConflics(Map<String, List<String>> graph) {
        Stack<String> stack = new Stack<>();
        List<String> visited = new ArrayList<>();

        // For every unvisited vertex call depthForSearch
        for (String vertex : graph.keySet()) {
            if (!visited.contains(vertex)) {
                depthForSearch(visited, vertex, graph, stack);
            }
        }

        // Reverse ordering from the stack
        List<String> result = new ArrayList<>();
        for (String s : stack) {
            result.add(s);
        }
        return result;
    }

    // Recursive implementation of DFS
    private void depthForSearch(List<String> visited, String vertex, Map<String, List<String>> graph, Stack<String> stack) {
        if (!visited.contains(vertex)) {
            visited.add(vertex);
            for (String neighbors : graph.get(vertex)) {
                depthForSearch(visited, neighbors, graph, stack);
            }
            stack.push(vertex);
        }
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
        resultKlausur.setTasks(result);;
    }

    public Klausur getResultKlausur() {
        return this.resultKlausur;
    }
}
