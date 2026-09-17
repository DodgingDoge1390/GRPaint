//Don't know if you want commments on every line, but I will do it anyway!
import javafx.application.Application; //JavaFX Application class (main class for JavaFX applications)
import javafx.application.Platform; //JavaFX Platform class (mainly used for exiting the application)
import javafx.embed.swing.SwingFXUtils; //JavaFX SwingFXUtils class (used for converting between JavaFX and AWT images)
import javafx.scene.*; //JavaFX Scene classes (encompasses all content in a JavaFX application)
import javafx.scene.control.*; //JavaFX Control classes (Buttons, Labels, Menus, etc.)
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.*; //JavaFX Image classes (Image, ImageView, etc.)
import javafx.scene.layout.*; //JavaFX Layout classes (BorderPane mainly)
import javafx.scene.input.*; //JavaFX Input classes (KeyCombinations)
import javafx.stage.*; //JavaFX Stage classes (Stage and FileChooser)
import javax.imageio.ImageIO; //Java ImageIO class (used for reading and writing images)

import java.awt.image.BufferedImage; //Java toolkit for BufferedImage
import java.io.File; //Java File class (used for file input/output operations)
import java.io.IOException; //Java IOException class (used for handling input/output exceptions, basically any errors)

public class Paint extends Application {  //start of the Paint class, which extends to the Application class
    private BorderPane pane; //private BorderPane, main layout for application
    private ImageView imageView; //private ImageView to display images in the application
    private File currentFile; //private File to keep track of the File currently worked on
    private Drawing drawing; //private Drawing for the tools
    private ScrollPane scrollPane; //private for the scrollpane which is for the bigger canvas
    private AutoSave autoSave; //private for autosave and its timer
    private StackPane contentWrapper; //Helps stack the image over the canvas
    

    public static void main(String[] args) { //entry for the application
        launch(args); //launches JavaFX application, which calls the start method
    } // end of main method

