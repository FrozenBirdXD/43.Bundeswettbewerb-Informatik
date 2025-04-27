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

    // Change the method signature and add path drawing logic
    public void saveMazeAsSvg(String filename, int cellSize, List<BFS.State> path, int playerIndex /* 1 or 2 */ ) {
        // Or use AStar.State if calling from AStar
        // Consider creating a common State interface or record if mixing isn't desired.

        int svgWidth = getArrayWidth() * cellSize;
        int svgHeight = getArrayHeight() * cellSize;

        try (PrintWriter out = new PrintWriter(new FileWriter(filename))) {
            // SVG Header
            out.println("<svg width=\"" + svgWidth + "\" height=\"" + svgHeight
                    + "\" xmlns=\"http://www.w3.org/2000/svg\" "
                    + "xmlns:xlink=\"http://www.w3.org/1999/xlink\">"); // Added xlink namespace

            // --- Draw Maze Background (Walls, Cells, Pits) ---
            // Optional: Hintergrund
            out.println(
                    "  <rect x=\"0\" y=\"0\" width=\"" + svgWidth + "\" height=\"" + svgHeight + "\" fill=\"white\"/>");

            // Zeichne die Labyrinthzellen als Rechtecke
            for (int y = 0; y < getArrayHeight(); y++) {
                for (int x = 0; x < getArrayWidth(); x++) {
                    byte cell = get(x, y);
                    String color = "magenta"; // Default für Fehler
                    boolean drawRect = true; // Flag to control drawing

                    switch (cell) {
                        case 0: // Pfad/Zwischenraum
                            if (x % 2 == 1 && y % 2 == 1) { // Logische Zelle
                                if (x == getMovableWidth() * 2 - 1 && y == getMovableHeight() * 2 - 1) {
                                    color = "lime"; // Ziel (hellgrün)
                                } else {
                                    color = "#d3d3d3"; // Pfad-Zelle (hellgrau)
                                }
                            } else {
                                color = "white"; // Zwischenraum
                                drawRect = false; // Don't draw white rects over white background
                            }
                            break;
                        case 1: // Wand
                            color = "black";
                            break;
                        // Don't draw agent start position directly here, we'll add markers later
                        // case 8: // Agent (Startposition) color = "red"; break;
                        case 2: // Loch
                            color = "blue";
                            break;
                        default: // Also handle agent start if marked differently (e.g., 8)
                            if (cell == 8 && x == 1 && y == 1) { // If start point is marked
                                color = "#d3d3d3"; // Draw start cell as normal path
                            } else {
                                // Keep magenta for unexpected values or leave white
                                color = "white";
                                drawRect = false;
                            }
                            break;

                    }

                    // Zeichne nur, wenn nicht weiß (oder explizit gewünscht)
                    if (drawRect) {
                        out.println("  <rect x=\"" + (x * cellSize) + "\" y=\"" + (y * cellSize) +
                                "\" width=\"" + cellSize + "\" height=\"" + cellSize +
                                "\" fill=\"" + color + "\"/>");
                    }
                }
            }

            // --- Draw Special Markers (Start/Goal) ON TOP ---
            // Draw Start Marker (e.g., a red circle)
            double startCenterX = (1 * cellSize + cellSize / 2.0); // Start is always at logical (0,0) -> array (1,1)
            double startCenterY = (1 * cellSize + cellSize / 2.0);
            out.println("  <circle cx=\"" + startCenterX + "\" cy=\"" + startCenterY +
                    "\" r=\"" + (cellSize / 2.5) + "\" fill=\"red\" stroke=\"black\" stroke-width=\"1\"/>");

            // Draw Goal Marker (e.g., a green square inside the lime cell)
            double goalX = (getMovableWidth() * 2 - 1);
            double goalY = (getMovableHeight() * 2 - 1);
            out.println("  <rect x=\"" + (goalX * cellSize + cellSize / 4.0) + "\" y=\""
                    + (goalY * cellSize + cellSize / 4.0) +
                    "\" width=\"" + (cellSize / 2.0) + "\" height=\"" + (cellSize / 2.0) +
                    "\" fill=\"darkgreen\"/>");

            // --- Draw the Path ON TOP ---
            if (path != null && path.size() > 1) {
                // Define path style
                String pathStrokeColor = (playerIndex == 1) ? "orange" : "cyan"; // Different colors per player
                double pathStrokeWidth = Math.max(1.0, cellSize / 5.0); // Adjust thickness
                double pathOpacity = 0.75; // Make it slightly transparent

                out.println("  <g id=\"path-player" + playerIndex + "\" stroke=\"" + pathStrokeColor
                        + "\" stroke-width=\"" + pathStrokeWidth + "\" stroke-opacity=\"" + pathOpacity
                        + "\" stroke-linecap=\"round\" fill=\"none\">");

                for (int i = 0; i < path.size() - 1; i++) {
                    BFS.State s1 = path.get(i); // Or AStar.State
                    BFS.State s2 = path.get(i + 1);

                    // Get logical coordinates for the correct player
                    int logX1 = (playerIndex == 1) ? s1.x1() : s1.x2();
                    int logY1 = (playerIndex == 1) ? s1.y1() : s1.y2();
                    int logX2 = (playerIndex == 1) ? s2.x1() : s2.x2();
                    int logY2 = (playerIndex == 1) ? s2.y1() : s2.y2();

                    // Convert logical coordinates to array coordinates (center of cell)
                    double ax1 = (logX1 * 2 + 1);
                    double ay1 = (logY1 * 2 + 1);
                    double ax2 = (logX2 * 2 + 1);
                    double ay2 = (logY2 * 2 + 1);

                    // Convert array coordinates to SVG center coordinates
                    double svgCX1 = ax1 * cellSize + cellSize / 2.0;
                    double svgCY1 = ay1 * cellSize + cellSize / 2.0;
                    double svgCX2 = ax2 * cellSize + cellSize / 2.0;
                    double svgCY2 = ay2 * cellSize + cellSize / 2.0;

                    // Draw line segment
                    // Avoid drawing line if player didn't move (hit wall/pit reset handled
                    // implicitly)
                    if (logX1 != logX2 || logY1 != logY2) {
                        out.println("    <line x1=\"" + svgCX1 + "\" y1=\"" + svgCY1
                                + "\" x2=\"" + svgCX2 + "\" y2=\"" + svgCY2 + "\"/>");
                    } else {
                        // Optional: draw a small dot if the player didn't move?
                        // out.println(" <circle cx=\"" + svgCX1 + "\" cy=\"" + svgCY1 + "\" r=\"" +
                        // pathStrokeWidth/3.0 + "\" fill=\"" + pathStrokeColor + "\" stroke=\"none\"
                        // />");
                    }
                }
                out.println("  </g>"); // End path group
            }

            // SVG Footer
            out.println("</svg>");
            System.out.println("Labyrinth als SVG gespeichert in: " + filename);

        } catch (IOException e) {
            System.err.println("Fehler beim Speichern der Labyrinth-SVG-Datei: " + e.getMessage());
        }
    }
}