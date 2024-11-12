public class Gerade {
    private Point start;
    private double directionX;
    private double directionY;
    private Point directionPoint;

    // Konstruktor: Erstellt eine Gerade von einem Startpunkt in Richtung eines
    // Richtungspunkts
    Gerade(Point start, Point directionPoint) {
        this.directionPoint = directionPoint;
        this.start = start;

        // Berechne den Richtungsvektor von start zu directionPoint
        this.directionX = directionPoint.getX() - start.getX();
        this.directionY = directionPoint.getY() - start.getY();

        // Normalisiere den Richtungsvektor (optional, für Einheitlänge)
        // double length = Math.sqrt(directionX * directionX + directionY * directionY);
        // this.directionX /= length;
        // this.directionY /= length;
    }

    // Methode, um einen Punkt auf der Gerade zu berechnen (für beliebiges t)
    public Point getPointAt(double t) {
        double x = start.getX() + t * directionX;
        double y = start.getY() + t * directionY;
        return new Point(x, y);
    }

    // Optional: Methode, um die Gerade als String darzustellen
    @Override
    public String toString() {
        return "Line Start: (" + start + "), Direction: (" + directionX + ", " + directionY + ")";
    }

    public double getDirX() {
        return this.directionX;
    }

    public double getDirY() {
        return this.directionY;
    }

    public Point getStart() {
        return this.start;
    }

    public Point getDirectionPoint() {
        return this.directionPoint;
    }
}