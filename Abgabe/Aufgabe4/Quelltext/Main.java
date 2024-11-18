import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private double ballRadius;

    public static void main(String[] args) {
        Main main = new Main();

        String input = null;
        try {
            // Read input from file
            //
            //
            //          Input file path here:           //
            //
            //
            input = Files.readString(Path.of("./Quelltext/beispielaufgaben/krocket5.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Gate> gates = main.parseFile(input);

        double stepPercent = 10;
        // First try smaller steps
        for (int i = 0; i < 5; i++) {
            stepPercent /= 10;
            Line line = main.findRayIntersectAllGates(gates, stepPercent, main.getBallRadius());

            if (line != null) {
                System.out.println("Es ist möglich mit einem Schlag alle Tore zu durchqueren.");
                System.out.println("Der Schlag kann als Halbgerade mit dem Startpunkt: " + line.getStart()
                        + ", die durch den Punkt " + line.getDirectionPoint() + " verläuft, beschrieben werden.");
                System.out.println(line.getDirectionVec() + ", bzw. Steigung der Halbgeraden: " + line.getSlope());

                return;
            }
        }
        System.out.println(
                "Es ist nicht möglich, alle Tore in der richtigen Reihenfolge mit nur einem Schlag zu durchqueren.");
    }

    /**
     * Calculates point of intersection of a line and a segment (krocket gate)
     * 
     * @param line - line to describe possible path of krocket ball
     * @param gate - Krocketgate (segment)
     * @return Point - Point of intersection; null if no intersection exists
     */
    public Point calculateLineIntersectSegment(Line line, Gate gate, double ballRadius) {
        double lineStartX = line.getStart().getX();
        double lineStartY = line.getStart().getY();
        double dx = line.getDeltaX();
        double dy = line.getDeltaY();
        double gateStartX = gate.getStart().getX();
        double gateStartY = gate.getStart().getY();
        double gateEndX = gate.getEnd().getX();
        double gateEndY = gate.getEnd().getY();

        // More info in the documentation of this task

        // To calculate the intersection the equation of the line and gate have to be
        // equated
        // A system of linear equation can be set up for that
        // Then solve for unknown (u & t)
        // Which can be interpreted as a 2x2 matrix

        // Calculate determinant of equation system
        double denominator = dx * (gateEndY - gateStartY) - dy * (gateEndX - gateStartX);

        // If determinant is (almost) zero, the line and gate (segment) are parallel or
        // colinear
        // Therefore they cant have an intersection
        if (Math.abs(denominator) < 1e-10) {
            return null;
        }

        // Solve the system of equations
        // (I used gauss algorithm)
        double t = ((gateStartX - lineStartX) * (gateEndY - gateStartY)
                - (gateStartY - lineStartY) * (gateEndX - gateStartX)) / denominator;
        double u = ((gateStartX - lineStartX) * dy - (gateStartY - lineStartY) * dx) / denominator;

        // Check if the intersection point is outside of the gate segment 
        if (u < 0 || u > 1) {
            return null;
        }

        // Calculate the intersection point on the line
        Point intersectionPoint = line.getPointAt(t);

        // Check if the line intersects circle (collision boundary of start and endpoint
        // of gate)
        if (lineIntersectsCircle(line, gate.getStart(), ballRadius) ||
                lineIntersectsCircle(line, gate.getEnd(), ballRadius)) {
            return null; // Intersection invalid due to collision with gate endpoints
        }

        // Valid intersection point with enough space to gate endpoints
        return intersectionPoint;
    }

    /**
     * Calculates a line that intersects all given gates
     * The line intersects the starting point (placed on the first gate)
     * Endpoint (direction of the line) for calculation of the line is on the second
     * gate
     * 
     * @param gates    - List of gates the line has to intersect
     * @param stepSize - Step size (0 to 1) percentage of the first gate's
     *                 length, specifies how finely the starting points
     *                 are iterated along the first gate
     * @return Line - line that intersects all gates, null if no intersection
     */
    public Line findRayIntersectAllGates(List<Gate> gates, double stepSize, double ballRadius) {
        Gate firstGate = gates.get(0);
        Gate secondGate = gates.get(1);

        // Iterate over positions on first gate in increments defined by step size
        // For every startpoint on the first gate, the loop is executed
        for (double stepStart = 0; stepStart <= 1; stepStart += stepSize) {
            // Create start point for the line on the first gate
            Point startPoint = new Point(
                    firstGate.getStart().getX()
                            + stepStart * (firstGate.getEnd().getX() - firstGate.getStart().getX()),
                    firstGate.getStart().getY()
                            + stepStart * (firstGate.getEnd().getY() - firstGate.getStart().getY()));

            // Iterate over positions on second gate in increments defined by step size
            for (double stepDir = 0; stepDir <= 1; stepDir += stepSize) {
                Point directionPoint = new Point(
                        secondGate.getStart().getX()
                                + stepDir * (secondGate.getEnd().getX() - secondGate.getStart().getX()),
                        secondGate.getStart().getY()
                                + stepDir * (secondGate.getEnd().getY() - secondGate.getStart().getY()));
                // Create line, that intersects startpoint and directionpoint
                Line gerade = new Line(startPoint, directionPoint);

                boolean intersectsAll = true;
                // Checks if line intersects all gates
                for (Gate gate : gates) {
                    // Intersection between Segment and line
                    Point intersectionSegmentGerade = calculateLineIntersectSegment(gerade, gate, ballRadius);
                    // Does not intersect
                    if (intersectionSegmentGerade == null) {
                        intersectsAll = false;
                        break;
                    }
                }
                if (intersectsAll) {
                    return gerade;
                }
            }
        }
        return null;
    }

    /**
     * @param input - Contents of file to read as a String
     * @return List<Gate> - List of Krocketgates, compensated under consideration of
     *         ball radius
     */
    public List<Gate> parseFile(String input) {
        List<Gate> gates = new ArrayList<>();
        // Save every line in Array of Strings
        String[] lines = input.split("\n");
        String[] firstLine = lines[0].trim().split(" ");
        double totalCountGates = Integer.parseInt(firstLine[0].trim());
        this.ballRadius = Integer.parseInt(firstLine[1].trim());

        // For every line split at space and save
        for (int i = 1; i <= totalCountGates; i++) {
            String[] parts = lines[i].trim().split(" ");
            Point one = new Point((double) Integer.parseInt(parts[0]), (double) Integer.parseInt(parts[1]));
            Point two = new Point((double) Integer.parseInt(parts[2]), (double) Integer.parseInt(parts[3]));

            Gate gate = new Gate(one, two);
            // Compensate for radius of ball
            // gate.shrink(ballRadius);

            gates.add(gate);
        }
        return gates;
    }

    public double getBallRadius() {
        return this.ballRadius;
    }

    /**
     * Check if a line intersects a circle
     * 
     * @param line         - line
     * @param circleCenter - center of the circle as a Point
     * @param radius       - radius of circle as double
     * @return true if the line intersects the circle, else false
     */
    private boolean lineIntersectsCircle(Line line, Point circleCenter, double radius) {
        double cx = circleCenter.getX();
        double cy = circleCenter.getY();
        double x1 = line.getStart().getX();
        double y1 = line.getStart().getY();
        double dx = line.getDeltaX();
        double dy = line.getDeltaY();

        // More info in the docs of this task
        // circle equation: (x - cx)^2 + (y - cy)^2=r^2
        // line equation: x = x1 + t * dx
        // &&
        // line equation: y = y1 + t * dy
        // Short: plug line equation into circle equation and solve for t
        double a = dx * dx + dy * dy;
        double b = 2 * (dx * (x1 - cx) + dy * (y1 - cy));
        double c = (x1 - cx) * (x1 - cx) + (y1 - cy) * (y1 - cy) - radius * radius;

        // Solve quadratic equation a*t^2 + b*t + c = 0 and just check discriminant
        // If discriminant < 0: no intersection
        // = 0: one intersection = line is a tangent
        // > 0: two intersections
        double discriminant = b * b - 4 * a * c;

        if (discriminant <= 0) {
            return false; // No real number
        } else {
            return true;
        }
    }
}
