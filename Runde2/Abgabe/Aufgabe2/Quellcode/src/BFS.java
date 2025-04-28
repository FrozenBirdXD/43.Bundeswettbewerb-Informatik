import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Deque;
import java.util.List;
import java.util.Queue;

public class BFS {
    static final int[] dx = { -1, 1, 0, 0 }; // Left, Right, Up, Down
    static final int[] dy = { 0, 0, -1, 1 };
    static final String[] moves = { "←", "→", "↑", "↓" };
    static int width = 0;
    static int height = 0;

    static final int BITSET_SIZE = Integer.MAX_VALUE; // Max bits per BitSet
    static BitSet[] visited;
    static int numBitSets;

    public record StateMazes(Maze maze1, Maze maze2) {
    }

    public record State(int x1, int y1, int x2, int y2, int steps, State parent, char move) {
    }

    public static void main(String[] args) {

        String input = null;
        try {
            input = Files.readString(Path.of("beispielaufgaben/labyrinthe9.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 1. Parse input and save in Mazes
        StateMazes mazes = parseInput(input);
        width = mazes.maze1.getMovableWidth();
        height = mazes.maze1.getMovableHeight();

        State result;
        long start = System.currentTimeMillis();

        // 2. Find shortest Path using BFS
        result = bfs(mazes.maze1, mazes.maze2);
        String shortestPath = reconstructPath(result);
        System.out
                .println("Ausführungszeit in Sekunden für BFS: "
                        + (double) (System.currentTimeMillis() - start) / (long) 1000);
        if (result == null) {
            System.out.println("No solution");
        } else {
            System.out.println(
                    "Anweisungsfolge der Länge " + result.steps + ": " + shortestPath);
        }

        List<State> pathStates = reconstructPathStates(result); // Get the list of states

        // Save SVG for Maze 1 with Player 1 path
        //mazes.maze1.saveMazeAsSvg("maze91.svg", 100, pathStates, 1);

        // Save SVG for Maze 2 with Player 2 path
        //mazes.maze2.saveMazeAsSvg("maze92.svg", 100, pathStates, 2);
    }

    // 2. Use BFS to find shortest sequence of moves
    public static State bfs(Maze maze1, Maze maze2) {
        // 2a.
        int count = 0; // Counter for visited states (for progress printing)

        // FIFO queue for BFS states
        Queue<State> queue = new ArrayDeque<>();
        // Create Bitsets
        initializeBitSets();
        // 2b. Create Startstate and add starting state
        State startState = new State(0, 0, 0, 0, 0, null, ' ');
        queue.add(startState);
        setVisited(0, 0, 0, 0);

        // 2c. Main BFS loop
        while (!queue.isEmpty()) {

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
                if (!isVisited(nx1, ny1, nx2, ny2)) {
                    // Mark as visited
                    setVisited(nx1, ny1, nx2, ny2);
                    // Create the new state and add it to the queue
                    queue.add(new State(nx1, ny1, nx2, ny2, currentState.steps + 1, currentState,
                            moves[i].charAt(0)));
                }
            }
        }
        return null;
    }

    // 2a. create enough bitsets for maze
    private static void initializeBitSets() {
        // Ensure no overflow and calculate amount of bitsets and size
        long totalStates = (long) width * height * width * height;
        numBitSets = (int) ((totalStates / BITSET_SIZE) + 1);
        visited = new BitSet[numBitSets];

        // Allocate each bitset segment
        for (int i = 0; i < numBitSets; i++) {
            visited[i] = new BitSet(BITSET_SIZE);
        }
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

    // Map a state to index in bitsets
    private static long encodeState(int x1, int y1, int x2, int y2) {
        long spacePerMaze = width * height;
        return ((long) x1 * height + y1) * spacePerMaze + ((long) x2 * height + y2);
    }

    // Set corresponding bit in Bitset to indicate if state already visited
    private static void setVisited(int x1, int y1, int x2, int y2) {
        // Calculate which bitset and which bit
        long index = encodeState(x1, y1, x2, y2);
        int bitsetIndex = (int) (index / BITSET_SIZE);
        int bitIndex = (int) (index % BITSET_SIZE);

        // Set corresponding bit
        visited[bitsetIndex].set(bitIndex);
    }

    // Get value of corresponding bit from bitset to see if state already visited
    private static boolean isVisited(int x1, int y1, int x2, int y2) {
        long index = encodeState(x1, y1, x2, y2);
        int bitsetIndex = (int) (index / BITSET_SIZE);
        int bitIndex = (int) (index % BITSET_SIZE);

        return visited[bitsetIndex].get(bitIndex);
    }

    public static List<State> reconstructPathStates(State finalState) {
        if (finalState == null) {
            return new ArrayList<>(); // Return empty list if no solution
        }
        Deque<State> pathDeque = new ArrayDeque<>();
        State current = finalState;
        while (current != null) { // Include the start state
            pathDeque.addFirst(current);
            current = current.parent;
        }
        return new ArrayList<>(pathDeque); // Convert Deque to List
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
