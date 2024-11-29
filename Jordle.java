import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.layout.GridPane;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.io.IOException;
import javafx.scene.layout.ColumnConstraints;
import javafx.geometry.HPos;





/**
 * @version 1.0
 * @author sam orouji
 * */
public class Jordle extends Application {
    private final int width = 800; //**Final
    private final int height = 800;
    private int currentRow = 0;
    private int currentCol = 0;
    private TextField[][] textFields = new TextField[6][5];
    private GridPane grid; //to be able to focus on grid in helper method resetGrid()
    private Text status; //make this variable universal to change status text in resetGrid() helper method
    private GridPane keyboard;
    private Button[][] keyboardButtons = new Button[3][10];

    /**
     * java --module-path '/Users/samorouji/Library/Mobile Documents/com~apple~CloudDocs/javafx-sdk-21.0.5/lib'.
     * --add-modules javafx.controls -cp '/Users/samorouji/Downloads/OfficialJordleProject' Jordle*/
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setScene(welcomeScreen(primaryStage)); //returns a scene of welcome screen
        primaryStage.setTitle("Jordle"); //title for this main stage is always Jordle
        primaryStage.show(); //show stage
    }


    private Scene welcomeScreen(Stage primaryStage) {
        //load image **get a square image yellow background
        Image image = new Image("file:/Users/samorouji/Downloads/OfficialJordleProject/jordleImage.png"); //abs path

        //Create Image view to represent Image as a Node --bc it's a node has more functions
        //put image in its own pane to center it
        StackPane imagePane = new StackPane();
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(300);
        imageView.setFitWidth(300);
        imageView.setPreserveRatio(true); // Preserve aspect ratio
        imagePane.getChildren().add(imageView);
        imagePane.setAlignment(Pos.CENTER);
        imageView.setTranslateX(30);
        imageView.setTranslateY(-110);


        //holds child nodes - StackPane so (text/button) go ontop & center of image pane.
        StackPane pane = new StackPane(imageView);
        pane.setStyle("-fx-background-color: rgba(229,229,229,1);"); // Alpha is 1 for fully opaque

        //this VBox of text and button will go ontop of the StackPanes image
        VBox pane2 = new VBox(10);
        pane2.setAlignment(Pos.CENTER);

        //Title
        Text text = new Text("Jordle");
        text.setFont(Font.font("Karnak Condensed", FontWeight.BOLD, 50)); //static method Font.font

        //header text
        Text header = new Text("Get 6 Chances to guess a 5-letter word.");
        header.setFont(Font.font("Karnak Condensed", 20));

        //play button that links to game screen
        Button button = new Button("Play");
        button.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-background-radius: 15;"
                + "-fx-font-size: 18px");
        button.setPrefWidth(100); //make button wider
        //button functionality lambda
        button.setOnAction(e -> {
            primaryStage.setScene(gameScreen()); });


        //aligns text up top, button bottom
        pane2.getChildren().addAll(imagePane, text, header, button);

        //now add the VBox pane of text and button to our stackpane w background image
        pane.getChildren().add(pane2);


        //root is stackpane, and pane2 is added to StackPane
        return new Scene(pane, width, height);
    }


    /**
     * @param args all lines of code.
     * main method for compilers sanity.*/
    public static void main(String[] args) {
        launch(args);
    }







    private Scene gameScreen() {
        Backend backend = new Backend();

        // Load word list into HashSet (initialize once)
        HashSet<String> wordSet = loadWordList("wordle-Ta.txt");

        // Title
        StackPane titlePane = new StackPane();
        Text title = new Text("Jordle");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        titlePane.getChildren().add(title);
        titlePane.setAlignment(Pos.CENTER);

        // GridPane setup
        grid = new GridPane();
        grid.setHgap(2);
        grid.setVgap(2);
        grid.setAlignment(Pos.CENTER);

        // Initialize 6x5 TextFields
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 5; col++) {
                TextField textField = new TextField();
                textField.setPrefSize(70, 80);
                textField.setFont(Font.font(20));
                textField.setAlignment(Pos.CENTER);
                textField.setEditable(false);
                textField.setFocusTraversable(false);
                textFields[row][col] = textField;
                grid.add(textField, col, row);
            }
        }

        // Status and buttons
        status = new Text("Try guessing a Jord!");
        status.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        // Restart Button
        Button restart = new Button("Restart");
        restart.setOnAction(e -> {
            backend.reset();
            resetGrid();
        });

        // Instructions Button (Anonymous Inner Class)
        Button instructions = new Button("Instructions");
        instructions.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Stage instructionStage = new Stage();
                instructionStage.setScene(instructionScene());
                instructionStage.show();
            }
        });

        HBox hbox = new HBox(20, status, restart, instructions);
        hbox.setAlignment(Pos.CENTER);