    @Override //Overrides the start method to set up the application
    public void start(Stage stage) { //start method, which sets up the application
    scrollPane = new ScrollPane(); // starts the new scrollpane object
    scrollPane.setFitToWidth(true); //makes content fit width of the screen
    scrollPane.setFitToHeight(true); //makes content fit height of the screen

    contentWrapper = new StackPane(); //creates a stackpane object
    contentWrapper.setStyle("-fx-background-color: white;"); //sets the background of the stackpane to white
    scrollPane.setContent(contentWrapper); //puts the Contentwrapper inside of the scrollpane

        MenuBar menuBar = new MenuBar(); //a MenuBar object, went with a MenuBar so that the user can open, save, and close files on a clean layout
        Menu fileMenu = new Menu("File"); //a Menu for the File options (object as well)
        Menu surpriseMenu = new Menu("Surprise"); //a Menu for the Surprise option (object as well)
        Menu closeMenu = new Menu("Close"); //a Menu for the Close option (object as well)
        Menu settingsMenu = new Menu("Settings"); //a Menu for settings (object as well)
        Menu toolsMenu = new Menu("Tools"); //a Menu for tools (object as well)
        
        menuBar.setStyle("-fx-background-color: #d3d3d3;"); //sets the background color of the MenuBar to a light gray

        MenuItem openItem = new MenuItem("Open"); //a MenuItem for the Open option (object as well)
        MenuItem saveItem = new MenuItem("Save"); //a MenuItem for the Save option (object as well)
        MenuItem saveAsItem = new MenuItem("Save As"); //a MenuItem for the Save As option (object as well)
        MenuItem surpriseItem = new MenuItem("Surprise Me"); //a MenuItem for the Surprise Me option (object as well)
        MenuItem closeItem = new MenuItem("Close"); //a MenuItem for the Close option (object as well)
        MenuItem resizeItem = new MenuItem("Resize"); //a MenuItem for the Settings option (object as well)
        MenuItem helpItem = new MenuItem("Help"); //a MenuItem for the Help option
        MenuItem lineItem = new MenuItem("Line"); //a MenuItem for Line option
        MenuItem pencilItem = new MenuItem("Pencil"); //new MenuItem for Pencil option
        
        openItem.setAccelerator(KeyCombination.keyCombination("Ctrl+O")); //sets the keyboard shortcut for the Open option to Ctrl+O
        saveItem.setAccelerator(KeyCombination.keyCombination("Ctrl+S")); //sets the keyboard shortcut for the Save option to Ctrl+S
        saveAsItem.setAccelerator(KeyCombination.keyCombination("Ctrl+Shift+S")); //sets the keyboard shortcut for the Save As option to Ctrl+Shift+S
        surpriseItem.setAccelerator(KeyCombination.keyCombination("Ctrl+Shift+Alt+R")); //sets the keyboard shortcut for the Surprise Me option to Ctrl+Shift+Alt+R
        closeItem.setAccelerator(KeyCombination.keyCombination("Ctrl+Q")); //sets the keyboard shortcut for the Close option to Ctrl+Q
        resizeItem.setAccelerator(KeyCombination.keyCombination("Ctrl+R")); //sets the keyboard shorcut for the Settings to Crtl+Alt+S

        fileMenu.getItems().addAll(openItem, saveItem, saveAsItem); //adds the Open, Save, and Save As objects to the File menu
        surpriseMenu.getItems().addAll(surpriseItem); //adds the Surprise Me object to the Surprise menu
        closeMenu.getItems().addAll(closeItem); //adds the Close object to the Close menu
        settingsMenu.getItems().addAll(resizeItem, helpItem); //adds the settings and help objects to the Settings menu
        toolsMenu.getItems().addAll(lineItem, pencilItem); //adds Line to the tools menu
        menuBar.getMenus().add(fileMenu); //adds the File menu to the MenuBar
        menuBar.getMenus().add(toolsMenu); //adds Tools menu to MenuBar
        menuBar.getMenus().add(surpriseMenu); //adds the Surprise menu to the MenuBar
        menuBar.getMenus().add(settingsMenu); //adds the settings menu to the MenuBar
        menuBar.getMenus().add(closeMenu);  //adds the Close menu to the MenuBar

        openItem.setOnAction(e -> openImage(stage)); //On the action, it will call the openImage method
        saveItem.setOnAction(e -> saveImage(stage, false)); // On the action, it will call the SaveImage method, the second parameter is false that means that it will save the image to the current file if there is one, if not it will prompt the user to save as a new file
        saveAsItem.setOnAction(e -> saveImage(stage, true)); // On the action, it will call the SaveImage method, the second parameter is true that means that it will prompt the user to save as a new file
        surpriseItem.setOnAction(e -> displaySurprise()); //On the action, it will call the displaySuprise method, which is a suprise of course
        closeItem.setOnAction(e -> { //On the action..
            if (autoSave.confirmClose(stage)) { //if the user hasn't saved an alert will pop up
                autoSave.stopAutoSave(); //stops the autosave after the user has saved
                Platform.exit(); //exits the platform
            } //end of if
        }); //end of OnAction
        helpItem.setOnAction(e -> displayHelp()); //On the action, it will call the displayHelp() method
        resizeItem.setOnAction(e -> resizeCanvas()); //On the action, it will call the resizeCanvas() function
        pencilItem.setOnAction(e -> {drawing.setTool("pencil");}); //On the action, it will set the setTool to pencil 
        lineItem.setOnAction(e -> {drawing.setTool("line");}); //On the action, it will set the setTool to line

        drawing = new Drawing(800, 600); //creates a new drawing object, the same as the scene
        imageView = new ImageView(); //creates a new ImageView object which sets the the image in your window
        imageView.setPreserveRatio(true); //keeps the image's proportions
        contentWrapper.getChildren().setAll(drawing.getDrawingPane()); //basically gets your stackpane and gets everything inside of the stackpane and removes it and puts the borderpane which is my canvas and toolbar
        autoSave = new AutoSave(() -> saveImage(stage, false)); //creates a new object for Autosave, whenever the user saves an image, it will call Autosave to be with stage and false
        autoSave.startAutoSave(); //starts the autoSave timer

        Label label = new Label("GRPaint"); //The label at the top of the application
        label.setStyle( //Styling for the GRPaint label
        "-fx-font-family: 'Rockwell Extra Bold';" + //sets the font to Rockwell Extra bold
        "-fx-font-size: 60px;" +//Font size is 60 pixels
        "-fx-text-fill: green;" //sets the font color to green
        ); //end of style

        BorderPane titleLabel = new BorderPane(); //new BorderPane for the title labeel
        titleLabel.setCenter(label); //sets the label to the center of the BorderPane

        pane = new BorderPane(); //new BorderPane object for the main layout of the application
        pane.setBottom(menuBar); //sets the menuBar to the bottom of the BorderPane
        pane.setCenter(scrollPane); //sets the scrollpane at the center
        pane.setTop(titleLabel); //sets the titleLabel to the top of the BorderPane

        Scene scene = new Scene(pane, 1000, 800); //new Scene object basically sets the application to 1000 pixels wide and 800 pixels tall
        stage.setTitle("GRPaint"); //Sets the title of the program
        Image appIcon = new Image("file:resources/GRPaintIcon.png"); //new icon for the app
        stage.getIcons().add(appIcon); //adds the appIcon to the application
        stage.setScene(scene); //sets the scene
        stage.show(); //shows the stage, which is the main window
    } //end of start method

