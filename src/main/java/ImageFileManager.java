
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Handles opening, saving, and exporting images in GRPaint.
 * Supports multiple formats, autosave, and file warnings.
 */
public final class ImageFileManager {
    private final Stage stage;
    private final TabManager tabs;
    private final NotificationService notifications;
    private final Supplier<Boolean> showAutosaveNotifications;

    /**
     * Initializes the image file manager.
     * @param stage the main application window
     * @param tabs the tab manager
     * @param notifications the notification service
     * @param showAutosaveNotifications the autosave notification setting
     */
    public ImageFileManager(Stage stage, TabManager tabs, NotificationService notifications,
                            Supplier<Boolean> showAutosaveNotifications) {
        this.stage = stage;
        this.tabs = tabs;
        this.notifications = notifications;
        this.showAutosaveNotifications = showAutosaveNotifications;
    }

    /**
     * Opens a file chooser to select an image.
     * Loads the selected image into a new tab.
     */
    public void openImageInNewTab() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open Image File");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif"));
        File file = chooser.showOpenDialog(stage);
        if (file == null) return;
        tabs.addNewTab();
        openSelectedFile(file);
    }

    /**
     * Loads an image into the currently selected tab.
     * @param file the image file to open
     */
    public void openSelectedFile(File file) {
        PaintTab tab = tabs.currentTab();
        if (tab == null) return;
        Image image = new Image(file.toURI().toString());
        if (image.isError()) {
            new Alert(Alert.AlertType.ERROR, "Could not open image: " + file.getName()).showAndWait();
            return;
        }
        tab.setCurrentFile(file);
        tab.getAutoSave().setCurrentFile(file);
        tab.getDrawing().loadImage(image);
        tab.getAutoSave().markSaved();
        Logging.log(tab.getLogName(), "Opened " + file.getName() + " in " + tab.getText());
    }

    /**
     * Saves the currently selected image tab.
     * @param saveAs true to open the Save As dialog
     */
    public void saveImage(boolean saveAs) {
        saveImageForTab(tabs.currentTab(), saveAs, false);
    }

    /**
     * Saves an image and handles format warnings and autosave.
     * @param tab the image tab to save
     * @param saveAs true to select a new file location
     * @param automatic true if triggered by autosave
     */
    public void saveImageForTab(PaintTab tab, boolean saveAs, boolean automatic) {
        if (tab == null) {
            notifications.show("GRPaint", "There is no tab selected to save.");
            return;
        }
        if (automatic && tab.getCurrentFile() == null) return;
        Canvas canvas = tab.getDrawing().getCanvas();
        File previousFile = tab.getCurrentFile();
        boolean choseNewFile = false;
        if (saveAs || previousFile == null) {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Save Image As");
            chooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("PNG Image", "*.png"),
                    new FileChooser.ExtensionFilter("JPG Image", "*.jpg", "*.jpeg"),
                    new FileChooser.ExtensionFilter("BMP Image", "*.bmp"));
            File file = chooser.showSaveDialog(stage);
            if (file == null) return;
            tab.setCurrentFile(file);
            choseNewFile = true;
        }
        File currentFile = tab.getCurrentFile();
        String format = getImageFormat(currentFile);
        try {
            String oldFormat = previousFile == null ? format : getImageFormat(previousFile);
            boolean changingFormat = previousFile != null && !oldFormat.equals(format);
            if ((saveAs && changingFormat) || (saveAs && (format.equals("jpg") || format.equals("bmp")))) {
                Alert warning = new Alert(Alert.AlertType.CONFIRMATION);
                warning.setTitle("Image Format Warning");
                warning.setHeaderText("Changing image formats can lose image data");
                String message = "Saving in a different format can affect transparency, quality, or metadata. "
                    + "Previously lost image details cannot be restored by changing formats.";
                if (format.equals("jpg")) message += "\nJPG uses lossy compression and does not support transparency.";
                else if (format.equals("bmp")) message += "\nBMP files can be larger and may not preserve all image features.";
                warning.setContentText(message + "\nDo you want to continue?");
                if (warning.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                    if (choseNewFile) restoreFile(tab, previousFile);
                    return;
                }
            }
            WritableImage snapshot = canvas.snapshot(null, null);
            BufferedImage buffered = SwingFXUtils.fromFXImage(snapshot, null);
            if (format.equals("jpg")) {
                BufferedImage rgb = new BufferedImage(buffered.getWidth(), buffered.getHeight(), BufferedImage.TYPE_INT_RGB);
                java.awt.Graphics2D graphics = rgb.createGraphics();
                try {
                    graphics.setColor(java.awt.Color.WHITE);
                    graphics.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
                    graphics.drawImage(buffered, 0, 0, null);
                } finally {
                    graphics.dispose();
                }
                buffered = rgb;
            }
            if (!ImageIO.write(buffered, format, currentFile)) {
                throw new IOException("No image writer is available for " + format);
            }
            tab.getAutoSave().setCurrentFile(currentFile);
            tab.getAutoSave().markSaved();
            System.out.println("Saved successfully to: " + currentFile.getAbsolutePath());
            Logging.log(tab.getLogName(), (automatic ? "Autosave " : saveAs ? "SaveAs " : "Save ")
                    + currentFile.getName());
            if (!automatic || showAutosaveNotifications.get()) {
                notifications.show("GRPaint", (automatic ? "Autosaved " : "Saved ") + currentFile.getName());
            }
        } catch (IOException | RuntimeException ex) {
            if (choseNewFile) restoreFile(tab, previousFile);
            new Alert(Alert.AlertType.ERROR, "Failed to save image: " + ex.getMessage()).showAndWait();
            System.err.println("Failed to save image: " + ex.getMessage());
            Logging.log(tab.getLogName(), "Save failed: " + ex.getMessage());
        }
    }

    /**
     * Restores the previous file when saving is canceled.
     * @param tab the tab whose file is restored
     * @param file the previous image file
     */
    private static void restoreFile(PaintTab tab, File file) {
        tab.setCurrentFile(file);
        tab.getAutoSave().setCurrentFile(file);
    }

    /**
     * Determines the image format from its filename.
     * @param file the image file to check
     * @return jpg, bmp, or png
     */
    private static String getImageFormat(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") ? "jpg"
                : name.endsWith(".bmp") ? "bmp" : "png";
    }
}
