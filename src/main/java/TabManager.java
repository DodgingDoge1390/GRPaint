
import java.net.URL;
import java.util.function.Supplier;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Manages the tabs used by the GRPaint application.
 * Handles tab creation, selection, autosave functionality,
 * closing tabs, event logging, and displaying the surprise image.
 *
 * Each tab contains its own drawing canvas and autosave timer,
 * allowing users to work on multiple images independently.
 */
public final class TabManager {

    /** The TabPane that contains all open drawing tabs. */
    private final TabPane tabPane;

    /** The primary JavaFX application window. */
    private final Stage stage;

    /** Service responsible for displaying operating system notifications. */
    private final NotificationService notifications;

    /** Supplies the current notification preference. */
    private final Supplier<Boolean> showNotifications;

    /** Handles opening and saving images associated with tabs. */
    private ImageFileManager imageFiles;

    /** Counter used to generate unique names for new tabs. */
    private int tabCounter = 1;

    /**
     * Constructs a TabManager to control the application's drawing tabs.
     *
     * @param tabPane the TabPane containing the drawing tabs
     * @param stage the primary JavaFX application window
     * @param notifications the service used for system notifications
     * @param showNotifications supplier that determines whether notifications are enabled
     */
    public TabManager(TabPane tabPane, Stage stage, NotificationService notifications,
                      Supplier<Boolean> showNotifications) {
        this.tabPane = tabPane;
        this.stage = stage;
        this.notifications = notifications;
        this.showNotifications = showNotifications;
    }

    /**
     * Connects the image file manager after the application
     * components have been initialized.
     *
     * @param imageFiles the ImageFileManager responsible for opening and saving images
     */
    public void setImageFiles(ImageFileManager imageFiles) {
        this.imageFiles = imageFiles;
    }

    /**
     * Returns the TabPane containing all open drawing tabs.
     *
     * @return the application's TabPane
     */
    public TabPane getTabPane() {
        return tabPane;
    }

    /**
     * Retrieves the currently selected drawing tab.
     * Returns null when no drawing tab is selected.
     *
     * @return the active PaintTab, or null if no tab is selected
     */
    public PaintTab currentTab() {
        Tab selected = tabPane.getSelectionModel().getSelectedItem();
        return selected instanceof PaintTab ? (PaintTab) selected : null;
    }

    /**
     * Creates a new blank drawing tab with an 800 by 600 canvas.
     * Each new tab receives its own AutoSave instance, which
     * manages automatic saving and unsaved-change notifications.
     * The method also configures event logging and a close
     * confirmation to prevent accidental loss of unsaved work.
     *
     * @throws IllegalStateException if the ImageFileManager has not been connected
     */
    public void addNewTab() {
        if (imageFiles == null) {
            throw new IllegalStateException("ImageFileManager is not connected");
        }

        PaintTab tab = new PaintTab("Untitled " + tabCounter++, 800, 600);

        AutoSave autosave = new AutoSave(
            () -> imageFiles.saveImageForTab(tab, false, true)
        );

        tab.setAutoSave(autosave);

        autosave.setManualSaveFunction(
            () -> imageFiles.saveImageForTab(tab, false, false)
        );

        autosave.setNotificationFunction(() -> {
            Logging.log(
                tab.getLogName(),
                "Autosave skipped: file not saved yet"
            );

            if (showNotifications.get()) {
                notifications.show(
                    "GRPaint",
                    "Save " + tab.getLogName() +
                    " once before autosave can run."
                );
            }
        });

        autosave.startAutoSave();

        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);

        Logging.log(tab.getLogName(), "New tab created");

        tab.setOnCloseRequest(e -> {
            if (!autosave.confirmClose(stage)) {
                e.consume();
            } else {
                autosave.stopAutoSave();
                Logging.log(tab.getLogName(), "Tab closed");
            }
        });

        tab.setOnClosed(e -> autosave.stopAutoSave());
    }

    /**
     * Records an event associated with the currently selected tab.
     * If no tab is selected, the event is recorded under "new tab".
     *
     * @param event the description of the user action being logged
     */
    public void logCurrent(String event) {
        PaintTab tab = currentTab();

        Logging.log(
            tab == null ? "new tab" : tab.getLogName(),
            event
        );
    }

    /**
     * Checks all drawing tabs for unsaved changes before the
     * application is allowed to close.
     * If any tab cancels the closing operation, the application
     * remains open. Otherwise, all autosave timers are stopped.
     *
     * @return true if all tabs can close, or false if closing was canceled
     */
    public boolean confirmCloseTabs() {
        for (Tab item : tabPane.getTabs()) {
            if (!((PaintTab) item).getAutoSave().confirmClose(stage)) {
                return false;
            }
        }

        stopAutoSaves();
        return true;
    }

    /**
     * Stops the autosave timer associated with every open tab.
     * This prevents autosave tasks from continuing after the
     * JavaFX application has closed.
     */
    public void stopAutoSaves() {
        for (Tab item : tabPane.getTabs()) {
            ((PaintTab) item).getAutoSave().stopAutoSave();
        }
    }

    /**
     * Opens the bundled Capybara surprise image in a new drawing tab.
     */
    public void displaySurprise() {
        URL imageURL = TabManager.class.getResource("/Capybara.jpg");

        if (imageURL == null) {
            System.out.println("Capybara.jpg not found!");
            return;
        }

        addNewTab();
        PaintTab tab = currentTab();

        tab.getDrawing().loadImage(
            new Image(imageURL.toExternalForm())
        );
        tab.getAutoSave().markSaved();
        tab.setCurrentFile(null);
        tab.getAutoSave().markUnsaved();

        logCurrent("Surprise image loaded");
    }
}
