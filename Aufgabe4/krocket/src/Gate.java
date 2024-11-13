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

    public void shrink(double radius) {
        double length = length();
        // Normierungsfaktoren für die Verschiebung (Richtung vom Startpunkt zum
        // Endpunkt)
        double dx = (end.getX() - start.getX()) / length;
        double dy = (end.getY() - start.getY()) / length;

        start.moveBy(dx * radius, dy * radius);
        end.moveBy(-dx * radius, -dy * radius);
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
