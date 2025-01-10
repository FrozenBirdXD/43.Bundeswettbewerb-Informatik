import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        String input = null;
        try {
            // Read input from file
            input = Files.readString(Path.of("beispielaufgaben/grabmal1.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        int[] blocks = parseInput(input);
        List<String> instructions = findPath2(blocks);

        // Anweisungen ausgeben
        for (String instruction : instructions) {
            System.out.println(instruction);
        }

        System.out
                .println("Ausführungszeit in Sekunden: " + (double) (System.currentTimeMillis() - start) / (long) 1000);
    }

    public static int[] parseInput(String input) {
        // List<Integer> blocks = new ArrayList<>();
        // Save every line in Array of Strings
        String[] lines = input.split("\n");
        int totalCountBlocks = Integer.parseInt(lines[0].trim());
        int[] blocks = new int[totalCountBlocks];

        // For every line split at space and save
        for (int i = 1; i <= totalCountBlocks; i++) {
            // blocks.add(Integer.parseInt(lines[i].trim()));
            blocks[i - 1] = Integer.parseInt(lines[i].trim());
        }
        return blocks;
    }

    // Methode, um festzustellen, ob ein Quader zur gegebenen Zeit frei ist
    private static boolean isFree(int time, int period) {
        return (time / period) % 2 == 1;
    }

    // Methode, die die Anweisungen zum Durchqueren des Ganges generiert
    public static List<String> findPath(int[] periods) {
        List<String> instructions = new ArrayList<>();
        int time = 0; // Zeit in Minuten
        int position = 0; // Startposition am Anfang des Gangs

        // Solange Petra noch nicht am Grabmal angekommen ist
        while (position < periods.length) {
            // Prüfen, ob der aktuelle Abschnitt und der nächste Abschnitt gleichzeitig frei
            // sind
            while (!(isFree(time, periods[position]) &&
                    (position + 1 >= periods.length || isFree(time, periods[position + 1])))) {
                time++; // Zeit weiterlaufen lassen
            }

            // Instruktionen zum Warten und Laufen hinzufügen
            if (position == 0) {
                instructions.add("Warte " + time + " Minuten");
            } else {
                if (position < periods.length - 1) {
                    int previousWait = Integer.parseInt(instructions.get(instructions.size() - 2).split(" ")[1]);
                    instructions.add("Warte " + (time - previousWait) + " Minuten");
                }
            }

            if (position < periods.length - 1) {
                instructions.add("Laufe in den Abschnitt " + (position + 2));
            }
            position++;
        }

        // Letzte Anweisung, um das Grabmal zu erreichen
        instructions.add("Laufe zum Grabmal");

        return instructions;
    }

    public static List<String> findPath2(int[] periods) {
        List<String> instructions = new ArrayList<>();
        int time = 0; // Zeit in Minuten
        int position = 0; // Startposition am Anfang des Gangs

        // Solange Petra noch nicht am Grabmal angekommen ist
        while (position < periods.length) {
            // Suche nach dem frühesten Zeitpunkt, an dem beide benachbarten Abschnitte
            // freigegeben sind
            int nextTime = time;
            while (!(isFree(nextTime, periods[position]) &&
                    (position + 1 >= periods.length || isFree(nextTime, periods[position + 1])))) {
                nextTime++; // Erhöhe den Zeitpunkt, bis beide Abschnitte frei sind
            }

            // Instruktionen zum Warten und Laufen hinzufügen
            int waitTime = nextTime - time; // Berechne die Wartezeit bis zum nächsten freien Zeitpunkt
            if (position == 0) {
                instructions.add("Warte " + waitTime + " Minuten");
            } else {
                int previousWait = Integer.parseInt(instructions.get(instructions.size() - 2).split(" ")[1]);
                instructions.add("Warte " + (waitTime) + " Minuten");
            }

            instructions.add("Laufe in den Abschnitt " + (position + 2));
            time = nextTime; // Setze die aktuelle Zeit auf den neuen Zeitpunkt
            position++; // Gehe zum nächsten Abschnitt
        }

        // Letzte Anweisung, um das Grabmal zu erreichen
        instructions.add("Laufe zum Grabmal");

        return instructions;
    }
}
