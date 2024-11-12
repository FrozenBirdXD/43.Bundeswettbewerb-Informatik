import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private int ballRadius;
    private int totalCountGates;
    private Point leftMostIntersection;

    public static void main(String[] args) {
        Main main = new Main();

        String input = null;
        try {
            // Read input from file
            input = Files.readString(Path.of("krocket/beispielaufgaben/krocket3.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Gate> gates = main.parseFile(input);

        // TODO: First try big step percentages, if not found try smaller until

        // TODO: Use Ball radius

        System.out.println("Shortest Gate: " + main.findShortestSegment(gates));
        System.out.println("Edge Segment: " + main.findEdgeSegment(gates));

        Gerade gerade = main.findRayIntersectAllGates(gates, 0.01);
        Ray ray = main.getKrocketRay(gerade);

        System.out.println("Left most intersection: " + main.getLeftMostIntersection());

        if (ray == null) {
            System.out.println(
                    "Es ist nicht möglich, alle Tore mit nur einem Schlag zu durchqueren.");
        } else {
            System.out.println("Es war möglich mit folgendem Ray: " + ray);
        }
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

    public Ray getKrocketRay(Gerade gerade) {
        if (gerade == null) {
            return null;
        }
        // -1 to not start inside of a gate
        double x = this.leftMostIntersection.getX() - 1;
        return new Ray(new Point(x, gerade.getPointAt(x).getY()), gerade.getDirectionPoint());
    }

    public Gerade findRayIntersectAllGates(List<Gate> gates, double stepPercent) {
        // Wähle das Randsegment und das kürzeste Segment
        Gate edgeSegment = null;
        Gate shortestSegment = null;
        if (gates.size() == 2) {
            edgeSegment = gates.get(0);
            shortestSegment = gates.get(1);
        } else {
            edgeSegment = findEdgeSegment(gates);
            shortestSegment = findShortestSegment(gates);
        }

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
                // Ray ray = new Ray(startPoint, directionPoint);
                Gerade gerade = new Gerade(startPoint, directionPoint);

                boolean intersectsAll = true;
                for (Gate gate : gates) {
                    // Intersection between Segment and line
                    Point intersectionSegmentGerade = calculateLineIntersectSegment(gerade, gate);
                    if (intersectionSegmentGerade == null) {
                        intersectsAll = false;
                        break;
                    }
                    // Update left most intersection
                    if (this.leftMostIntersection == null
                            || intersectionSegmentGerade.getX() < this.leftMostIntersection.getX()) {
                        this.leftMostIntersection = intersectionSegmentGerade;
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
        this.totalCountGates = Integer.parseInt(firstLine[0].trim());
        this.ballRadius = Integer.parseInt(firstLine[1].trim());

        // For every line split at space and save
        for (int i = 1; i <= totalCountGates; i++) {
            String[] parts = lines[i].trim().split(" ");
            Point one = new Point((double) Integer.parseInt(parts[0]), (double) Integer.parseInt(parts[1]));
            Point two = new Point((double) Integer.parseInt(parts[2]), (double) Integer.parseInt(parts[3]));
            gates.add(new Gate(one, two));
        }
        return gates;
    }

    // TODO: Not necessary

    public Gate findEdgeSegment(List<Gate> gates) {
        // Initialize bounding box values
        double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;

        // Find bounding box
        for (Gate segment : gates) {
            minX = Math.min(minX, Math.min(segment.getStart().getX(), segment.getEnd().getX()));
            maxX = Math.max(maxX, Math.max(segment.getStart().getX(), segment.getEnd().getX()));
            minY = Math.min(minY, Math.min(segment.getStart().getY(), segment.getEnd().getY()));
            maxY = Math.max(maxY, Math.max(segment.getStart().getY(), segment.getEnd().getY()));
        }

        Gate seg = null;
        // Identify segments that touch the bounding box
        for (Gate segment : gates) {
            if (segment.getStart().getX() == minX || segment.getStart().getX() == maxX ||
                    segment.getStart().getY() == minY || segment.getStart().getY() == maxY ||
                    segment.getEnd().getX() == minX || segment.getEnd().getX() == maxX ||
                    segment.getEnd().getY() == minY || segment.getEnd().getY() == maxY) {
                // Return the first segment found that touches the bounding box
                seg = segment;
            }
        }
        return seg;
    }

    public Gate findShortestSegment(List<Gate> segments) {
        Gate shortestSegment = segments.get(0);
        double minLength = shortestSegment.length();

        for (Gate segment : segments) {
            double length = segment.length();
            if (length < minLength) {
                minLength = length;
                shortestSegment = segment;
            }
        }
        return shortestSegment;
    }

    public Point getLeftMostIntersection() {
        return this.leftMostIntersection;
    }
}
