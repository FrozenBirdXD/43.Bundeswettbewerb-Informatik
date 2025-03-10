import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

public class Main {
    static final int[] dx = { -1, 1, 0, 0 }; // Left, Right, Up, Down
    static final int[] dy = { 0, 0, -1, 1 };
    static final String[] moves = { "←", "→", "↑", "↓" };

    public record StateMazes(Maze maze1, Maze maze2) {
    }

    public record State(int x1, int y1, int x2, int y2, int steps, String path, int cost) {
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        String input = null;
        try {
            // Read input from file
            //
            //
            // Input file path here: //
            //
            //
            input = Files.readString(Path.of("beispielaufgaben/labyrinthe4.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        StateMazes mazes = parseInput(input);
        // System.out.println(mazes.maze1);
        // System.out.println(mazes.maze2);
        State result = null;
        result = breadthFirstSearch(mazes.maze1, mazes.maze2);
        System.out
                .println("Ausführungszeit in Sekunden für BFS: "
                        + (double) (System.currentTimeMillis() - start) / (long) 1000);
        if (result == null) {
            System.out.println("No solution");
        } else {
            System.out.println("Anweisungsfolge der Länge " + result.steps + ": " + result.path + "");
        }

        start = System.currentTimeMillis();
        State result2 = null;
        result2 = aStarManhatten(mazes.maze1, mazes.maze2);
        System.out
                .println("Ausführungszeit in Sekunden für A*: "
                        + (double) (System.currentTimeMillis() - start) / (long) 1000);

        if (result2 == null) {
            System.out.println("No solution");
        } else {
            System.out.println(
                    "Anweisungsfolge der Länge " + result2.steps + ": " + result2.path + "\nCost: " + result2.cost);
        }

    }

    public static State aStarManhatten(Maze maze1, Maze maze2) {
        // Prioritizes the paths with the lowest cost: f(x) = g(x)(cost so far) +
        // h(x)(heuristic)
        int count = 0;
        PriorityQueue<State> queue = new PriorityQueue<>(Comparator.comparingInt(s -> s.cost));
        Map<String, Integer> visited = new HashMap<>();

        int h = heuristicManhattenDistance(0, 0, maze1.getGoalX(), maze1.getGoalY(), 0, 0, maze2.getGoalX(),
                maze2.getGoalY());
        State startState = new State(0, 0, 0, 0, 0, "", 0);
        queue.add(startState);
        visited.put(encode(startState), h);

        while (!queue.isEmpty()) {
            count++;
            State currentState = queue.poll();

            if (currentState.x1 == maze1.getGoalX() && currentState.x2 == maze2.getGoalX()
                    && currentState.y1 == maze1.getGoalY() && currentState.y2 == maze2.getGoalY()) {
                // Return shortest Path
                System.out.println("States visited: " + count);
                return currentState;
            }

            for (int i = 0; i < 4; i++) {
                int nx1 = currentState.x1 + dx[i];
                int ny1 = currentState.y1 + dy[i];
                int nx2 = currentState.x2 + dx[i];
                int ny2 = currentState.y2 + dy[i];

                // Check boundaries
                if (!maze1.isValidMove(currentState.x1, currentState.y1, nx1, ny1)) {
                    nx1 = currentState.x1;
                    ny1 = currentState.y1;
                }
                if (!maze2.isValidMove(currentState.x2, currentState.y2, nx2, ny2)) {
                    nx2 = currentState.x2;
                    ny2 = currentState.y2;
                }

                int newCost = currentState.steps + 1
                        + heuristicManhattenDistance(nx1, ny1, maze1.getGoalX(), maze1.getGoalY(), nx2, ny2,
                                maze2.getGoalX(), maze2.getGoalY());
                String stateKey = encode(nx1, ny1, nx2, ny2);

                if (!visited.containsKey(stateKey) || newCost < visited.get(stateKey)) {
                    visited.put(stateKey, newCost);
                    queue.add(new State(nx1, ny1, nx2, ny2, currentState.steps + 1, currentState.path + moves[i],
                            newCost));
                }
            }
        }

        return null;
    }

    private static int heuristicManhattenDistance(int x1, int y1, int goalx1, int goaly1, int x2, int y2, int goalx2,
            int goaly2) {
        return Math.max(Math.abs(x1 - goalx1) + Math.abs(y1 - goaly1), Math.abs(x2 -
                goalx2) + Math.abs(y2 - goaly2));
        // return 2 * (Math.abs(x1 - goalx1) + Math.abs(y1 - goaly1) + Math.abs(x2 -
        // goalx2) + Math.abs(y2 - goaly2));
    }

    public static State breadthFirstSearch(Maze maze1, Maze maze2) {
        int count = 0;
        int width = maze1.getMovableWidth();
        int height = maze1.getMovableHeight();

        Queue<State> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();

        State startState = new State(0, 0, 0, 0, 0, "", 0);
        queue.add(startState);
        visited.add(encode(startState));

        while (!queue.isEmpty()) {
            if (count % 10000 == 0) {
                System.out.println("States visited: " + count);
            }
            State currentState = queue.poll();
            count++;
            if (currentState.x1 == maze1.getGoalX() && currentState.x2 == maze2.getGoalX()
                    && currentState.y1 == maze1.getGoalY() && currentState.y2 == maze2.getGoalY()) {
                // Return shortest Path
                System.out.println("States visited: " + count);
                return currentState;
            }

            for (int i = 0; i < 4; i++) {
                int nx1 = currentState.x1 + dx[i];
                int ny1 = currentState.y1 + dy[i];
                int nx2 = currentState.x2 + dx[i];
                int ny2 = currentState.y2 + dy[i];

                // Check boundaries
                if (!maze1.isValidMove(currentState.x1, currentState.y1, nx1, ny1)) {
                    nx1 = currentState.x1;
                    ny1 = currentState.y1;
                }
                if (!maze2.isValidMove(currentState.x2, currentState.y2, nx2, ny2)) {
                    nx2 = currentState.x2;
                    ny2 = currentState.y2;
                }

                String stateKey = encode(nx1, ny1, nx2, ny2);
                if (!visited.contains(stateKey)) {
                    /*
                     * if (nx1 == 1) {
                     * maze1.moveRight();
                     * maze2.moveRight();
                     * } else if (nx1 == -1) {
                     * maze1.moveLeft();
                     * maze2.moveLeft();
                     * } else if (ny1 == 1) {
                     * maze1.moveDown();
                     * maze2.moveDown();
                     * } else {
                     * maze1.moveUp();
                     * maze2.moveUp();
                     * }
                     */
                    // System.out.println(maze1);
                    // System.out.println(maze2);
                    visited.add(stateKey);
                    queue.add(new State(nx1, ny1, nx2, ny2, currentState.steps + 1, currentState.path + moves[i], 0));
                }
            }
        }
        return null;
    }

    private static String encode(int x1, int y1, int x2, int y2) {
        return x1 + "," + y1 + "," + x2 + "," + y2;
    }

    private static String encode(State state) {
        return state.x1 + "," + state.y1 + "," + state.x2 + "," + state.y2;
    }

    public static StateMazes parseInput(String input) {
        // Save every line in Array of Strings
        String[] lines = input.split("\n");
        int index = 0;

        // Read width and height of the mazes
        String[] parts = lines[index++].trim().split(" ");
        int width = Integer.parseInt(parts[0]);
        int height = Integer.parseInt(parts[1]);
        Maze maze1 = new Maze(new byte[height * 2 + 1][width * 2 + 1], width, height);
        Maze maze2 = new Maze(new byte[height * 2 + 1][width * 2 + 1], width, height);

        // Read first maze
        // Num of height lines each with num of width - 1 elements -> vertical walls
        for (int y = 0; y < height; y++) {
            parts = lines[index].trim().split(" ");
            for (int x = 0; x < parts.length; x++) {
                if (Integer.parseInt(parts[x]) == 1) {
                    maze1.set(x * 2 + 2, y * 2 + 1, 1);
                }
            }
            index++;
        }

        for (int y = 0; y < height - 1; y++) {
            parts = lines[index].trim().split(" ");
            for (int x = 0; x < parts.length; x++) {
                if (Integer.parseInt(parts[x]) == 1) {
                    maze1.set(x * 2 + 1, y * 2 + 2, 1);
                }
            }
            index++;
        }
        maze1.setNumHoles(Integer.parseInt(lines[index++].trim()));

        for (int i = 0; i < maze1.getNumHoles(); i++) {
            parts = lines[index++].trim().split(" ");
            maze1.setHole(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        }

        // Read second maze
        // Num of height lines each with num of width - 1 elements -> vertical walls
        for (int y = 0; y < height; y++) {
            parts = lines[index].trim().split(" ");
            for (int x = 0; x < parts.length; x++) {
                if (Integer.parseInt(parts[x]) == 1) {
                    maze2.set(x * 2 + 2, y * 2 + 1, 1);
                }
            }
            index++;
        }

        for (int y = 0; y < height - 1; y++) {
            parts = lines[index].trim().split(" ");
            for (int x = 0; x < parts.length; x++) {
                if (Integer.parseInt(parts[x]) == 1) {
                    maze2.set(x * 2 + 1, y * 2 + 2, 1);
                }
            }
            index++;
        }
        maze2.setNumHoles(Integer.parseInt(lines[index++].trim()));

        for (int i = 0; i < maze2.getNumHoles(); i++) {
            parts = lines[index].trim().split(" ");
            maze2.setHole(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        }

        return new StateMazes(maze1, maze2);
    }
}