// Initialize the GridPane for the keyboard
        keyboard = new GridPane();
        keyboard.setHgap(2);
        keyboard.setVgap(2);
        keyboard.setAlignment(Pos.CENTER);

// Define the keyboard layout
        String[][] keys = {
                {"Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"}, // First row
                {"A", "S", "D", "F", "G", "H", "J", "K", "L"},      // Second row
                {"Enter", "Z", "X", "C", "V", "B", "N", "M", "Backspace"} // Third row
        };

// Create a jagged array for buttons
        keyboardButtons = new Button[keys.length][];

// Populate the keyboard
        for (int row = 0; row < keys.length; row++) {
            keyboardButtons[row] = new Button[keys[row].length]; // Allocate space for the row

            for (int col = 0; col < keys[row].length; col++) {
                Button key = new Button(keys[row][col]); // Set the button label
                key.setFont(Font.font(20));
                key.setAlignment(Pos.CENTER);
                System.out.println("Creating button with text: " + key.getText()); // Debugging

                // Adjust button sizes for "Enter" and "Backspace"
                if (keys[row][col].equals("Enter") || keys[row][col].equals("Backspace")) {
                    key.setPrefSize(80, 50); // Larger width for Enter and Backspace
                } else {
                    key.setPrefSize(50, 50); // Default size for regular keys
                }

                // Add the button to the jagged array and GridPane
                keyboardButtons[row][col] = key;
                System.out.println("Button created: " + key.getText()); // Debugging

                // Offset the second row by adding a column span or leaving an empty column
                if (row == 1) {
                    keyboard.add(key, col + 1, row); // Add an empty column at the start of the second row
                } else {
                    keyboard.add(key, col, row);
                }

                // Add event handling for buttons
                if (keys[row][col].equals("Enter")) {
                    key.setOnAction(e -> handleEnter(backend, wordSet));
                } else if (keys[row][col].equals("Backspace")) {
                    key.setOnAction(e -> handleBackspace());
                } else {
                    // For letter keys, simulate typing
                    key.setOnAction(e -> handleLetter(key.getText()));
                }
            }
        }


        // Ensure the GridPane looks consistent by adding column constraints for alignment
        ColumnConstraints colConstraint = new ColumnConstraints();
        colConstraint.setHalignment(HPos.CENTER);
        keyboard.getColumnConstraints().addAll(colConstraint);


        //add all elements in main VBox
        VBox root = new VBox(20, titlePane, grid, keyboard, hbox);
        root.setAlignment(Pos.CENTER);
        Scene scene = new Scene(root, width, height);

        // Scene-level key event handler
        scene.setOnKeyPressed(e -> {
            if (e.getCode().isLetterKey() && currentCol < 5) {
                textFields[currentRow][currentCol].setText(e.getText().toUpperCase());
                currentCol++;
            } else if (e.getCode() == KeyCode.BACK_SPACE && currentCol > 0) {
                currentCol--;
                textFields[currentRow][currentCol].setText("");
            } else if (e.getCode() == KeyCode.ENTER) {
                if (currentCol == 5) {
                    StringBuilder word = new StringBuilder();
                    for (int col = 0; col < 5; col++) {
                        word.append(textFields[currentRow][col].getText());
                    }

                    String guessedWord = word.toString().toLowerCase();

                    // Validate word
                    if (guessedWord.length() != 5) {
                        showError("Input Error", "Word must be exactly 5 characters long!");
                    } else if (!wordSet.contains(guessedWord)) {
                        showError("Invalid Word", "The word is not in the word list!");
                    } else {
                        try {
                            String result = backend.check(guessedWord);
                            for (int col = 0; col < 5; col++) {
                                char feedback = result.charAt(col);
                                TextField tf = textFields[currentRow][col];
                                Button correspondingButton = getKeyboardButton(tf.getText()); //on screen keyboard!
                                if (feedback == 'g') {
                                    tf.setStyle("-fx-background-color: green; -fx-text-fill: white;");
                                    if (correspondingButton != null) {
                                        correspondingButton.setStyle("-fx-background-color: green; "
                                                + "-fx-text-fill: white;");
                                    }
                                } else if (feedback == 'y') {
                                    tf.setStyle("-fx-background-color: yellow; -fx-text-fill: black;");
                                    if (correspondingButton != null) {
                                        correspondingButton.setStyle("-fx-background-color: yellow; "
                                                + "-fx-text-fill: white;");
                                    }
                                } else {
                                    tf.setStyle("-fx-background-color: gray; -fx-text-fill: white;");
                                    if (correspondingButton != null) {
                                        correspondingButton.setStyle("-fx-background-color: gray; "
                                                + "-fx-text-fill: white;");
                                    }
                                }
                            }

                            if (result.equals("ggggg")) {
                                status.setText("Congratulations, you guessed the word!");
                                backend.reset();
                            } else {
                                currentRow++;
                                currentCol = 0;
                                if (currentRow >= 6) {
                                    status.setText("Game Over, the correct word was " + backend.getTarget());
                                    backend.reset();
                                }
                            }
                        } catch (InvalidGuessException ex) {
                            showError("Invalid Guess", ex.getMessage());
                        }
                    }
                } else {
                    showError("Input Error", "Word must be exactly 5 characters long!");
                }
            }
        });

        scene.setOnMouseClicked(e -> grid.requestFocus());
        grid.requestFocus();

        return scene;
    }


    private void handleLetter(String letter) {
        if (currentCol < 5) { // Ensure we're within the column limit
            textFields[currentRow][currentCol].setText(letter.toUpperCase());
            currentCol++;
        }
    }


    private void handleBackspace() {
        if (currentCol > 0) { // Ensure we don't backspace before the first column
            currentCol--;
            textFields[currentRow][currentCol].setText("");
        }
    }


    /**
     *
     * */

    private void handleEnter(Backend backend, HashSet<String> wordSet) {
        // Ensure the current row is full
        if (currentCol < 5) {
            showError("Input Error", "Word must be exactly 5 characters long!");
            return;
        }

        // Construct the guessed word
        StringBuilder word = new StringBuilder();
        for (int col = 0; col < 5; col++) {
            String letter = textFields[currentRow][col].getText();
            if (letter == null || letter.isEmpty()) {
                showError("Input Error", "Word must be exactly 5 characters long!");
                return;
            }
            word.append(letter);
        }

        String guessedWord = word.toString().toLowerCase().trim(); // Normalize word
        System.out.println("Guessed Word: " + guessedWord); // Debugging

        // Validate the guessed word
        if (!wordSet.contains(guessedWord)) {
            showError("Invalid Word", "The word is not in the word list!");
            return;
        }

        try {
            // Get feedback from the backend
            String result = backend.check(guessedWord);
            System.out.println("Backend Result: " + result); // Debugging

            for (int col = 0; col < 5; col++) {
                char feedback = result.charAt(col);
                TextField tf = textFields[currentRow][col];
                Button correspondingButton = getKeyboardButton(tf.getText());

                switch (feedback) {
                case 'g': // Correct letter and position (highest priority)
                    tf.setStyle("-fx-background-color: green; -fx-text-fill: white;");
                    if (correspondingButton != null) {
                        correspondingButton.setStyle("-fx-background-color: green; -fx-text-fill: white;");
                    }
                    break;

                case 'y': // Correct letter, wrong position
                    tf.setStyle("-fx-background-color: yellow; -fx-text-fill: black;");
                    if (correspondingButton != null && !correspondingButton.getStyle().contains("green")) {
                        // Only update to yellow if not already green
                        correspondingButton.setStyle("-fx-background-color: yellow; -fx-text-fill: black;");
                    }
                    break;

                case 'i': // Incorrect letter (lowest priority)
                    tf.setStyle("-fx-background-color: gray; -fx-text-fill: white;");
                    if (correspondingButton != null
                            && !correspondingButton.getStyle().contains("green")
                            && !correspondingButton.getStyle().contains("yellow")) {
                        // Only update to gray if not already green or yellow
                        correspondingButton.setStyle("-fx-background-color: gray; -fx-text-fill: white;");
                    }
                    break;

                default:
                    break;
                }
            }


            // Check for win condition
            if (result.equals("ggggg")) {
                status.setText("Congratulations, you guessed the word!");
                backend.reset();
            } else {
                // Move to the next row
                currentRow++;
                currentCol = 0;

                // Check if all rows are used
                if (currentRow >= 6) {
                    status.setText("Game Over, the correct word was " + backend.getTarget());
                    backend.reset();
                }
            }

        } catch (InvalidGuessException ex) {
            // Handle backend exceptions
            System.out.println("InvalidGuessException: " + ex.getMessage()); // Debugging
            showError("Invalid Guess", ex.getMessage());
        }
    }





    /**
     * Handle NPEs -issues with on screen ke
     * */
    private Button getKeyboardButton(String letter) {
        if (letter == null || letter.isEmpty()) {
            System.out.println("getKeyboardButton: Received null or empty letter");
            return null;
        }

        letter = letter.trim().toUpperCase(); // Normalize the letter for comparison

        for (Button[] row : keyboardButtons) {
            for (Button key : row) {
                if (key != null && key.getText().equalsIgnoreCase(letter.trim())) {
                    System.out.println("Found button for letter: " + letter); // Debugging
                    return key;
                }
            }
        }
        System.out.println("getKeyboardButton: No matching button found for letter: " + letter);
        return null; // Return null if no matching button is found
    }







    // Helper method to load word list (you can guess words from wordle-La
    // (all from word-Ta included PLUS more words that will never be the target)
    private HashSet<String> loadWordList(String filePath) {
        HashSet<String> wordSet = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                wordSet.add(line.trim().toLowerCase());
            }
        } catch (IOException e) {
            e.printStackTrace();
            wordSet.add("default"); // Add a default word if file loading fails
        }
        return wordSet;
    }


    // Helper method to reset the grid and state
    private void resetGrid() {
        currentRow = 0;
        currentCol = 0;
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 5; col++) {
                textFields[row][col].setText("");
                //reset color to white
                textFields[row][col].setStyle("-fx-background-color: white; -fx-text-fill: black;");
            }
        }

        // Reset the keyboard buttons
        for (Button[] row : keyboardButtons) {
            for (Button button : row) {
                if (button != null) {
                    button.setStyle("-fx-background-color: white; -fx-text-fill: black;");
                }
            }
        }

        //refix focus on the grid - so it works
        grid.requestFocus();
        //reset status text
        status.setText("Try guessing a Jord!");
    }





    private void showError(String title, String message) {
        //errors don't need to return scene/new stage, bc they are errors they have their own window
        Alert alert = new Alert(AlertType.ERROR); // Set Alert type to ERROR
        alert.setTitle(title); // Title of the alert
        alert.setHeaderText(null); // Optional header text (null for no header)
        alert.setContentText(message); // Error message content
        alert.showAndWait(); // Display the alert and wait for the user to dismiss it
    }

    private Scene instructionScene() {
        VBox vbox = new VBox(10);
        Text title = new Text("How to play Jordle:");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 40));
        Text bullet1 = new Text("-Type 5 letter words and hit enter to evaluate. You have 6 tries!");
        bullet1.setFont(Font.font(20));
        Text bullet2 = new Text("-If a letter is correct and in the right spot is will be highlighted in green");
        bullet2.setFont(Font.font(20));
        Text bullet3 = new Text("-If a letter is correct but in the wrong spot it will be highlighted in yellow");
        bullet3.setFont(Font.font(20));
        Text bullet4 = new Text("-If a letter is not in the word it will be highlighted in gray");
        bullet4.setFont(Font.font(20));
        vbox.getChildren().addAll(title, bullet1, bullet2, bullet3, bullet4);
        return new Scene(vbox, 650, 350);
    }


    //winning javafx animation, sound for correct answers, chart for total games played/won
    //make main page presentable -yellow/bumblebees
}
