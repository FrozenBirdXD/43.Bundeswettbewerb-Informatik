import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    public record StateMazes(Maze maze1, Maze maze2) {
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
            input = Files.readString(Path.of("beispielaufgaben/labyrinthe1.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        StateMazes mazes = parseInput(input);

        System.out
                .println("Ausführungszeit in Sekunden: "
                        + (double) (System.currentTimeMillis() - start) / (long) 1000);
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
        System.out.println(maze1.getNumHoles());

        for (int i = 0; i < maze1.getNumHoles(); i++) {
            parts = lines[index++].trim().split(" ");
            maze1.setHole(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        }

        System.out.println(maze1);

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

        System.out.println(maze2);

        return new StateMazes(maze1, maze2);
    }
}