    private void openImage(Stage stage) { //method to open an image file made it private so it can only be called within the Paint class
        FileChooser fileChooser = new FileChooser(); //new FileChooser object, which helps tremedously with opening files
        fileChooser.setTitle("Open Image File"); //sets the title of the FileChooser window to "Open Image File"
        fileChooser.getExtensionFilters().addAll( //adds a filter to only allow certain types of image files to be opened
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif") // all the different file types that can be opened
        );//end of getExtensionFilters
        File selectedFile = fileChooser.showOpenDialog(stage); //shows the open dialog and returns the selected file
        if (selectedFile != null) { //if the user selected a file
            currentFile = selectedFile; // replaces the currentFile to the selected file
            autoSave.setCurrentFile(currentFile); //sets the autosave to the currentFile
            autoSave.markSaved(); //calls the markSavedFunction
            Image image = new Image(selectedFile.toURI().toString()); //creates a new Image object from the selected file
            drawing.loadImage(image); //loads the image of the drawing
            contentWrapper.getChildren().setAll(drawing.getDrawingPane()); //takes everything out and 
            pane.setCenter(scrollPane);
        } // end of if
    } //end of openImage method
    

    private void saveImage(Stage stage, boolean saveAs) { //method to save an image file made it private so it can only be called within the Paint class
        if (imageView.getImage() == null) { //if there is no image loaded
            System.out.println("No image loaded to save!"); // it will print out "No image loaded to save!" to the console
            return; //returns back to the method that was called
        } // end of if

        if (saveAs || currentFile == null) { //if the user wants to save a new file or if there is no current file saved
            FileChooser fileChooser = new FileChooser(); //new FileChooser object, which helps with saving files
            fileChooser.setTitle("Save Image As"); //sets the title of the FileChooser window to "Save Image As"
            fileChooser.getExtensionFilters().addAll( //adds a filter to only allow certain types of image files to be saved
                new FileChooser.ExtensionFilter("PNG Image", "*.png"), //adds the filter for PNG's
                new FileChooser.ExtensionFilter("JPG Image", "*.jpg", "*.jpeg"), //adds the filter for JPG's and JPEG's
                new FileChooser.ExtensionFilter("BMP Image", "*.bmp") //adds the filter for BMP's
            );//end of getExtensionFilters
            File file = fileChooser.showSaveDialog(stage); //shows the save dialog and returns the selected file
            if (file != null) { //if the user selected a file
                currentFile = file; //replaces the currentFile to the selected file
            } else { //if not 
                return; //returns back to the method that was called
            } //end of if
        } //end of if
        
        String fileName = currentFile.getName().toLowerCase(); //gets the currentFile and puts it all in lowercase for the Filename
        String format; //sets the variable for the format of the file

        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")){ //if the file ends with .jpg or .jpeg
            format = "jpg"; //the format is jpg
        } else if (fileName.endsWith(".bmp")){ //if the file ends with .bmp
            format = "bmp"; //the format is .bmp
        } else { //else
            format = "png"; //the format is png
        } //end of if statements
        try {//try to save the image
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(imageView.getImage(), null); //transfers the BufferedImage to SwingFXUtils and to get the image
            ImageIO.write(bufferedImage, format, currentFile); //writes the buffered image to the format of the currentFile
            autoSave.markSaved(); //marks the file as saved
            autoSave.setCurrentFile(currentFile); //sets the currentFile of the autosave to currentFile
            System.out.println("Saved successfully to: " + currentFile.getAbsolutePath()); //prints out "Saved successfully to: " and the absolute path of the current file to the console
        } catch (IOException ex) { //catches any IOExceptions that may occur while saving the image
            System.err.println("Failed to save image: " + ex.getMessage()); //prints out "Failed to save image: " and the exception message to the console
        } //end of try-catch
    } //end of saveImage method

    private void displaySurprise() { //method to display the suprise image, I will for sure add more suprises later down the line
        File file = new File("resources/Capybara.jpg"); //creates a file object for the Capybara.jpg image, which is located in the resources folder

        if (file.exists()) { // if the file exists
            Image image = new Image(file.toURI().toString()); //creates a new Image object from the Capybara.jpg file
            imageView.setImage(image); //sets the imageView to display the Capybara.jpg image
            contentWrapper.getChildren().setAll(imageView);
            pane.setCenter(scrollPane);
            autoSave.markUnsaved();
        } else { //if the file does not exist
            System.out.println("Capybara.jpg not found!"); //prints out "Capybara.jpg not found!" to the console
        } //end of if
    } //end of displaySurprise method

    private void displayHelp() { //method for displaying help window
        Stage helpStage = new Stage(); //creates a new stage for the popup helpStage

        helpStage.setTitle("GRPaint Help"); //title of the GRPaint help

        Label helpText = new Label( //label for the help to display the text for Help here's all the text
            "GRPaint Help\n\n" +

            "File:\n" +
            "Open - Opens an image file.\n" +
            "Save - Saves the current image.\n" +
            "Save As - Saves the image as a new file.\n\n" +

            "Tools:\n" +
            "Line - Allows you to draw lines on the canvas.\n" +
            "Pencil - Allows you to free flow your canvas.\n\n" +

            "Keyboard Shortcuts:\n" +
            "Ctrl + O - Open\n" +
            "Ctrl + S - Save\n" +
            "Ctrl + Shift + S - Save As\n" +
            "Ctrl + Q - Close\n" +
            "Crtl + Shift + Alt + R - Suprise\n" +
            "Ctrl + R - Resize\n" +
            "Resize: Insert any height or width you want as long as it isn't 0 or below.\n"+
            "Auto Save: After 30 seconds your project will be automatically saved if it isn't saved already.\n\n"
        ); //end of help

        helpText.setWrapText(true); //sets the text wrap to true so it can text wrap

        helpText.setStyle( //font style for the help text
            "-fx-font-size: 14px;"
        );

        VBox layout = new VBox(helpText); //VBox object helps organize the helpText
        layout.setSpacing(10); //sets the spacing to 10 for the text

        Scene helpScene = new Scene(layout, 500, 500); //creates a smaller window for 
        helpStage.setScene(helpScene); //sets the scene to soon display the helpScene
        helpStage.show(); //displays the Help window
    }

    private void resizeCanvas() { //resize canvas method
        TextInputDialog widthDialog = new TextInputDialog("800"); //text input dialog set to 800
        widthDialog.setTitle("Resize Canvas"); //sets the title
        widthDialog.setHeaderText("Enter new canvas width:"); //sets the header text

        widthDialog.showAndWait().ifPresent(widthInput -> { //wait for width input from the user, if the user inputs, then move to this

            TextInputDialog heightDialog = new TextInputDialog("600"); //text input dialog set to 600
            heightDialog.setTitle("Resize Canvas"); //sets the title
            heightDialog.setHeaderText("Enter new canvas height:"); //sets the header text

            heightDialog.showAndWait().ifPresent(heightInput -> { //wait for height input from the user, if the user inputs, then move to this
                try { //try function
                    double width = Double.parseDouble(widthInput); //sets the width from string widthInput
                    double height = Double.parseDouble(heightInput); //sets the height from string heightInput
                    drawing = new Drawing(width, height); //creates a new drawing with the inputted height and width
                    contentWrapper.getChildren().setAll(drawing.getDrawingPane()); //resets all images,canvas,etc to only this drawing
                    autoSave.markUnsaved(); //autosave marks it as unsaved
                } catch (NumberFormatException e) { //catches any Number Format errors
                    Alert alert = new Alert(Alert.AlertType.ERROR); //creates an alert for invalid canvas size
                    alert.setTitle("Invalid Size");
                    alert.setHeaderText("Invalid canvas size");
                    alert.setContentText("Please enter numbers for the width and height.");
                    alert.showAndWait();
                } //end of catch
            }); //end of showAndWait
        }); //end of showAndWait
    }//end of resize method
} //end of Paint class
