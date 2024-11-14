// Represent Krocket gate with a start and end point
public class Gate {
    private Point start;
    private Point end;

    Gate(Point start, Point end) {
        this.start = start;
        this.end = end;
    }

    public double length() {
        double dx = end.getX() - start.getX();
        double dy = end.getY() - start.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public String toString() {
        return "[" + start + "; " + end + "]";
    }

    public Point getStart() {
        return this.start;
    }

    public Point getEnd() {
        return this.end;
    }
}
