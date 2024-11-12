class Ray {
    private Point start;
    private double directionX; 
    private double directionY;

    // Konstruktor: Erstellt eine Halbgerade mit einem Startpunkt und einer Richtung
    // TODO: Richtungsvektor normieren extra

    Ray(Point start, Point directionPoint) {
        this.start = start;
        this.directionX = directionPoint.getX() - start.getX();
        this.directionY = directionPoint.getY() - start.getY();
    }

    @Override
    public String toString() {
        return start + " DirX: " + directionX + " DirY: " + directionY;
    }

    public Point getStart() {
        return this.start;
    }

    public double getDirX() {
        return this.directionX;
    }

    public double getDirY() {
        return this.directionY;
    }
}