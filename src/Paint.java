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
import java.awt.image.BufferedImage;
import java.io.File; //Java File class (used for file input/output operations)
import java.io.IOException; //Java IOException class (used for handling input/output exceptions, basically any errors)

public class Paint extends Application {  //start of the Paint class, which extends to the Application class
    private BorderPane pane; //private BorderPane, main layout for application
    private ImageView imageView; //private ImageView to display images in the application
    private File currentFile; //private File to keep track of the File currently worked on
    private Drawing drawing;
    private ScrollPane scrollPane;
    private AutoSave autoSave;
    private StackPane contentWrapper;
    

    public static void main(String[] args) { //entry for the application
        launch(args); //launches JavaFX application, which calls the start method
    } // end of main method

    @Override //Overrides the start method to set up the application
    public void start(Stage stage) { //start method, which sets up the application
    scrollPane = new ScrollPane();
    scrollPane.setFitToWidth(true);
    scrollPane.setFitToHeight(true);

    contentWrapper = new StackPane();
    contentWrapper.setStyle("-fx-background-color: white;");
    scrollPane.setContent(contentWrapper);

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
        MenuItem pencilItem = new MenuItem("Pencil");
        
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
        closeItem.setOnAction(e -> {
            if (autoSave.confirmClose(stage)) {
                autoSave.stopAutoSave();
                Platform.exit();
            }
        });
        helpItem.setOnAction(e -> displayHelp());
        resizeItem.setOnAction(e -> resizeCanvas());
        pencilItem.setOnAction(e -> {
            drawing.setTool("pencil");
        });
        lineItem.setOnAction(e -> {
            drawing.setTool("line");
        });

        drawing = new Drawing(800, 600);
        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        contentWrapper.getChildren().setAll(drawing.getDrawingPane());
        autoSave = new AutoSave(() -> saveImage(stage, false));
        autoSave.startAutoSave();

        Label label = new Label("GRPaint"); //The label at the bottom of the application
        label.setStyle( //Styling for the GRPaint label, I am sorry in advance
        "-fx-font-family: 'Rockwell Extra Bold';" + 
        "-fx-font-size: 60px;" +//Font size is 40 pixels
        "-fx-text-fill: green;"
        ); //end of style

        BorderPane titleLabel = new BorderPane(); //new BorderPane for the title labeel
        titleLabel.setCenter(label); //sets the label to the center of the BorderPane

        pane = new BorderPane(); //new BorderPane object for the main layout of the application
        pane.setBottom(menuBar); //sets the menuBar to the bottom of the BorderPane
        pane.setCenter(scrollPane);
        pane.setTop(titleLabel); //sets the titleLabel to the top of the BorderPane

        Scene scene = new Scene(pane, 1000, 800); //new Scene object basically sets the application to 1000 pixels wide and 800 pixels tall
        stage.setTitle("GRPaint"); //Sets the title of the program
        Image appIcon = new Image("file:resources/GRPaintIcon.png");
        stage.getIcons().add(appIcon);
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
            autoSave.setCurrentFile(currentFile);
            autoSave.markSaved();
            Image image = new Image(selectedFile.toURI().toString()); //creates a new Image object from the selected file
            drawing.loadImage(image);
            contentWrapper.getChildren().setAll(drawing.getDrawingPane());
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
                new FileChooser.ExtensionFilter("PNG Image", "*.png"), 
                new FileChooser.ExtensionFilter("JPG Image", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("BMP Image", "*.bmp")
            );//end of getExtensionFilters
            File file = fileChooser.showSaveDialog(stage); //shows the save dialog and returns the selected file
            if (file != null) { //if the user selected a file
                currentFile = file; //replaces the currentFile to the selected file
            } else { //if not 
                return; //returns back to the method that was called
            } //end of if
        } //end of if
        
        String fileName = currentFile.getName().toLowerCase();
        String format;

        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")){
            format = "jpg";
        } else if (fileName.endsWith(".bmp")){
            format = "bmp";
        } else {
            format = "png";
        }
        try {//try to save the image
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(imageView.getImage(), null);
            ImageIO.write(bufferedImage, format, currentFile);
            autoSave.markSaved();
            autoSave.setCurrentFile(currentFile);
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

    private void displayHelp() { 
        Stage helpStage = new Stage(); 

        helpStage.setTitle("GRPaint Help"); 

        Label helpText = new Label(
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
            "Crtl + Shift + Alt + r - Suprise\n" +
            "Resize: Insert any height or width you want as long as it isn't 0 or below\n"+
            "Auto Save: After 30 seconds your project will be automatically saved if it isn't saved already\n\n"
        );

        helpText.setWrapText(true);

        helpText.setStyle(
            "-fx-font-size: 14px;"
        );

        VBox layout = new VBox(helpText);
        layout.setSpacing(10); 

        Scene helpScene = new Scene(layout, 500, 500);
        helpStage.setScene(helpScene);
        helpStage.show(); //displays the Help window
    }

private void resizeCanvas() {
    TextInputDialog widthDialog = new TextInputDialog("800");
    widthDialog.setTitle("Resize Canvas");
    widthDialog.setHeaderText("Enter new canvas width:");

    widthDialog.showAndWait().ifPresent(widthInput -> {

        TextInputDialog heightDialog = new TextInputDialog("600");
        heightDialog.setTitle("Resize Canvas");
        heightDialog.setHeaderText("Enter new canvas height:");

        heightDialog.showAndWait().ifPresent(heightInput -> {

            try {
                double width = Double.parseDouble(widthInput);
                double height = Double.parseDouble(heightInput);

                if (width <= 0 || height <= 0) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Invalid Size");
                    alert.setHeaderText("Invalid canvas size");
                    alert.setContentText("Width and height must be greater than 0.");
                    alert.showAndWait();
                    return;
                }
                    drawing = new Drawing(width, height);

                    contentWrapper.getChildren().setAll(drawing.getDrawingPane());

                    autoSave.markUnsaved();
            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Size");
                alert.setHeaderText("Invalid canvas size");
                alert.setContentText("Please enter numbers for the width and height.");
                alert.showAndWait();
            }
        });
    });
}
}