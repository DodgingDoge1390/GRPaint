//I like my Java how I like my coffee, at home and brewe
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
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
    private Stage primaryStage;
    private ImageServer imageServer;
    private volatile boolean autosaveNotifications = true;
    private TrayIcon notificationTrayIcon;

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
        primaryStage = stage;
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        addNewTab();

        //Menubar and menus
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        Menu editMenu = new Menu("Edit"); 
        Menu surpriseMenu = new Menu("Surprise");
        Menu closeMenu = new Menu("Close");
        Menu settingsMenu = new Menu("Settings");
        Menu toolsMenu = new Menu("Tools");
        Menu webserverMenu = new Menu("Web Server");

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
        MenuItem textItem = new MenuItem("Text");
        MenuItem selectItem = new MenuItem("Select");
        MenuItem moveItem = new MenuItem("Move Selection");
        MenuItem undoItem = new MenuItem("Undo");
        MenuItem redoItem = new MenuItem("Redo");
        MenuItem copyItem = new MenuItem("Copy");
        MenuItem pasteItem = new MenuItem("Paste");
        MenuItem clearItem = new MenuItem("Clear Canvas");
        MenuItem shareItem = new MenuItem("Share Selected Tabs on Local Web Server");
        MenuItem stopShareItem = new MenuItem("Stop Image Server");
        CheckMenuItem autosaveItem = new CheckMenuItem("Enable Autosave");
        CheckMenuItem timerItem = new CheckMenuItem("Show Autosave Countdown");
        CheckMenuItem notificationItem = new CheckMenuItem("Show Autosave Notifications");
        notificationItem.setSelected(true);
        Menu canvasTransformMenu = createTransformMenu("Transform Whole Canvas", false);
        Menu selectionTransformMenu = createTransformMenu("Transform Selection", true);

        //Shortcuts for MenuItems
        openItem.setAccelerator(KeyCombination.keyCombination("Shortcut+O"));
        saveItem.setAccelerator(KeyCombination.keyCombination("Shortcut+S"));
        saveAsItem.setAccelerator(KeyCombination.keyCombination("Shortcut+Shift+S"));
        newTabItem.setAccelerator(KeyCombination.keyCombination("Shortcut+T"));
        surpriseItem.setAccelerator(KeyCombination.keyCombination("Shortcut+Shift+Alt+R"));
        closeItem.setAccelerator(KeyCombination.keyCombination("Shortcut+Q"));
        resizeItem.setAccelerator(KeyCombination.keyCombination("Shortcut+R"));
        undoItem.setAccelerator(KeyCombination.keyCombination("Shortcut+Z"));
        redoItem.setAccelerator(KeyCombination.keyCombination("Shortcut+Y"));
        copyItem.setAccelerator(KeyCombination.keyCombination("Shortcut+C"));
        pasteItem.setAccelerator(KeyCombination.keyCombination("Shortcut+V"));
        clearItem.setAccelerator(KeyCombination.keyCombination("Shortcut+Shift+N"));

        //Puts MenuItems in the Menu
        fileMenu.getItems().addAll(openItem, saveItem, saveAsItem, autosaveItem, newTabItem);
        editMenu.getItems().addAll(undoItem, redoItem, copyItem, pasteItem, clearItem, canvasTransformMenu, selectionTransformMenu);
        surpriseMenu.getItems().addAll(surpriseItem);
        closeMenu.getItems().addAll(closeItem);
        settingsMenu.getItems().addAll(resizeItem, timerItem, notificationItem, helpItem);
        toolsMenu.getItems().addAll(lineItem, pencilItem, colorGrabberItem, eraserItem, textItem, selectItem, moveItem);
        webserverMenu.getItems().addAll(shareItem, stopShareItem);


        menuBar.getMenus().addAll(fileMenu, editMenu, surpriseMenu, webserverMenu, settingsMenu, closeMenu);

        //When you click on the menuItem, the method is called
        openItem.setOnAction(e -> openImageInNewTab(stage));
        saveItem.setOnAction(e -> { logCurrent("Save menu selected"); saveImage(stage, false); });
        saveAsItem.setOnAction(e -> { logCurrent("SaveAs menu selected"); saveImage(stage, true); });
        newTabItem.setOnAction(e -> addNewTab());
        surpriseItem.setOnAction(e -> displaySurprise());
        closeItem.setOnAction(e -> closeApplication(stage));
        helpItem.setOnAction(e -> displayHelp());
        resizeItem.setOnAction(e -> resizeCanvas());
        pencilItem.setOnAction(e -> currentTab().getDrawing().setTool("pencil"));
        lineItem.setOnAction(e -> currentTab().getDrawing().setTool("line"));
        colorGrabberItem.setOnAction(e -> currentTab().getDrawing().setTool("colorGrabber"));
        eraserItem.setOnAction(e -> currentTab().getDrawing().setTool("eraser"));
        textItem.setOnAction(e -> currentTab().getDrawing().setTool("text"));
        selectItem.setOnAction(e -> currentTab().getDrawing().setTool("select"));
        moveItem.setOnAction(e -> currentTab().getDrawing().setTool("move"));
        undoItem.setOnAction(e -> currentTab().getDrawing().undo());
        redoItem.setOnAction(e -> currentTab().getDrawing().redo());
        copyItem.setOnAction(e -> currentTab().getDrawing().copySelection());
        pasteItem.setOnAction(e -> currentTab().getDrawing().pasteSelection());
        clearItem.setOnAction(e -> currentTab().getDrawing().refresh());
        shareItem.setOnAction(e -> startImageServer());
        stopShareItem.setOnAction(e -> stopImageServer());

        // Autosave settings belong to the selected tab.
        autosaveItem.setOnAction(e -> {
            if (currentTab() != null) {
                currentTab().getAutoSave().setEnabled(autosaveItem.isSelected());
                logCurrent("Autosave " + (autosaveItem.isSelected() ? "enabled" : "disabled"));
                if (autosaveNotifications) showSystemNotification("GRPaint",
                    "Autosave " + (autosaveItem.isSelected() ? "enabled" : "disabled")
                    + " for " + currentTab().getLogName());
            }
        });
        timerItem.setOnAction(e -> {
            if (currentTab() != null) {
                currentTab().getAutoSave().setCountdownVisible(timerItem.isSelected());
                logCurrent("Autosave countdown " + (timerItem.isSelected() ? "shown" : "hidden"));
            }
        });
        notificationItem.setOnAction(e -> {
            autosaveNotifications = notificationItem.isSelected();
            logCurrent("Autosave notifications " + (autosaveNotifications ? "enabled" : "disabled"));
            showSystemNotification("GRPaint", "Autosave notifications " + (autosaveNotifications ? "enabled" : "disabled"));
        });

        //Autosave belongs to each tab, so each tab saves its own image.
        autoSave = currentTab().getAutoSave();
        updateAutoSaveMenuItems(autosaveItem, timerItem);
        settingsMenu.setOnShowing(e -> updateAutoSaveMenuItems(autosaveItem, timerItem));
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab instanceof PaintTab) {
                autoSave = ((PaintTab) newTab).getAutoSave();
            } else {
                autoSave = null;
            }
            updateAutoSaveMenuItems(autosaveItem, timerItem);
        });

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
        URL iconUrl = getClass().getResource("/GRPaintIcon.png");
        if (iconUrl != null) {
            Image appIcon = new Image(iconUrl.toExternalForm());
            stage.getIcons().add(appIcon);
        }
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> {
            if (!confirmCloseTabs()) event.consume();
        });
        stage.show();
        Logging.log("GRPaint", "Application started");
    }

    /**
     * Builds all rotation and flip actions for either the canvas or a selection.
     * @param title the menu heading
     * @param selectedOnly whether the actions use the current selection
     * @return the menu with five transformation options
     */
    private Menu createTransformMenu(String title, boolean selectedOnly) {
        Menu menu = new Menu(title);
        for (int angle : new int[]{90, 180, 270}) {
            MenuItem rotate = new MenuItem("Rotate " + angle + "° Clockwise");
            rotate.setOnAction(e -> transformCurrent(selectedOnly, angle, null));
            menu.getItems().add(rotate);
        }
        menu.getItems().add(new SeparatorMenuItem());
        MenuItem horizontal = new MenuItem("Flip Horizontal (Left/Right)");
        MenuItem vertical = new MenuItem("Flip Vertical (Top/Bottom)");
        horizontal.setOnAction(e -> transformCurrent(selectedOnly, null, true));
        vertical.setOnAction(e -> transformCurrent(selectedOnly, null, false));
        menu.getItems().addAll(horizontal, vertical);
        return menu;
    }

    /**
     * Changes the active image or selected piece and warns when no piece is selected.
     * @param selectedOnly true for selection actions
     * @param degrees the rotation angle, or null for a flip
     * @param horizontal true for horizontal flipping, false for vertical flipping
     */
    private void transformCurrent(boolean selectedOnly, Integer degrees, Boolean horizontal) {
        PaintTab tab = currentTab();
        if (tab == null) return;
        if (selectedOnly && !tab.getDrawing().hasSelection()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Selection");
            alert.setHeaderText("Select part of your image first");
            alert.setContentText("Choose the Select tool and drag a rectangle before transforming a selection.");
            alert.showAndWait();
            return;
        }
        if (degrees != null) tab.getDrawing().rotate(degrees, selectedOnly);
        else tab.getDrawing().flip(horizontal, selectedOnly);
    }

    /**
     * Writes an event with the active tab's filename to the log.
     * @param event a description of what happened
     */
    private void logCurrent(String event) {
        PaintTab tab = currentTab();
        Logging.log(tab == null ? "new tab" : tab.getLogName(), event);
    }

    /**
     * Private method that updates the Settings checks for the selected tab.
     * @param autosaveItem the menu item for automatic saving
     * @param timerItem the menu item for displaying the countdown
     */
    private void updateAutoSaveMenuItems(CheckMenuItem autosaveItem, CheckMenuItem timerItem) {
        PaintTab tab = currentTab();
        boolean hasTab = tab != null;
        autosaveItem.setDisable(!hasTab);
        timerItem.setDisable(!hasTab);
        autosaveItem.setSelected(hasTab && tab.getAutoSave().isEnabled());
        timerItem.setSelected(hasTab && tab.getAutoSave().isCountdownVisible());
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
        AutoSave tabAutoSave = new AutoSave(() -> saveImageForTab(primaryStage, tab, false, true));
        tab.setAutoSave(tabAutoSave);
        tabAutoSave.setManualSaveFunction(() -> saveImageForTab(primaryStage, tab, false, false));
        tabAutoSave.setNotificationFunction(() -> {
            Logging.log(tab.getLogName(), "Autosave skipped: file not saved yet");
            if (autosaveNotifications) showSystemNotification("GRPaint",
                "Save " + tab.getLogName() + " once before autosave can run.");
        });
        tabAutoSave.startAutoSave();
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
        autoSave = tabAutoSave;
        Logging.log(tab.getLogName(), "New tab created");
        tab.setOnCloseRequest(e -> {
            if (!tabAutoSave.confirmClose(primaryStage)) {
                e.consume();
            } else {
                tabAutoSave.stopAutoSave();
                Logging.log(tab.getLogName(), "Tab closed");
            }
        });
        tab.setOnClosed(e -> tabAutoSave.stopAutoSave());
    }

    /**
     * Opens a new tab only if the user chooses an image file.
     * @param stage the application window
     */
    private void openImageInNewTab(Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open Image File");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif")
        );
        File file = chooser.showOpenDialog(stage);
        if (file == null) return;
        addNewTab();
        openSelectedFile(file);
    }

    /**
     * Loads a chosen file into the current tab and logs the opened image.
     * @param selectedFile the image file to open
     */
    private void openSelectedFile(File selectedFile) {
        PaintTab tab = currentTab();
        Image image = new Image(selectedFile.toURI().toString());
        if (image.isError()) {
            new Alert(Alert.AlertType.ERROR, "Could not open image: " + selectedFile.getName()).showAndWait();
            return;
        }
        tab.setCurrentFile(selectedFile);
        tab.getAutoSave().setCurrentFile(selectedFile);
        tab.getDrawing().loadImage(image);
        tab.getAutoSave().markSaved();
        Logging.log(tab.getLogName(), "Opened " + selectedFile.getName() + " in " + tab.getText());
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
        openImageInNewTab(stage);
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
        saveImageForTab(stage, currentTab(), saveAs, false);
    }

    /**
     * Private method that saves the image belonging to one specific tab.
     * This keeps autosave from accidentally saving whichever tab is selected later.
     * @param stage primary JavaFX stage
     * @param tab the tab that should be saved
     * @param saveAs true to show the Save As dialog
     */
    private void saveImageForTab(Stage stage, PaintTab tab, boolean saveAs, boolean automatic) {
        if (tab == null) {
            showSystemNotification("GRPaint", "There is no tab selected to save.");
            return;
        }
        Canvas canvas = tab.getDrawing().getCanvas();
        File previousFile = tab.getCurrentFile();
        boolean choseNewFile = false;

        if (automatic && tab.getCurrentFile() == null) return;
        if (saveAs || tab.getCurrentFile() == null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Image As");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PNG Image", "*.png"),
                new FileChooser.ExtensionFilter("JPG Image", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("BMP Image", "*.bmp")
            );
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                tab.setCurrentFile(file);
                choseNewFile = true;
            } else {
                return;
            }
        }

        File currentFile = tab.getCurrentFile();
        String format = getImageFormat(currentFile);

        try {
            // Warn users about possible feature or quality loss when changing formats.
            String oldFormat = previousFile == null ? format : getImageFormat(previousFile);
            boolean changingFormat = previousFile != null && !oldFormat.equals(format);
            if ((saveAs && changingFormat) || (saveAs && (format.equals("jpg") || format.equals("bmp")))) {
                Alert warning = new Alert(Alert.AlertType.CONFIRMATION);
                warning.setTitle("Image Format Warning");
                warning.setHeaderText("Changing image formats can lose image data");
                String message = "Saving in a different format can affect transparency, quality, or metadata. "
                    + "Previously lost image details cannot be restored by changing formats.";
                if (format.equals("jpg")) {
                    message += "\nJPG uses lossy compression and does not support transparency.";
                } else if (format.equals("bmp")) {
                    message += "\nBMP files can be larger and may not preserve all image features.";
                }
                warning.setContentText(message + "\nDo you want to continue?");
                if (warning.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                    if (choseNewFile) {
                        tab.setCurrentFile(previousFile);
                        tab.getAutoSave().setCurrentFile(previousFile);
                    }
                    return;
                }
            }
            WritableImage snapshot = canvas.snapshot(null, null);
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(snapshot, null);
            if (format.equals("jpg")) {
                BufferedImage rgbImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
                java.awt.Graphics2D graphics = rgbImage.createGraphics();
                graphics.setColor(java.awt.Color.WHITE);
                graphics.fillRect(0, 0, rgbImage.getWidth(), rgbImage.getHeight());
                graphics.drawImage(bufferedImage, 0, 0, null);
                graphics.dispose();
                bufferedImage = rgbImage;
            }
            if (!ImageIO.write(bufferedImage, format, currentFile)) {
                throw new IOException("No image writer is available for " + format);
            }
            tab.getAutoSave().setCurrentFile(currentFile);
            tab.getAutoSave().markSaved();
            System.out.println("Saved successfully to: " + currentFile.getAbsolutePath());
            Logging.log(tab.getLogName(), (automatic ? "Autosave " : saveAs ? "SaveAs " : "Save ") + currentFile.getName());
            if (!automatic || autosaveNotifications) showSystemNotification("GRPaint",
                (automatic ? "Autosaved " : "Saved ") + currentFile.getName());
        } catch (IOException ex) {
            if (choseNewFile) {
                tab.setCurrentFile(previousFile);
                tab.getAutoSave().setCurrentFile(previousFile);
            }
            Alert error = new Alert(Alert.AlertType.ERROR, "Failed to save image: " + ex.getMessage());
            error.showAndWait();
            System.err.println("Failed to save image: " + ex.getMessage());
            Logging.log(tab.getLogName(), "Save failed: " + ex.getMessage());
        }
    }

    /**
     * Private method that gets the image format from the file name.
     * @param file the image file being saved
     * @return jpg, bmp, or png
     */
    private String getImageFormat(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") ? "jpg"
                : fileName.endsWith(".bmp") ? "bmp" : "png";
    }

    /**
     * Private method that asks each tab whether its unsaved changes should be saved.
     * Stops each autosave timer before closing the application.
     * @param stage the primary application window
     */
    private void closeApplication(Stage stage) {
        if (confirmCloseTabs()) {
            Logging.log("GRPaint", "Application closed from menu");
            Platform.exit();
        }
    }

    /**
     * Checks every tab before closing and stops its autosave timer.
     * @return true when the application can close
     */
    private boolean confirmCloseTabs() {
        for (javafx.scene.control.Tab item : tabPane.getTabs()) {
            PaintTab tab = (PaintTab) item;
            if (!tab.getAutoSave().confirmClose(primaryStage)) return false;
        }
        for (javafx.scene.control.Tab item : tabPane.getTabs()) {
            ((PaintTab) item).getAutoSave().stopAutoSave();
        }
        stopImageServer();
        return true;
    }

    /**
     * Private method that displays an operating system notification when supported.
     * Uses the system tray when it is available and falls back to a short console message.
     * @param title the notification title
     * @param message the notification text
     */
    private void showSystemNotification(String title, String message) {
        Thread task = new Thread(() -> {
            String os = System.getProperty("os.name", "").toLowerCase();
            if (os.contains("mac")) {
                try {
                    // Native macOS Notification Center alert (not a JavaFX popup).
                    String script = "display notification " + appleScriptQuote(message)
                            + " with title " + appleScriptQuote(title);
                    Process process = new ProcessBuilder("osascript", "-e", script).start();
                    if (process.waitFor() == 0) return;
                } catch (IOException ex) {
                    System.err.println("Could not launch macOS notification: " + ex.getMessage());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            try {
                if (SystemTray.isSupported()) {
                    synchronized (this) {
                        if (notificationTrayIcon == null) {
                            SystemTray tray = SystemTray.getSystemTray();
                            BufferedImage icon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
                            java.awt.Graphics2D graphics = icon.createGraphics();
                            graphics.setColor(new java.awt.Color(30, 120, 65));
                            graphics.fillOval(0, 0, 15, 15);
                            graphics.setColor(java.awt.Color.WHITE);
                            graphics.drawString("G", 3, 12);
                            graphics.dispose();
                            notificationTrayIcon = new TrayIcon(icon, "GRPaint");
                            notificationTrayIcon.setImageAutoSize(true);
                            tray.add(notificationTrayIcon);
                        }
                        notificationTrayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
                    }
                } else {
                    System.out.println("OS notifications unavailable: " + title + ": " + message);
                }
            } catch (Exception ex) {
                System.err.println("OS notification failed: " + ex.getMessage());
            }
        }, "GRPaint-Notifications");
        task.setDaemon(true);
        task.start();
    }

    /**
     * Quotes a message for Apple's notification script.
     * @param value the plain message text
     * @return a quoted AppleScript string
     */
    private String appleScriptQuote(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", " ").replace("\r", " ") + "\"";
    }

    /**
     * Private method that asks which tabs to display on the local web server.
     * Multiple tabs can be shared together. No selections will stop sharing.
     */
    private void startImageServer() {
        if (tabPane.getTabs().isEmpty()) {
            if (imageServer != null) stopImageServer();
            new Alert(Alert.AlertType.INFORMATION, "There are no image tabs to share.").showAndWait();
            return;
        }

        Alert selectDialog = new Alert(Alert.AlertType.CONFIRMATION);
        selectDialog.setTitle("Share Images");
        selectDialog.setHeaderText("Choose the tabs to share on the local web server:");
        VBox choices = new VBox(8);
        choices.setStyle("-fx-padding: 10px;");
        List<CheckBox> boxes = new ArrayList<>();
        for (javafx.scene.control.Tab item : tabPane.getTabs()) {
            CheckBox box = new CheckBox(item.getText());
            box.setSelected(item == currentTab());
            boxes.add(box);
            choices.getChildren().add(box);
        }
        selectDialog.getDialogPane().setContent(new ScrollPane(choices));
        if (selectDialog.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        Map<String, byte[]> images = new LinkedHashMap<>();
        try {
            for (int i = 0; i < boxes.size(); i++) {
                if (!boxes.get(i).isSelected()) continue;
                PaintTab tab = (PaintTab) tabPane.getTabs().get(i);
                WritableImage snapshot = tab.getDrawing().getCanvas().snapshot(null, null);
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(snapshot, null);
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                if (!ImageIO.write(bufferedImage, "png", output)) {
                    throw new IOException("PNG writer is unavailable.");
                }
                images.put((i + 1) + ". " + tab.getText(), output.toByteArray());
            }

            if (images.isEmpty()) {
                if (imageServer != null) stopImageServer();
                new Alert(Alert.AlertType.INFORMATION,
                    "No images were selected, so image sharing is offline.").showAndWait();
                return;
            }

            if (imageServer == null) imageServer = new ImageServer();
            imageServer.start(images, 8080);
            Logging.log("GRPaint", "Web server started with " + images.size() + " images");
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Image Server Started");
            alert.setHeaderText("Your selected tabs are available locally");
            alert.setContentText("Opened http://127.0.0.1:8080/ in the browser of your choice. "
                + "The server shares snapshots taken when you started sharing.");
            alert.showAndWait();
        } catch (IOException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                "Could not start the image server: " + ex.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Private method that stops the local image server if it is running.
     */
    private void stopImageServer() {
        if (imageServer != null) {
            imageServer.stop();
            imageServer = null;
            Logging.log("GRPaint", "Web server stopped");
            showSystemNotification("GRPaint", "Image server stopped.");
        }
    }

    /**
     * Public method that stops autosave and the image server when GRPaint exits.
     * This also cleans up if the window is closed using the window close button.
     */
    @Override
    public void stop() {
        if (tabPane != null) {
            for (javafx.scene.control.Tab item : tabPane.getTabs()) {
                ((PaintTab) item).getAutoSave().stopAutoSave();
            }
        }
        if (imageServer != null) {
            imageServer.stop();
            imageServer = null;
        }
        if (notificationTrayIcon != null && SystemTray.isSupported()) {
            SystemTray.getSystemTray().remove(notificationTrayIcon);
        }
        Logging.log("GRPaint", "Application stopped");
        Logging.close();
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
            currentTab().getAutoSave().markSaved();
            currentTab().setCurrentFile(null);
            currentTab().getAutoSave().markUnsaved();
            logCurrent("Surprise image loaded");
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
            "Edit:\n" +
            "Undo / Redo - Step backward or forward through your changes.\n" +
            "Copy / Paste - Copies the selected piece. Paste drops it in the top left, drag it where you want.\n" +
            "Clear Canvas - Wipes the canvas (asks first).\n\n" +
            "Tools:\n" +
            "Line - Click twice to draw a line.\n" +
            "Pencil - Allows you to free flow your canvas.\n" +
            "Text - Click on the canvas and type the text to add.\n" +
            "Select - Drag a box around part of the image.\n" +
            "Move Selection - Drag the selected piece to move it.\n\n" +
            "Shapes:\n" +
            "Drag to draw. Triangle is a regular triangle, Right Triangle has a 90 degree corner.\n" +
            "Polygon asks how many sides you want (3 or more).\n\n" +
            "Keyboard Shortcuts:\n" +
            "Ctrl (Command on Mac) + O - Open\n" +
            "Ctrl + S - Save\n" +
            "Ctrl + Shift + S - Save As\n" +
            "Ctrl + T - New Tab\n" +
            "Ctrl + Q - Close\n" +
            "Crtl + Shift + Alt + R - Suprise\n" +
            "Ctrl + R - Resize\n" +
            "Ctrl + Z - Undo\n" +
            "Ctrl + Y - Redo\n" +
            "Ctrl + C - Copy\n" +
            "Ctrl + V - Paste\n" +
            "Ctrl + Shift + N - Clear Canvas\n" +
            "Transforms: Edit > Transform Whole Canvas / Transform Selection.\n" +
            "Select a piece before rotating or flipping just that piece.\n" +
            "Resize: Insert any height or width you want as long as it isn't 0 or below.\n" +
            "Auto Save: Saved tabs automatically save changed images every 30 seconds.\n" +
            "Settings: Enable Autosave and Show Autosave Countdown apply to the selected tab.\n" +
            "Web Server: Choose one or more tabs to share a snapshot at http://127.0.0.1:8080/.\n\n"
        );


        // Put the help text inside a ScrollPane
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(helpText);

        // Allow the ScrollPane to resize the Label horizontally
        scrollPane.setFitToWidth(true);

        // Create the scene
        Scene scene = new Scene(scrollPane, 500, 500);

        helpStage.setScene(scene);
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
                    if (width <= 0 || height <= 0) {
                        new Alert(Alert.AlertType.ERROR, "Canvas dimensions must be greater than zero.").showAndWait();
                        return;
                    }
                    currentTab().setDrawing(new Drawing(width, height));
                    currentTab().getAutoSave().markUnsaved();
                    logCurrent("Canvas resized to " + width + " x " + height);
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