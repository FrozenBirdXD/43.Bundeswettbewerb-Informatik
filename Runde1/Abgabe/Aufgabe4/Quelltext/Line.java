// Represents a line that intersects points "start" and "directionPoint"
public class Line {
    private Point start;
    private Point directionPoint;
    private double deltaX;
    private double deltaY;

    Line(Point start, Point directionPoint) {
        this.directionPoint = directionPoint;
        this.start = start;

        this.deltaX = directionPoint.getX() - start.getX();
        this.deltaY = directionPoint.getY() - start.getY();
    }

    /**
     * Calculate Point on specific x coordinate on line
     * 
     * @param x - x coordinate of point
     * @return Point - point on line with coord x
     */
    public Point getPointAt(double x) {
        double t = start.getY() - deltaY / deltaX * start.getX();
        return new Point(x, deltaY / deltaX * x + t);
    }

    public double getSlope() {
        return deltaY / deltaX;
    }

    public double getYIntercerpt() {
        return start.getY() - getSlope() * start.getX();
    }

    public String getDirectionVec() {
        // Length of direction vector
        double length = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        double normedX = deltaX / length;
        double normedY = deltaY / length;

        return "Normierter Richtungsvektor: (" + normedX + ", " + normedY + ")";
    }

    @Override
    public String toString() {
        return "Line Start: (" + start + "), Direction: (" + deltaX + ", " + deltaY + "), Direction Point: "
                + directionPoint;
    }

    public double getDeltaX() {
        return this.deltaX;
    }

    public double getDeltaY() {
        return this.deltaY;
    }

    public Point getStart() {
        return this.start;
    }

    public Point getDirectionPoint() {
        return this.directionPoint;
    }
}