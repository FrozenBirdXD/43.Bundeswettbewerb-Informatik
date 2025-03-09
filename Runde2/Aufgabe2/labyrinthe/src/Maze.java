public class Maze {
    private byte[][] maze;
    private int width, height;
    private int numHoles;
    private Agent agent;

    /*
     * 0 = Path
     * 1 = Wall
     * 2 = Hole
     * 8 = Agent
     */

    private class Agent {
        private int x, y;

        public Agent() {
            this.x = 0;
            this.y = 0;
            set(x * 2 + 1, y * 2 + 1, 8);
        }

        public boolean moveRight() {
            if (x == width) {
                System.out.println("Bounds");
                return false;
            }
            if (get(x * 2 + 2, y * 2 + 1) == 1) {
                System.out.println("Wall");
                return false;
            }
            set(x * 2 + 1, y * 2 + 1, 0);
            x++;
            set(x * 2 + 1, y * 2 + 1, 8);
            return true;
        }

        public boolean moveLeft() {
            if (x == 0) {
                System.out.println("Bounds");
                return false;
            }
            if (get(x * 2, y * 2 + 1) == 1) {
                System.out.println("Wall");
                return false;
            }
            set(x * 2 + 1, y * 2 + 1, 0);
            x--;
            set(x * 2 + 1, y * 2 + 1, 8);
            return true;
        }

        public boolean moveDown() {
            if (y == height) {
                System.out.println("Bounds");
                return false;
            }
            if (get(x * 2 + 1, y * 2 + 2) == 1) {
                System.out.println("Wall");
                return false;
            }
            set(x * 2 + 1, y * 2 + 1, 0);
            y++;
            set(x * 2 + 1, y * 2 + 1, 8);
            return true;
        }

        public boolean moveUp() {
            if (y == 0) {
                System.out.println("Bounds");
                return false;
            }
            if (get(x * 2 + 1, y * 2) == 1) {
                System.out.println("Wall");
                return false;
            }
            set(x * 2 + 1, y * 2 + 1, 0);
            y--;
            set(x * 2 + 1, y * 2 + 1, 8);
            return true;
        }

        public boolean reachedGoal() {
            if (x == width * 2 - 1 && y == height * 2 - 1) {
                return true;
            }
            return false;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }
    }

    public Maze(byte[][] maze, int width, int height) {
        this.maze = maze;
        this.width = width;
        this.height = height;
        this.numHoles = 0;
        this.agent = new Agent();

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

    public void moveDown() {
        agent.moveDown();
    }

    public void moveUp() {
        agent.moveUp();
    }

    public void moveLeft() {
        agent.moveLeft();
    }

    public void moveRight() {
        agent.moveRight();
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

    public int getGoalX() {
        return width - 1;
    }

    public int getGoalY() {
        return height - 1;
    }

    public int getNumHoles() {
        return this.numHoles;
    }

    public void setNumHoles(int numHoles) {
        this.numHoles = numHoles;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int y = 0; y < maze.length; y++) {
            for (int x = 0; x < maze[y].length; x++) {
                byte cell = maze[y][x];
                if (cell == 0) {
                    if (x % 2 == 1 && y % 2 == 1) {
                        if (x == width * 2 - 1 && y == height * 2 - 1) {
                            // X for goal
                            builder.append("\u001B[1mx\u001B[0m");
                        } else {
                            // Green for uneven coordinates
                            builder.append("\u001B[1m\u001B[32m0\u001B[0m");
                        }
                    } else {
                        builder.append(".");
                    }
                } else if (cell == 1) {
                    // Red for 1
                    builder.append("\u001B[31m█\u001B[0m");
                } else if (cell == 8) {
                    builder.append("\u001B[1ma\u001B[0m");
                } else if (cell == 2) {
                    builder.append("\u001B[1mh\u001B[0m");
                } else {
                    builder.append(cell);
                }

                // Add a wall if there are two walls next to each other but separated by a space
                if (x < maze[y].length - 1) {
                    byte nextCell = maze[y][x + 1];
                    if (cell == 1 && nextCell == 1) {
                        builder.append("\u001B[31m█\u001B[0m"); // Add a wall between two walls
                    } else {
                        builder.append(" ");
                    }
                }
            }
            builder.append("\n");
        }
        return builder.toString();
    }
}