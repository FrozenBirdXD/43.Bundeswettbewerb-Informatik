package com.aufgabe1;

import java.io.IOException;

import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

public class App extends Application {

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

        // ChangeListener for the textArea textProperty
        textArea.textProperty()
                .addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                    // Called every time the text is changed
                    processTextNew(newValue);
                });

        // Create SplitPane
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(
                textArea, textFlow);

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

    private void processTextNew(String input) {
        textFlow.getChildren().clear(); // Clear previous content

        /*
         * if (input.isEmpty()) {
         * output.setText("Ist dein Text ein Hopsitext? Tippe in das Textfeld :)");
         * } else if (input.length() == 1) {
         * output.setText("Zu kurz für ein Hopsitext");
         * } else {
         */
        int nextIndex1 = 0;
        int nextIndex2 = 0;

        // Find the position of alphabetic characters and calculate based on them
        char currentChar = input.charAt(0);
        Text textNode = new Text(String.valueOf(currentChar));
        textNode.setFill(Color.PINK); // Change color to red for specific indices

        // Check if the current character is an alphabetic letter
        while (nextIndex1 < input.length()) {
            currentChar = input.charAt(nextIndex1);
            textNode = new Text(String.valueOf(currentChar));

            if (getAlphabetPosition(currentChar) != -1) {
                textNode.setFill(Color.RED);
                textFlow.getChildren().add(textNode);
                // Find its position in the alphabet
                int alphabetPosition = getAlphabetPosition(currentChar);
                System.out.println("Character: " + currentChar + ", Position in alphabet: " + alphabetPosition);

                // Calculate the next index based on the alphabet position
                int count = 0;

                // Skip non-alphabetic characters and move forward by the alphabet position
                while (count < alphabetPosition) {
                    nextIndex1++;
                    if (nextIndex1 >= input.length()) {
                        System.out.println("Index " + nextIndex1 + " is out of bounds for the input length.");
                        break;
                    }
                    if (getAlphabetPosition(input.charAt(nextIndex1)) != -1) {
                        count++;
                        if (count == alphabetPosition) {
                        } else {
                            textNode = new Text(String.valueOf(input.charAt(nextIndex1)));
                            textFlow.getChildren().add(textNode);
                        }
                    } else {
                        textNode = new Text(String.valueOf(input.charAt(nextIndex1)));
                        textNode.setFill(Color.GREEN); // Change color to red for specific indices
                        textFlow.getChildren().add(textNode);
                    }
                }
                // Check if we found a valid next character
                /*
                 * if (nextIndex1 < input.length() &&
                 * getAlphabetPosition(input.charAt(nextIndex1)) != -1) {
                 * System.out.println("Character at position " + nextIndex1 + " is: " +
                 * input.charAt(nextIndex1));
                 * textNode = new Text(String.valueOf(input.charAt(nextIndex1)));
                 * textNode.setFill(Color.RED); // Change color to red for specific indices
                 * textFlow.getChildren().add(textNode);
                 * nextIndex1++;
                 * }
                 */
                /*
                 * } else {
                 * textFlow.getChildren().add(textNode);
                 * }
                 */

                /*
                 * // Find the position of alphabetic characters and calculate based on them
                 * for (int i = 1; i < input.length(); i++) {
                 * char currentChar = input.charAt(i);
                 * 
                 * // Check if the current character is an alphabetic letter
                 * if (getAlphabetPosition(currentChar) != -1) {
                 * // Find its position in the alphabet
                 * int alphabetPosition = getAlphabetPosition(currentChar);
                 * System.out.println("Character: " + currentChar + ", Position in alphabet: " +
                 * alphabetPosition);
                 * 
                 * // Calculate the next index based on the alphabet position
                 * int count = 0;
                 * nextIndex2 = i;
                 * 
                 * // Skip non-alphabetic characters and move forward by the alphabet position
                 * while (count < alphabetPosition) {
                 * nextIndex2++;
                 * if (nextIndex2 >= input.length()) {
                 * System.out.println("Index " + nextIndex2 +
                 * " is out of bounds for the input length.");
                 * break;
                 * }
                 * if (getAlphabetPosition(input.charAt(nextIndex2)) != -1) {
                 * count++;
                 * }
                 * }
                 * 
                 * // Check if we found a valid next character
                 * if (nextIndex2 < input.length() &&
                 * Character.isLetter(input.charAt(nextIndex2))) {
                 * System.out.println("Character at position " + nextIndex2 + " is: " +
                 * input.charAt(nextIndex2));
                 * 
                 * }
                 * }
                 * }
                 */
                if (nextIndex1 == nextIndex2) {
                    output.setText("Der Text ist kein Hopsitext :(");
                } else {
                    output.setText("Der Text ist ein Hopsitext :)");
                }
            } else {
                textNode = new Text(String.valueOf(input.charAt(nextIndex1)));
                textNode.setFill(Color.ORANGE); // Change color to red for specific indices
                textFlow.getChildren().add(textNode);
                nextIndex1++;
            }
        }
    }

    private String filterText(String input) {
        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (getAlphabetPosition(c) != -1) {
                result.append(c);
            }
        }

        return result.toString();
    }

    private void processText(String input) {
        if (input.isEmpty()) {
            output.setText("Ist dein Text ein Hopsitext? Tippe in das Textfeld :)");
        } else if (input.length() == 1) {
            output.setText("Zu kurz für ein Hopsitext");
        } else {
            char char1 = input.charAt(0);
            char char2 = input.charAt(1);
            int alphabetPosition1 = getAlphabetPosition(char1);
            int alphabetPosition2 = getAlphabetPosition(char2);

            int nextIndex1 = alphabetPosition1;
            int nextIndex2 = alphabetPosition2;

            while (nextIndex1 < input.length()) {
                // TODO I want to change the color of the character in the text at position
                // nextIndex1
                char1 = input.charAt(nextIndex1);

                alphabetPosition1 = getAlphabetPosition(char1);
                nextIndex1 += alphabetPosition1;
            }
            while (nextIndex2 < input.length()) {
                char2 = input.charAt(nextIndex2);
                // TODO I want to change the color of the character in the text at position
                // nextIndex2

                alphabetPosition2 = getAlphabetPosition(char2);
                nextIndex2 += alphabetPosition2;
            }
            if (nextIndex1 == nextIndex2) {
                output.setText("Der Text ist kein Hopsitext :(");
            } else {
                output.setText("Der Text ist ein Hopsitext :)");
            }
        }
    }

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

        if (Character.isUpperCase(c)) {
            return c - 'A' + 1; // A=1, B=2, ..., Z=26
        } else if (Character.isLowerCase(c)) {
            return c - 'a' + 1; // a=1, b=2, ..., z=26
        } else {
            return -1; // Not a letter
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}