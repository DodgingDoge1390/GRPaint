//I like my Java how I like my coffee, at home and brewed 
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import javafx.stage.*;
import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
/**
 * Main Paint class that extends the Application
 * Connects the UI for the program
 */
public class Paint extends Application {
    private BorderPane pane;
    private ImageView imageView;
    private AutoSave autoSave;
    private TabPane tabPane;
    private int tabCounter = 1;

/**
 * Main method that launches the main application
 * 
 * @param args command line arguments passed to application
 */
    public static void main(String[] args) {
        launch(args);
    }

/**
 * Public method for the start of the application
 * Sets the private variables needed for the application to work
 * Sets MenuBars, MenuItems, what the Menus do, Labels, and the Scene
 * Creates shortcuts using accelerators
 * 
 * @Override to the overthrow the main function
 * @param stage primary JavaFX stage
 * @see javafx.application.Application#start(javafx.stage.Stage)
 */
    @Override
    public void start(Stage stage) {
        //Tabpane object
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        addNewTab();

        //Menubar and menus
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        Menu surpriseMenu = new Menu("Surprise");
        Menu closeMenu = new Menu("Close");
        Menu settingsMenu = new Menu("Settings");
        Menu toolsMenu = new Menu("Tools");
        Menu shapesMenu = new Menu("Shapes");

        menuBar.setStyle("-fx-background-color: #d3d3d3;");

        //MenuItems
        MenuItem openItem = new MenuItem("Open");
        MenuItem saveItem = new MenuItem("Save");
        MenuItem saveAsItem = new MenuItem("Save As");
        MenuItem newTabItem = new MenuItem("New Tab");
        MenuItem surpriseItem = new MenuItem("Surprise Me");
        MenuItem closeItem = new MenuItem("Close");
        MenuItem resizeItem = new MenuItem("Resize");
        MenuItem helpItem = new MenuItem("Help");
        MenuItem lineItem = new MenuItem("Line");
        MenuItem pencilItem = new MenuItem("Pencil");
        MenuItem colorGrabberItem = new MenuItem("Color Grabber");
        MenuItem eraserItem = new MenuItem("Eraser");
        MenuItem squareItem = new MenuItem("Square");
        MenuItem circleItem = new MenuItem("Circle");
        MenuItem rectangleItem = new MenuItem("Rectangle");
        MenuItem ellipseItem = new MenuItem("Ellipse");
        MenuItem triangleItem = new MenuItem("Triangle");
        MenuItem starItem = new MenuItem("Star");

        //Shortcuts for MenuItems
        openItem.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));
        saveItem.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
        saveAsItem.setAccelerator(KeyCombination.keyCombination("Ctrl+Shift+S"));
        newTabItem.setAccelerator(KeyCombination.keyCombination("Ctrl+T"));
        surpriseItem.setAccelerator(KeyCombination.keyCombination("Ctrl+Shift+Alt+R"));
        closeItem.setAccelerator(KeyCombination.keyCombination("Ctrl+Q"));
        resizeItem.setAccelerator(KeyCombination.keyCombination("Ctrl+R"));

        //Puts MenuItems in the Menu
        fileMenu.getItems().addAll(openItem, saveItem, saveAsItem, newTabItem);
        surpriseMenu.getItems().addAll(surpriseItem);
        closeMenu.getItems().addAll(closeItem);
        settingsMenu.getItems().addAll(resizeItem, helpItem);
        toolsMenu.getItems().addAll(lineItem, pencilItem, colorGrabberItem, eraserItem);
        shapesMenu.getItems().addAll(squareItem, circleItem, rectangleItem, ellipseItem, triangleItem, starItem);

        //Adds menus to menuBar
        menuBar.getMenus().addAll(fileMenu, toolsMenu, shapesMenu, surpriseMenu, settingsMenu, closeMenu);

        //When you click on the menuItem, the method is called
        openItem.setOnAction(e -> {addNewTab(); openImage(stage);});
        saveItem.setOnAction(e -> saveImage(stage, false));
        saveAsItem.setOnAction(e -> saveImage(stage, true));
        newTabItem.setOnAction(e -> addNewTab());
        surpriseItem.setOnAction(e -> displaySurprise());
        closeItem.setOnAction(e -> {
            if (autoSave.confirmClose(stage)) {
                autoSave.stopAutoSave();
                Platform.exit();
            }
        });
        helpItem.setOnAction(e -> displayHelp());
        resizeItem.setOnAction(e -> resizeCanvas());
        pencilItem.setOnAction(e -> currentTab().getDrawing().setTool("pencil"));
        lineItem.setOnAction(e -> currentTab().getDrawing().setTool("line"));
        colorGrabberItem.setOnAction(e -> currentTab().getDrawing().setTool("colorGrabber"));
        eraserItem.setOnAction(e -> currentTab().getDrawing().setTool("eraser"));
        squareItem.setOnAction(e -> currentTab().getDrawing().setTool("square"));
        circleItem.setOnAction(e -> currentTab().getDrawing().setTool("circle"));
        rectangleItem.setOnAction(e -> currentTab().getDrawing().setTool("rectangle"));
        ellipseItem.setOnAction(e -> currentTab().getDrawing().setTool("ellipse"));
        triangleItem.setOnAction(e -> currentTab().getDrawing().setTool("triangle"));
        starItem.setOnAction(e -> currentTab().getDrawing().setTool("star"));

        //ImageView for the images and starts Autosave
        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        autoSave = new AutoSave(() -> saveImage(stage, false));
        autoSave.startAutoSave();

        //Style for the Title
        Label label = new Label("GRPaint");
        label.setStyle(
            "-fx-font-family: 'Rockwell Extra Bold';" +
            "-fx-font-size: 60px;" +
            "-fx-text-fill: green;"
        );

        //Borderpane title object
        BorderPane titleLabel = new BorderPane();
        titleLabel.setCenter(label);

        //Sets Borderpane for organization
        pane = new BorderPane();
        pane.setBottom(menuBar);
        pane.setCenter(tabPane);
        pane.setTop(titleLabel);

        //Sets the scence and the app icon and starts it
        Scene scene = new Scene(pane, 1000, 800);
        stage.setTitle("GRPaint");
        Image appIcon = new Image(getClass().getResource("GRPaintIcon.png").toExternalForm());
        stage.getIcons().add(appIcon);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Private method returns current tabpane
     * @return the current Tab
     */
    private PaintTab currentTab() {
        return (PaintTab) tabPane.getSelectionModel().getSelectedItem();
    }

    /**
     * Private method that creates a new PaintTab object
     * Automatically sets the new PaintTab as the current
     */
    private void addNewTab() {
        PaintTab tab = new PaintTab("Untitled " + tabCounter++, 800, 600);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
    }

    /**
     * Private method that opens an image from the user's files
     * FileChooser that takes png, jpg, jpeg, bmp, and gif and waits for the user to select their file
     * Sets the selected file to the current tab and loads the image to the tab
     * Sets autosave on the current file and marks as saved
     * 
     * @param stage the primary stage for the JavaFX FileChooser
     */
    private void openImage(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Image File");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            currentTab().setCurrentFile(selectedFile);
            autoSave.setCurrentFile(selectedFile);
            autoSave.markSaved();
            Image image = new Image(selectedFile.toURI().toString());
            currentTab().getDrawing().loadImage(image);
        }
    }

    /**
     * Private method for saving the Image to the user's system
     * Files can be saved in png, jpg, jpeg, and bmp
     * Once the user selects their file, a snapshot is created
     * The method uses SwingFX to write to the system in the selected format
     * Autosave is called to let it know the file has been saved
     * 
     * @param stage primary stage for the JavaFX application
     * @param saveAs boolean variable for whether to use SaveAs dialog
     */
    private void saveImage(Stage stage, boolean saveAs) {
        Canvas canvas = currentTab().getDrawing().getCanvas();

        if (saveAs || currentTab().getCurrentFile() == null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Image As");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PNG Image", "*.png"),
                new FileChooser.ExtensionFilter("JPG Image", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("BMP Image", "*.bmp")
            );
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                currentTab().setCurrentFile(file);
            } else {
                return;
            }
        }

        File currentFile = currentTab().getCurrentFile();
        String fileName = currentFile.getName().toLowerCase();
        String format = fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") ? "jpg"
                    : fileName.endsWith(".bmp") ? "bmp" : "png";

        try {
            WritableImage snapshot = canvas.snapshot(null, null);
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(snapshot, null);
            ImageIO.write(bufferedImage, format, currentFile);
            autoSave.markSaved();
            autoSave.setCurrentFile(currentFile);
            System.out.println("Saved successfully to: " + currentFile.getAbsolutePath());
        } catch (IOException ex) {
            System.err.println("Failed to save image: " + ex.getMessage());
        }
    }

    /**
     * Private method to display Suprise Capybara
     * Gets the imageURL from the program folder
     * Displays it on the currentTab
     */
    private void displaySurprise() {
        java.net.URL imageURL = getClass().getResource("/Capybara.jpg");
        if (imageURL != null) {
            addNewTab(); // gives the surprise its own tab instead of wiping your work
            Image image = new Image(imageURL.toExternalForm());
            currentTab().getDrawing().loadImage(image);
            currentTab().setCurrentFile(null);
            autoSave.markUnsaved();
        } else {
            System.out.println("Capybara.jpg not found!");
        }
    }

    /**
     * Private method that displays the window for the help features
     * Uses a labe and opens a new stage that is smaller than the program window
     */
    private void displayHelp() {
        Stage helpStage = new Stage();
        helpStage.setTitle("GRPaint Help");

        Label helpText = new Label(
            "GRPaint Help\n\n" +
            "File:\n" +
            "Open - Opens an image file.\n" +
            "Save - Saves the current image.\n" +
            "Save As - Saves the image as a new file.\n" +
            "New Tab - Opens a new blank canvas tab.\n\n" +
            "Tools:\n" +
            "Line - Allows you to draw lines on the canvas.\n" +
            "Pencil - Allows you to free flow your canvas.\n\n" +
            "Keyboard Shortcuts:\n" +
            "Ctrl + O - Open\n" +
            "Ctrl + S - Save\n" +
            "Ctrl + Shift + S - Save As\n" +
            "Ctrl + T - New Tab\n" +
            "Ctrl + Q - Close\n" +
            "Crtl + Shift + Alt + R - Suprise\n" +
            "Ctrl + R - Resize\n" +
            "Resize: Insert any height or width you want as long as it isn't 0 or below.\n" +
            "Auto Save: After 30 seconds your project will be automatically saved if it isn't saved already.\n\n"
        );

        helpText.setWrapText(true);
        helpText.setStyle("-fx-font-size: 14px;");

        VBox layout = new VBox(helpText);
        layout.setSpacing(10);

        Scene helpScene = new Scene(layout, 500, 500);
        helpStage.setScene(helpScene);
        helpStage.show();
    }

    /**
     * Private method that prompts users to resize the canvas
     * Using textInputDialog the user inputs the width and the height
     * The drawing is then set to the width and the height
     */
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
                    currentTab().setDrawing(new Drawing(width, height));
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