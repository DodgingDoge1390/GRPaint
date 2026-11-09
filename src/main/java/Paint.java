//I like my Java how I like my coffee, at home and brewed

import java.net.URL;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Initializes the GRPaint application.
 */
public class Paint extends Application {
    private TabPane tabPane;
    private TabManager tabs;
    private ImageFileManager imageFiles;
    private ImageServerController imageSharing;
    private volatile boolean autosaveNotifications = true;
    private final NotificationService notificationService = new NotificationService();

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
 * Override to the overthrow the main function
 * @param stage primary JavaFX stage
 * @see javafx.application.Application#start(javafx.stage.Stage)
 */
    @Override
    public void start(Stage stage) {
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        tabs = new TabManager(tabPane, stage, notificationService, () -> autosaveNotifications);
        imageFiles = new ImageFileManager(stage, tabs, notificationService, () -> autosaveNotifications);
        tabs.setImageFiles(imageFiles);
        imageSharing = new ImageServerController(tabs, notificationService);
        tabs.addNewTab();

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
        MenuItem testNotificationItem = new MenuItem("Test OS Notification");
        notificationItem.setSelected(true);
        Menu canvasTransformMenu = TransformMenu.create("Transform Whole Canvas", false, tabs::currentTab);
        Menu selectionTransformMenu = TransformMenu.create("Transform Selection", true, tabs::currentTab);

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
        settingsMenu.getItems().addAll(resizeItem, timerItem, notificationItem, testNotificationItem, helpItem);
        toolsMenu.getItems().addAll(lineItem, pencilItem, colorGrabberItem, eraserItem, textItem, selectItem, moveItem);
        webserverMenu.getItems().addAll(shareItem, stopShareItem);

        //Getting rid of toolsmenu and keeping it in the code just so it doesn't break
        menuBar.getMenus().addAll(fileMenu, editMenu, surpriseMenu, webserverMenu, settingsMenu, closeMenu);

        //When you click on the menuItem, the method is called
        openItem.setOnAction(e -> imageFiles.openImageInNewTab());
        saveItem.setOnAction(e -> { tabs.logCurrent("Save menu selected"); imageFiles.saveImage(false); });
        saveAsItem.setOnAction(e -> { tabs.logCurrent("SaveAs menu selected"); imageFiles.saveImage(true); });
        newTabItem.setOnAction(e -> tabs.addNewTab());
        surpriseItem.setOnAction(e -> tabs.displaySurprise());
        closeItem.setOnAction(e -> closeApplication());
        helpItem.setOnAction(e -> HelpWindow.show());
        resizeItem.setOnAction(e -> CanvasResizeDialog.show(tabs.currentTab()));
        pencilItem.setOnAction(e -> tabs.currentTab().getDrawing().setTool("pencil"));
        lineItem.setOnAction(e -> tabs.currentTab().getDrawing().setTool("line"));
        colorGrabberItem.setOnAction(e -> tabs.currentTab().getDrawing().setTool("colorGrabber"));
        eraserItem.setOnAction(e -> tabs.currentTab().getDrawing().setTool("eraser"));
        textItem.setOnAction(e -> tabs.currentTab().getDrawing().setTool("text"));
        selectItem.setOnAction(e -> tabs.currentTab().getDrawing().setTool("select"));
        moveItem.setOnAction(e -> tabs.currentTab().getDrawing().setTool("move"));
        undoItem.setOnAction(e -> tabs.currentTab().getDrawing().undo());
        redoItem.setOnAction(e -> tabs.currentTab().getDrawing().redo());
        copyItem.setOnAction(e -> tabs.currentTab().getDrawing().copySelection());
        pasteItem.setOnAction(e -> tabs.currentTab().getDrawing().pasteSelection());
        clearItem.setOnAction(e -> tabs.currentTab().getDrawing().refresh());
        shareItem.setOnAction(e -> imageSharing.start());
        stopShareItem.setOnAction(e -> imageSharing.stop());

        // Autosave settings belong to the selected tab.
        autosaveItem.setOnAction(e -> {
            if (tabs.currentTab() != null) {
                tabs.currentTab().getAutoSave().setEnabled(autosaveItem.isSelected());
                tabs.logCurrent("Autosave " + (autosaveItem.isSelected() ? "enabled" : "disabled"));
                if (autosaveNotifications) notificationService.show("GRPaint",
                    "Autosave " + (autosaveItem.isSelected() ? "enabled" : "disabled")
                    + " for " + tabs.currentTab().getLogName());
            }
        });
        timerItem.setOnAction(e -> {
            if (tabs.currentTab() != null) {
                tabs.currentTab().getAutoSave().setCountdownVisible(timerItem.isSelected());
                tabs.logCurrent("Autosave countdown " + (timerItem.isSelected() ? "shown" : "hidden"));
            }
        });
        testNotificationItem.setOnAction(e -> notificationService.show("GRPaint", "Test notification from GRPaint"));
        notificationItem.setOnAction(e -> {
            autosaveNotifications = notificationItem.isSelected();
            tabs.logCurrent("Autosave notifications " + (autosaveNotifications ? "enabled" : "disabled"));
            notificationService.show("GRPaint", "Autosave notifications " + (autosaveNotifications ? "enabled" : "disabled"));
        });

        //Autosave belongs to each tab, so each tab saves its own image.
        SettingsMenuHelper.updateAutoSaveMenuItems(tabs.currentTab(), autosaveItem, timerItem);
        settingsMenu.setOnShowing(e -> SettingsMenuHelper.updateAutoSaveMenuItems(tabs.currentTab(), autosaveItem, timerItem));
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) ->
            SettingsMenuHelper.updateAutoSaveMenuItems(tabs.currentTab(), autosaveItem, timerItem));

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
        BorderPane pane = new BorderPane();
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
            if (!tabs.confirmCloseTabs()) event.consume();
            else imageSharing.close();
        });
        stage.show();
        Logging.log("GRPaint", "Application started");
    }

    /** Checks for unsaved work before closing the app from the menu. */
    private void closeApplication() {
        if (tabs.confirmCloseTabs()) {
            imageSharing.close();
            Logging.log("GRPaint", "Application closed from menu");
            Platform.exit();
        }
    }

    /**
     * Public method that stops autosave and the image server when GRPaint exits.
     * This also cleans up if the window is closed using the window close button.
     */
    @Override
    public void stop() {
        if (tabs != null) tabs.stopAutoSaves();
        if (imageSharing != null) imageSharing.close();
        notificationService.close();
        Logging.log("GRPaint", "Application stopped");
        Logging.close();
    }
}
