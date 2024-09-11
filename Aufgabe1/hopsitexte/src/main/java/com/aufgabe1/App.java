package com.aufgabe1;

import java.io.IOException;

import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class App extends Application {

    private static Scene scene;
    private TextArea textArea = new TextArea();
    private Text output = new Text("Ist dein Text ein Hopsitext? Tippe in das Textfeld:)");

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
                    processText(filterText(newValue));
                });

        // Set up the layout
        BorderPane layout = new BorderPane();
        layout.setCenter(textArea);
        layout.setTop(output);

        // Create Scene
        scene = new Scene(layout, 600, 400);
        stage.setScene(scene);
        stage.show();
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
        output.setText("Filtered Text: " + input);
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
                // TODO I want to change the color of the character in the text at position nextIndex1
                char1 = input.charAt(nextIndex1);

                alphabetPosition1 = getAlphabetPosition(char1);
                nextIndex1 += alphabetPosition1;
            }
            while (nextIndex2 < input.length()) {
                char2 = input.charAt(nextIndex2);
                // TODO I want to change the color of the character in the text at position nextIndex2

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