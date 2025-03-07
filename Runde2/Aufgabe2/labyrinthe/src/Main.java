import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class Main {
    public static class Maze {
        private byte[][] maze;
        private int width, height;

        public Maze(byte[][] maze, int width, int height) {
            this.maze = maze;
            this.width = width;
            this.height = height;
        }

        public byte get(int x, int y) {
            return this.maze[y][x];
        }

        public void set(int x, int y, int value) {
            this.maze[y][x] = (byte) value;
        }

        public byte[][] getMaze() {
            return this.maze;
        }

        public int getMovableWidth() {
            return this.width;
        }

        public int getMovableHeight() {
            return this.height;
        }

        public int getArrayHeight() {
            return this.height * 2 + 1;
        }

        public int getArrayWidth() {
            return this.width * 2 + 1;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            for (byte[] row : maze) {
                builder.append(Arrays.toString(row));
                builder.append("\n");
            }
            return builder.toString();
        }
    }

    public record Mazes(Maze maze1, Maze maze2, int width, int height) {
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
            input = Files.readString(Path.of("beispielaufgaben/labyrinthe0.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(input);
        Mazes mazes = parseInput(input);

        System.out
                .println("Ausführungszeit in Sekunden: "
                        + (double) (System.currentTimeMillis() - start) / (long) 1000);
    }

    public static Mazes parseInput(String input) {
        // Save every line in Array of Strings
        String[] lines = input.split("\n");
        int index = 0;

        // Read width and height of the mazes
        String[] parts = lines[index].trim().split(" ");
        int width = Integer.parseInt(parts[0]);
        int height = Integer.parseInt(parts[1]);
        Maze maze1 = new Maze(new byte[height * 2 + 1][width * 2 + 1], width, height);
        Maze maze2 = new Maze(new byte[height * 2 + 1][width * 2 + 1], width, height);
        for (int y = 0; y < maze1.getArrayWidth(); y++) {
            maze1.set(0, y, 1);
            maze1.set(width * 2, y, 1);
            maze2.set(0, y, 1);
            maze2.set(width * 2, y, 1);
        }
        for (int x = 0; x < maze1.getArrayHeight(); x++) {
            maze1.set(x, 0, 1);
            maze1.set(x, height * 2, 1);
            maze2.set(x, 0, 1);
            maze2.set(x, height * 2, 1);
        }
        // Set walls
        for (int x = 0; x < maze1.getArrayHeight(); x++) {
            for (int y = 0; y < maze1.getArrayWidth(); y++) {
                maze1.set(x, 0, 1);
                maze1.set(x, height * 2, 1);
                maze2.set(x, 0, 1);
                maze2.set(x, height * 2, 1);
            }
        }

        System.exit(0);

        // Read first maze
        // Num of height lines each with num of width - 1 elements -> vertical walls
        for (int i = 0; i < height; i++) {
            parts = lines[index].trim().split(" ");
            for (int j = 0; j < parts.length; i++) {
                if (Integer.parseInt(parts[j]) == 1) {
                    // maze1[j + 1][i] = 1;
                } else {
                    // maze1[j + 1][i] = 0;
                }
            }

            index++;
        }

        return new Mazes(null, null, width, height);
    }

}
