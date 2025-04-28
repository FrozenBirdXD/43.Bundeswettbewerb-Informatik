import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class Maze {
    byte[][] maze;
    int width, height;
    int numHoles;

    /*
     * 0 = Path
     * 1 = Wall
     * 2 = Hole
     */

    public Maze(byte[][] maze, int width, int height) {
        this.maze = maze;
        this.width = width;
        this.height = height;
        this.numHoles = 0;

        for (int y = 0; y < getArrayHeight(); y++) {
            set(0, y, 1);
            set(width * 2, y, 1);
            set(0, y, 1);
            set(width * 2, y, 1);
        }
        for (int x = 0; x < getArrayWidth(); x++) {
            set(x, 0, 1);
            set(x, height * 2, 1);
            set(x, 0, 1);
            set(x, height * 2, 1);
        }

        // Set walls
        for (int x = 1; x < getMovableWidth(); x++) {
            for (int y = 1; y < getMovableHeight(); y++) {
                set(x * 2, y * 2, 1);
                set(x * 2, y * 2, 1);
            }
        }
    }

    // Use logic from maze represented in byte[][] array
    public boolean isValidMove(int x, int y, int newX, int newY) {
        // Right
        if (x < newX) {
            if (x == width || get(x * 2 + 2, y * 2 + 1) == 1) {
                return false;
            }
            // Left
        } else if (x > newX) {
            if (x == 0 || get(x * 2, y * 2 + 1) == 1) {
                return false;
            }
        }
        // Down
        if (y < newY) {
            if (y == height || get(x * 2 + 1, y * 2 + 2) == 1) {
                return false;
            }
            // Up
        } else if (y > newY) {
            if (y == 0 || get(x * 2 + 1, y * 2) == 1) {
                return false;
            }
        }
        return true;
    }

    public boolean isHole(int x, int y) {
        return get(x * 2 + 1, y * 2 + 1) == 2;
    }

    public void setHole(int x, int y) {
        this.set(x * 2 + 1, y * 2 + 1, 2);
    }

    public byte get(int x, int y) {
        return this.maze[y][x];
    }

    public void set(int x, int y, int value) {
        this.maze[y][x] = (byte) value;
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

    public int getGoalX() {
        return width - 1;
    }

    public int getGoalY() {
        return height - 1;
    }

    // This method is not important for the algorithms and the logic of it
    // This is just to visualize the mazes and the calculates paths
    // Change method signature and add path drawing logic
    public void saveMazeAsSvg(String filename, int cellSize, List<AStar.State> path, int playerIndex /* 1 or 2 */ ) {
        // Or use BFS.State if calling from BFS
        int svgWidth = getArrayWidth() * cellSize;
        int svgHeight = getArrayHeight() * cellSize;

        try (PrintWriter out = new PrintWriter(new FileWriter(filename))) {
            // SVG Header
            out.println("<svg width=\"" + svgWidth + "\" height=\"" + svgHeight
                    + "\" xmlns=\"http://www.w3.org/2000/svg\" "
                    + "xmlns:xlink=\"http://www.w3.org/1999/xlink\">");

            out.println(
                    "  <rect x=\"0\" y=\"0\" width=\"" + svgWidth + "\" height=\"" + svgHeight + "\" fill=\"white\"/>");

            for (int y = 0; y < getArrayHeight(); y++) {
                for (int x = 0; x < getArrayWidth(); x++) {
                    byte cell = get(x, y);
                    String color = "magenta"; 
                    boolean drawRect = true; 

                    switch (cell) {
                        case 0: 
                            if (x % 2 == 1 && y % 2 == 1) { 
                                if (x == getMovableWidth() * 2 - 1 && y == getMovableHeight() * 2 - 1) {
                                    color = "lime"; 
                                } else {
                                    color = "#d3d3d3"; 
                                }
                            } else {
                                color = "white"; 
                                drawRect = false; 
                            }
                            break;
                        case 1: 
                            color = "black";
                            break;
                        case 2: 
                            color = "blue";
                            break;
                        default: 
                            if (cell == 8 && x == 1 && y == 1) { 
                                color = "#d3d3d3"; 
                            } else {
                                color = "white";
                                drawRect = false;
                            }
                            break;

                    }

                    if (drawRect) {
                        out.println("  <rect x=\"" + (x * cellSize) + "\" y=\"" + (y * cellSize) +
                                "\" width=\"" + cellSize + "\" height=\"" + cellSize +
                                "\" fill=\"" + color + "\"/>");
                    }
                }
            }

            double startCenterX = (1 * cellSize + cellSize / 2.0); 
            double startCenterY = (1 * cellSize + cellSize / 2.0);
            out.println("  <circle cx=\"" + startCenterX + "\" cy=\"" + startCenterY +
                    "\" r=\"" + (cellSize / 2.5) + "\" fill=\"red\" stroke=\"black\" stroke-width=\"1\"/>");

            double goalX = (getMovableWidth() * 2 - 1);
            double goalY = (getMovableHeight() * 2 - 1);
            out.println("  <rect x=\"" + (goalX * cellSize + cellSize / 4.0) + "\" y=\""
                    + (goalY * cellSize + cellSize / 4.0) +
                    "\" width=\"" + (cellSize / 2.0) + "\" height=\"" + (cellSize / 2.0) +
                    "\" fill=\"darkgreen\"/>");

            if (path != null && path.size() > 1) {
                String pathStrokeColor = (playerIndex == 1) ? "orange" : "cyan"; 
                double pathStrokeWidth = Math.max(1.0, cellSize / 5.0); 
                double pathOpacity = 0.75;

                out.println("  <g id=\"path-player" + playerIndex + "\" stroke=\"" + pathStrokeColor
                        + "\" stroke-width=\"" + pathStrokeWidth + "\" stroke-opacity=\"" + pathOpacity
                        + "\" stroke-linecap=\"round\" fill=\"none\">");

                for (int i = 0; i < path.size() - 1; i++) {
                    AStar.State s1 = path.get(i); 
                    AStar.State s2 = path.get(i + 1);

                    int logX1 = (playerIndex == 1) ? s1.x1() : s1.x2();
                    int logY1 = (playerIndex == 1) ? s1.y1() : s1.y2();
                    int logX2 = (playerIndex == 1) ? s2.x1() : s2.x2();
                    int logY2 = (playerIndex == 1) ? s2.y1() : s2.y2();

                    double ax1 = (logX1 * 2 + 1);
                    double ay1 = (logY1 * 2 + 1);
                    double ax2 = (logX2 * 2 + 1);
                    double ay2 = (logY2 * 2 + 1);

                    double svgCX1 = ax1 * cellSize + cellSize / 2.0;
                    double svgCY1 = ay1 * cellSize + cellSize / 2.0;
                    double svgCX2 = ax2 * cellSize + cellSize / 2.0;
                    double svgCY2 = ay2 * cellSize + cellSize / 2.0;

                    if (logX1 != logX2 || logY1 != logY2) {
                        out.println("    <line x1=\"" + svgCX1 + "\" y1=\"" + svgCY1
                                + "\" x2=\"" + svgCX2 + "\" y2=\"" + svgCY2 + "\"/>");
                    } else {
                    }
                }
                out.println("  </g>");
            }

            out.println("</svg>");

        } catch (IOException e) {
        }
    }
}