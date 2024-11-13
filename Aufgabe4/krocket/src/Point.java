public class Point {
    private double x;
    private double y;

    Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void moveBy(double dx, double dy) {
        this.x += dx;
        this.y += dy;
    }

    @Override
    public String toString() {
        return "(" + x + "|" + y + ")";
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }
}
