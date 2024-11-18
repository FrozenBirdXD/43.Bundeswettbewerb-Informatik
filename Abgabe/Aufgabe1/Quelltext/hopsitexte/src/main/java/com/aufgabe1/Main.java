package com.aufgabe1;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

//
//
// Requires JavaFX JDK and Java JDK
//
//

public class Main extends Application {
    private Set<Character> lowerCase = new HashSet<>(
            Arrays.asList('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                    'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'));
    private Set<Character> upperCase = new HashSet<>(
            Arrays.asList('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
                    'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'));

    private static Scene scene;
    private final TextArea textArea = new TextArea();
    private TextFlow textFlow = new TextFlow();
    private final Text output = new Text("Ist dein Text ein Hopsitext? Tippe in das Textfeld:)");

    @SuppressWarnings("exports")
    @Override
    public void start(Stage stage) throws IOException {
        // Setup
        stage.setTitle("Hopsitext Ersteller");
        textArea.setWrapText(true);
        output.setFont(Font.font("System", FontWeight.BOLD, FontPosture.REGULAR, 18));

        // ChangeListener for the textArea textProperty
        textArea.textProperty()
                .addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                    // Called every time the text is changed
                    processText(newValue);
                });

        // Create SplitPane
        SplitPane splitPane = new SplitPane();
        ScrollPane scrollPane = new ScrollPane(textFlow);
        splitPane.getItems().addAll(
                textArea, scrollPane);

        textFlow.setPrefWidth(600);
        // Set up the layout
        BorderPane layout = new BorderPane();
        layout.setCenter(splitPane);
        layout.setTop(output);

        // Create Scene
        scene = new Scene(layout, 600, 400);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Receives Text input from the use and displays the processed version with
     * correct colors for the jumps
     * 
     * @param input - Text from the input field
     */
    private void processText(String input) {
        textFlow.getChildren().clear();

        if (input.isEmpty()) {
            output.setText("Ist dein Text ein Hopsitext? Tippe in das Textfeld :)");
        } else if (input.length() == 1) {
            textFlow.getChildren().add(new Text(input));
            output.setText("Zu kurz um ein Hopsitext zu sein!");
        } else {
            textFlow.getChildren()
                    .addAll(mergeTextFlows(jump(input, 0, Color.RED), jump(input, 1, Color.BLUE)).getChildren());
        }
    }

    /**
     * Merges two TextFlows into one with merged colors of two players. Jump
     * collisions are colored purple
     * 
     * @param tf1 - first player jumps
     * @param tf2 - second player jumps
     * @return TextFlow
     */
    private TextFlow mergeTextFlows(TextFlow tf1, TextFlow tf2) {
        TextFlow result = new TextFlow();
        int count = 0;

        // Loops over every character
        for (int i = 0; i < tf1.getChildren().size(); i++) {
            Text text1 = (Text) tf1.getChildren().get(i);
            Text text2 = (Text) tf2.getChildren().get(i);

            Color color1 = (Color) text1.getFill();
            Color color2 = (Color) text2.getFill();

            Text mergedText = new Text(text1.getText());

            // Result Color of current character
            Color finalColor = determineColor(color1, color2);
            if (finalColor.equals(Color.PURPLE)) {
                output.setText("Dieser Text ist leider noch kein Hopsitext :(");
                count = 1;
            } else {
                if (count == 1) {
                    output.setText("Dieser Text ist leider noch kein Hopsitext :(");
                } else {
                    output.setText("Dieser Text ist ein Hopsitext!");
                }
            }
            mergedText.setFill(finalColor);

            // Display result to user
            result.getChildren().add(mergedText);
        }
        return result;
    }

    /**
     * Determines the result color based on two input colors
     * 
     * @param color1
     * @param color2
     * @return Color
     */
    private Color determineColor(Color color1, Color color2) {
        if (color1.equals(Color.BLACK) && color2.equals(Color.BLACK)) {
            return Color.BLACK;
        } else if (color1.equals(Color.RED) && color2.equals(Color.BLACK)) {
            return Color.RED;
        } else if (color1.equals(Color.BLACK) && color2.equals(Color.BLUE)) {
            return Color.BLUE;
        } else if (color1.equals(Color.RED) && color2.equals(Color.BLUE)) {
            return Color.PURPLE;
        } else if (color1.equals(Color.RED)) {
            return Color.RED;
        } else if (color2.equals(Color.BLUE)) {
            return Color.BLUE;
        }
        return Color.BLACK;
    }

    /**
     * Simulates Hopsitext-jumps
     * 
     * @param input      - Inputtext as String
     * @param startIndex - Starting Position of Player
     * @param color      - Color of Player
     * @return TextFlow
     */
    private TextFlow jump(String input, int startIndex, Color color) {
        // Textflow is a collection of textnodes
        TextFlow tf = new TextFlow();
        int nextIndex = 0;
        char currentChar = ' ';
        // Textnode is a single character, because characters need to be colored
        // individually
        Text textNode = new Text();

        // Skip over first non-alphabetic characters
        int i = 0;
        for (; i < input.length(); i++) {
            if (getAlphabetPosition(input.charAt(nextIndex)) == -1) {
                textNode = new Text(String.valueOf(input.charAt(nextIndex)));
                tf.getChildren().add(textNode);
                nextIndex++;
            } else {
                break;
            }
        }
        nextIndex += startIndex;

        if (nextIndex == (1 + i) && !input.isEmpty()) {
            textNode = new Text(String.valueOf(input.charAt(0)));
            tf.getChildren().add(textNode);
        }

        // Main loop
        while (nextIndex < input.length()) {
            currentChar = input.charAt(nextIndex);
            textNode = new Text(String.valueOf(currentChar));

            // Character that needs to be colored, beause jump starts on this char
            if (getAlphabetPosition(currentChar) != -1) {
                // Display character in specified color
                textNode.setFill(color);
                tf.getChildren().add(textNode);

                int alphabetPosition = getAlphabetPosition(currentChar);
                int count = 0;

                // Skip non-alphabetic characters and move forward by the alphabet position
                while (count < alphabetPosition) {
                    nextIndex++;
                    if (nextIndex >= input.length()) {
                        break;
                    }
                    // If Character is an alphabetic character
                    if (getAlphabetPosition(input.charAt(nextIndex)) != -1) {
                        count++;
                        // Add uncolored character
                        if (count != alphabetPosition) {
                            textNode = new Text(String.valueOf(input.charAt(nextIndex)));
                            tf.getChildren().add(textNode);
                        }
                        // If Character is non alphabetic -> skip it and don't increment loop counter
                    } else {
                        // Add uncolored character
                        textNode = new Text(String.valueOf(input.charAt(nextIndex)));
                        tf.getChildren().add(textNode);
                    }
                }
            } else {
                textNode = new Text(String.valueOf(input.charAt(nextIndex)));
                // textNode.setFill(color);
                System.out.println("If this gets executed, then there is something wrong");
                tf.getChildren().add(textNode);
                nextIndex++;
            }
        }
        return tf;
    }

    /**
     * Return the position in the alphabet of the character. Upper and lowercase
     * characters are treated the same. German "Umlaute" also return a valid
     * position. Everything else returns -1
     * 
     * @param c - Character to check
     * @return int - Position in the alphabet
     */
    private int getAlphabetPosition(char c) {
        switch (c) {
            case 'ä':
            case 'Ä':
                return 27;
            case 'ö':
            case 'Ö':
                return 28;
            case 'ü':
            case 'Ü':
                return 29;
            case 'ß':
                return 30;
            default:
                break;
        }

        if (Character.isUpperCase(c) && upperCase.contains(c)) {
            return c - 'A' + 1; // A=1, B=2, ..., Z=26
        } else if (Character.isLowerCase(c) && lowerCase.contains(c)) {
            return c - 'a' + 1; // a=1, b=2, ..., z=26
        } else {
            return -1;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}