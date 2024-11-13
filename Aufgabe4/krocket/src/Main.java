import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Main main = new Main();

        String input = null;
        try {
            // Read input from file
            input = Files.readString(Path.of("krocket/beispielaufgaben/krocket1.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Gate> gates = main.parseFile(input);

        double stepPercent = 10;
        for (int i = 0; i < 4; i++) {
            stepPercent /= 10;
            Gerade gerade = main.findRayIntersectAllGates(gates, stepPercent);

            if (gerade != null) {

                double x = gerade.getStart().getX() - 1;
                Point start = new Point(x, gerade.getPointAt(x).getY());

                System.out.println("Es ist möglich mit einem Schlag alle Tore zu durchqueren.");
                System.out.println("Der Schlag kann als Halbgerade mit dem Startpunkt: " + start + ", die durch den Punkt " + gerade.getDirectionPoint() + " verläuft, beschrieben werden.");
                System.out.println(gerade.getDirectionVec() + ", bzw. Steigung der Halbgeraden: " + gerade.getSlope());

                return;
            }
        }
        System.out.println(
                "Es ist nicht möglich, alle Tore in der richtigen Reihenfolge mit nur einem Schlag zu durchqueren.");
    }

    public Point calculateLineIntersectSegment(Gerade line, Gate segment) {
        double x0 = line.getStart().getX();
        double y0 = line.getStart().getY();
        double dx = line.getDirX();
        double dy = line.getDirY();
        double x1 = segment.getStart().getX();
        double y1 = segment.getStart().getY();
        double x2 = segment.getEnd().getX();
        double y2 = segment.getEnd().getY();

        // Berechne den Determinanten
        double denominator = dx * (y2 - y1) - dy * (x2 - x1);

        // Wenn der Determinant null ist, sind die Gerade und das Segment parallel
        if (Math.abs(denominator) < 1e-10) {
            return null;
        }

        // Berechne t und u
        double t = ((x1 - x0) * (y2 - y1) - (y1 - y0) * (x2 - x1)) / denominator;
        double u = ((x1 - x0) * dy - (y1 - y0) * dx) / denominator;

        // Überprüfe, ob der Schnittpunkt auf dem Segment liegt (0 <= u <= 1)
        if (u >= 0 && u <= 1) {
            return line.getPointAt(t);
        }
        return null;
    }

    public Gerade findRayIntersectAllGates(List<Gate> gates, double stepPercent) {
        // Wähle das Randsegment und das kürzeste Segment
        Gate edgeSegment = null;
        Gate shortestSegment = null;

        edgeSegment = gates.get(0);
        shortestSegment = gates.get(1);

        // Iteriere in kleinen Schritten entlang des Startsegments
        for (double stepStart = 0; stepStart <= 1; stepStart += stepPercent) {
            // Erstelle den Startpunkt auf dem Randsegment in `t`-Prozent der Länge
            Point startPoint = new Point(
                    edgeSegment.getStart().getX()
                            + stepStart * (edgeSegment.getEnd().getX() - edgeSegment.getStart().getX()),
                    edgeSegment.getStart().getY()
                            + stepStart * (edgeSegment.getEnd().getY() - edgeSegment.getStart().getY()));

            // Castet Halbgeraden in Richtung jedes
            for (double stepDir = 0; stepDir <= 1; stepDir += stepPercent) {
                Point directionPoint = new Point(
                        shortestSegment.getStart().getX()
                                + stepDir * (shortestSegment.getEnd().getX() - shortestSegment.getStart().getX()),
                        shortestSegment.getStart().getY()
                                + stepDir * (shortestSegment.getEnd().getY() - shortestSegment.getStart().getY()));
                Gerade gerade = new Gerade(startPoint, directionPoint);

                boolean intersectsAll = true;
                for (Gate gate : gates) {
                    // Intersection between Segment and line
                    Point intersectionSegmentGerade = calculateLineIntersectSegment(gerade, gate);
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

    public List<Gate> parseFile(String input) {
        List<Gate> gates = new ArrayList<>();
        // Save every line in Array of Strings
        String[] lines = input.split("\n");
        String[] firstLine = lines[0].trim().split(" ");
        double totalCountGates = Integer.parseInt(firstLine[0].trim());
        double ballRadius = Integer.parseInt(firstLine[1].trim());
        System.out.println("Ball Radius: " + ballRadius);

        // For every line split at space and save
        for (int i = 1; i <= totalCountGates; i++) {
            String[] parts = lines[i].trim().split(" ");
            Point one = new Point((double) Integer.parseInt(parts[0]), (double) Integer.parseInt(parts[1]));
            Point two = new Point((double) Integer.parseInt(parts[2]), (double) Integer.parseInt(parts[3]));

            Gate gate = new Gate(one, two);
            // Compensate for radius of ball
            gate.shrink(ballRadius);

            gates.add(gate);
        }
        return gates;
    }
}
