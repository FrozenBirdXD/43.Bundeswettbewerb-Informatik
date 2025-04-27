import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

public class AStar {
    static final int[] dx = { -1, 1, 0, 0 }; // Left, Right, Up, Down
    static final int[] dy = { 0, 0, -1, 1 };
    static final String[] moves = { "←", "→", "↑", "↓" };
    static int width = 0;
    static int height = 0;
    static int[] costArray;

    public record StateMazes(Maze maze1, Maze maze2) {
    }

    public record State(int x1, int y1, int x2, int y2, int steps, State parent, char move) {
    }

    public static void main(String[] args) {

        String input = null;
        try {
            input = Files.readString(Path.of("beispielaufgaben/labyrinthe3.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 1. Parse input and save in Mazes
        StateMazes mazes = parseInput(input);
        width = mazes.maze1.getMovableWidth();
        height = mazes.maze1.getMovableHeight();
        // System.out.println(mazes.maze1);
        // System.out.println(mazes.maze2);
        int stateSpaceSize = width * height * width * height;
        if (stateSpaceSize < 0) {
            System.out.println("Labyrinth zu groß für diese A* Implementierung, verwende BFS! (ist schneller)");
            return;
        }
        costArray = new int[stateSpaceSize]; // Allocate cost array

        State result = null;
        long start = System.currentTimeMillis();

        // 2. Find shortest Path using BFS
        result = aStar(mazes.maze1, mazes.maze2);
        System.out
                .println("Ausführungszeit in Sekunden für A*: "
                        + (double) (System.currentTimeMillis() - start) / (long) 1000);
        if (result == null) {
            System.out.println("No solution");
        } else {
            System.out.println(
                    "Anweisungsfolge der Länge " + result.steps + ": " + reconstructPath(result));
        }
    }

    // 2. Use A* to find shortest sequence of moves
    public static State aStar(Maze maze1, Maze maze2) {
        // 2a.
        int count = 0;// Counter for visited states (for progress printing)

        // Priority queue for states
        PriorityQueue<State> queue = new PriorityQueue<>(
                Comparator.comparingInt(s -> s.steps + heuristic(s, maze1, maze2)));

        // Fill cost array with Inteer.MAX_VALUE
        Arrays.fill(costArray, Integer.MAX_VALUE);
        // 2b. Create Startstate and add starting state
        State startState = new State(0, 0, 0, 0, 0, null, ' ');
        queue.add(startState);
        updateCost(0, 0, 0, 0, 0);

        // 2c. Main A* loop
        while (!queue.isEmpty()) {
            if (count % 1000000 == 0 && count != 0) {
                System.out.println("States visited: " + count);
            }

            // 2ci. Get the next state from front of the queue
            State currentState = queue.poll();
            count++;

            // 2cii. Check if end state reached
            if (currentState.x1 == maze1.getGoalX() && currentState.x2 == maze2.getGoalX()
                    && currentState.y1 == maze1.getGoalY() && currentState.y2 == maze2.getGoalY()) {
                System.out.println("States visited: " + count);
                // Solution found
                return currentState;
            }
            // 2ciii. Explore neighbors (apply each move)
            for (int i = 0; i < 4; i++) {
                // Calculate potential next coordinates
                int nx1 = currentState.x1 + dx[i];
                int ny1 = currentState.y1 + dy[i];
                int nx2 = currentState.x2 + dx[i];
                int ny2 = currentState.y2 + dy[i];

                // Check walls if move is invalid, player stays
                if (!maze1.isValidMove(currentState.x1, currentState.y1, nx1, ny1)) {
                    nx1 = currentState.x1;
                    ny1 = currentState.y1;
                }
                if (!maze2.isValidMove(currentState.x2, currentState.y2, nx2, ny2)) {
                    nx2 = currentState.x2;
                    ny2 = currentState.y2;
                }

                // Check for holes if player lands on a hole, reset to start
                boolean reset1 = maze1.isHole(nx1, ny1);
                boolean reset2 = maze2.isHole(nx2, ny2);
                if (reset1) {
                    nx1 = 0;
                    ny1 = 0;
                }
                if (reset2) {
                    nx2 = 0;
                    ny2 = 0;
                }

                // Check if neighbor State is already visited
                int newCost = currentState.steps + 1;
                if (isBetterPath(nx1, ny1, nx2, ny2, newCost)) {
                    // Update cost to state
                    updateCost(nx1, ny1, nx2, ny2, newCost);
                    // Create the new state and add it to the queue
                    queue.add(new State(nx1, ny1, nx2, ny2, newCost, currentState,
                            moves[i].charAt(0)));
                }
            }
        }
        return null;
    }

    // Admissible heuristic for this problem 
    // Returns max ManhattenDistance from both the players
    private static int heuristic(State state, Maze maze1, Maze maze2) {
        return Math.max(Math.abs(state.x1 - maze1.getGoalX()) + Math.abs(state.y1 - maze1.getGoalY()),
                Math.abs(state.x2 - maze2.getGoalX()) + Math.abs(state.y2 - maze2.getGoalY()));
    }

    // Check if path to a state is better by checking cost in cost array
    private static boolean isBetterPath(int x1, int y1, int x2, int y2, int newCost) {
        int index = encodeState(x1, y1, x2, y2);
        return newCost < costArray[index];
    }

    // 3. reconstruct path from final state
    public static String reconstructPath(State finalState) {
        StringBuilder path = new StringBuilder();
        // Iterate over parent chain and put into stringbuilder
        while (finalState.parent != null) {
            path.append(finalState.move);
            finalState = finalState.parent;
        }
        // Reverse the path to get final shortest path
        path.reverse();
        return path.toString();
    }

    // Set corresponding value in cost array
    private static void updateCost(int x1, int y1, int x2, int y2, int cost) {
        int index = encodeState(x1, y1, x2, y2);
        costArray[index] = cost;
    }

    // Map a state to index in cost array
    private static int encodeState(int x1, int y1, int x2, int y2) {
        return ((x1 * height + y1) * width * height + (x2 * height + y2));
    }

    // 1. Parse input
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
        maze1.numHoles = Integer.parseInt(lines[index++].trim());

        for (int i = 0; i < maze1.numHoles; i++) {
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
        maze2.numHoles = Integer.parseInt(lines[index++].trim());

        for (int i = 0; i < maze2.numHoles; i++) {
            parts = lines[index].trim().split(" ");
            maze2.setHole(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        }

        return new StateMazes(maze1, maze2);
    }
}