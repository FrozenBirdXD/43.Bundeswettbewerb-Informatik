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
    }

    // Methode, um einen Punkt auf der Gerade zu berechnen (für beliebiges t)
    public Point getPointAt(double x) {
        double t = start.getY() - directionY / directionX * start.getX();
        return new Point(x, directionY / directionX * x + t);
    }

    public double getSlope() {
        return directionY / directionX;
    }

    public String getDirectionVec() {
        // Berechnung der Länge des Richtungsvektors
        double length = Math.sqrt(directionX * directionX + directionY * directionY);
        double normedX = directionX / length;
        double normedY = directionY / length;

        return "Normierter Richtungsvektor: (" + normedX + ", " + normedY + ")";
    }

    // Optional: Methode, um die Gerade als String darzustellen
    @Override
    public String toString() {
        return "Line Start: (" + start + "), Direction: (" + directionX + ", " + directionY + "), Direction Point: "
                + directionPoint;
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