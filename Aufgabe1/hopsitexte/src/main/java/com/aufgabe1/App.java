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
                    processText(newValue);
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

    private void processText(String input) {
        textFlow.getChildren().clear();

        if (input.isEmpty()) {
            output.setText("Ist dein Text ein Hopsitext? Tippe in das Textfeld :)");
        }
        if (input.length() == 1) {
            textFlow.getChildren().add(new Text(input));
            output.setText("Zu kurz um ein Hopsitext zu sein!");
        }
        textFlow.getChildren()
                .addAll(mergeTextFlows(jump(input, 0, Color.RED), jump(input, 1, Color.BLUE)).getChildren());
    }

    private TextFlow mergeTextFlows(TextFlow tf1, TextFlow tf2) {
        TextFlow result = new TextFlow();

        for (int i = 0; i < tf1.getChildren().size(); i++) {
            Text text1 = (Text) tf1.getChildren().get(i);
            Text text2 = (Text) tf2.getChildren().get(i);

            Color color1 = (Color) text1.getFill();
            Color color2 = (Color) text2.getFill();

            Text mergedText = new Text(text1.getText());

            Color finalColor = determineColor(color1, color2);
            mergedText.setFill(finalColor);

            result.getChildren().add(mergedText);
        }

        return result;
    }

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

    private TextFlow jump(String input, int startIndex, Color color) {
        TextFlow tf = new TextFlow();
        int nextIndex = startIndex;
        char currentChar = ' ';
        Text textNode = new Text();

        if (nextIndex == 1 && !input.isEmpty()) {
            textNode = new Text(String.valueOf(input.charAt(0)));
            tf.getChildren().add(textNode);
        }

        while (nextIndex < input.length()) {
            currentChar = input.charAt(nextIndex);
            textNode = new Text(String.valueOf(currentChar));

            if (getAlphabetPosition(currentChar) != -1) {
                textNode.setFill(color);
                tf.getChildren().add(textNode);
                // Find its position in the alphabet
                int alphabetPosition = getAlphabetPosition(currentChar);
                int count = 0;

                // Skip non-alphabetic characters and move forward by the alphabet position
                while (count < alphabetPosition) {
                    nextIndex++;
                    if (nextIndex >= input.length()) {
                        System.out.println("Index " + nextIndex + " is out of bounds for the input length.");
                        break;
                    }
                    if (getAlphabetPosition(input.charAt(nextIndex)) != -1) {
                        count++;
                        if (count != alphabetPosition) {
                            textNode = new Text(String.valueOf(input.charAt(nextIndex)));
                            tf.getChildren().add(textNode);
                        }
                    } else {
                        textNode = new Text(String.valueOf(input.charAt(nextIndex)));
                        tf.getChildren().add(textNode);
                    }
                }
            } else {
                textNode = new Text(String.valueOf(input.charAt(nextIndex)));
                textNode.setFill(color);
                tf.getChildren().add(textNode);
                nextIndex++;
            }
        }
        return tf;
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
            return -1;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}