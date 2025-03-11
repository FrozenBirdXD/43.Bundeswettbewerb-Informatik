import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Queue;

public class Main2 {
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
            // Read input from file
            //
            //
            // Input file path here: //
            //
            //
            input = Files.readString(Path.of("beispielaufgaben/labyrinthe5.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        StateMazes mazes = parseInput(input);
        width = mazes.maze1.getMovableWidth();
        height = mazes.maze1.getMovableHeight();
        // System.out.println(mazes.maze1);
        // System.out.println(mazes.maze2);
        State result = null;
        long start = System.currentTimeMillis();

        result = breadthFirstSearchWithHoles(mazes.maze1, mazes.maze2);
        System.out
                .println("Ausführungszeit in Sekunden für BFS: "
                        + (double) (System.currentTimeMillis() - start) / (long) 1000);
        if (result == null) {
            System.out.println("No solution");
        } else {
            System.out.println(
                    "Anweisungsfolge der Länge " + result.steps + ": " + reconstructPath(result));
        }
    }

    public static State breadthFirstSearchWithHoles(Maze maze1, Maze maze2) {
        int count = 0;

        Queue<State> queue = new ArrayDeque<>();
        initializeBitSets();
        State startState = new State(0, 0, 0, 0, 0, null, ' ');
        queue.add(startState);
        setVisited(0, 0, 0, 0);

        while (!queue.isEmpty()) {
            if (count % 1000000 == 0) {
                System.out.println("States visited: " + count);
            }
            State currentState = queue.poll();
            count++;

            // Goal check
            if (currentState.x1 == maze1.getGoalX() && currentState.x2 == maze2.getGoalX()
                    && currentState.y1 == maze1.getGoalY() && currentState.y2 == maze2.getGoalY()) {
                System.out.println("States visited: " + count);
                return currentState;
            }
            for (int i = 0; i < 4; i++) {
                int nx1 = currentState.x1 + dx[i];
                int ny1 = currentState.y1 + dy[i];
                int nx2 = currentState.x2 + dx[i];
                int ny2 = currentState.y2 + dy[i];

                // Check walls
                if (!maze1.isValidMove(currentState.x1, currentState.y1, nx1, ny1)) {
                    nx1 = currentState.x1;
                    ny1 = currentState.y1;
                }
                if (!maze2.isValidMove(currentState.x2, currentState.y2, nx2, ny2)) {
                    nx2 = currentState.x2;
                    ny2 = currentState.y2;
                }

                // Check for holes
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

                if (!isVisited(nx1, ny1, nx2, ny2)) {
                    setVisited(nx1, ny1, nx2, ny2);
                    queue.add(new State(nx1, ny1, nx2, ny2, currentState.steps + 1, currentState,
                            moves[i].charAt(0)));
                }
            }
        }
        return null;
    }

    private static void initializeBitSets() {
        // Ensure no overflow
        long totalStates = (long) width * height * width * height;
        numBitSets = (int) ((totalStates / BITSET_SIZE) + 1);
        visited = new BitSet[numBitSets];

        for (int i = 0; i < numBitSets; i++) {
            visited[i] = new BitSet(BITSET_SIZE);
        }
    }

    public static String reconstructPath(State finalState) {
        StringBuilder path = new StringBuilder();
        while (finalState.parent != null) {
            path.append(finalState.move);
            finalState = finalState.parent;
        }
        path.reverse();
        return path.toString();
    }

    private static long encodeState(int x1, int y1, int x2, int y2) {
        return ((long) x1 * height + y1) * width * height + ((long) x2 * height + y2);
    }

    private static void setVisited(int x1, int y1, int x2, int y2) {
        long index = encodeState(x1, y1, x2, y2);
        int bitsetIndex = (int) (index / BITSET_SIZE);
        int bitIndex = (int) (index % BITSET_SIZE);

        visited[bitsetIndex].set(bitIndex);
    }

    private static boolean isVisited(int x1, int y1, int x2, int y2) {
        long index = encodeState(x1, y1, x2, y2);
        int bitsetIndex = (int) (index / BITSET_SIZE);
        int bitIndex = (int) (index % BITSET_SIZE);

        return visited[bitsetIndex].get(bitIndex);
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
