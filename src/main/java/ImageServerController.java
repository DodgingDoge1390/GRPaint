
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;

/**
 * Manages the local image web server in GRPaint.
 * Allows users to select and share images from multiple tabs.
 */
public final class ImageServerController {
    private final TabManager tabs;
    private final NotificationService notifications;
    private ImageServer server;

    /**
     * Initializes the image server controller.
     * @param tabs the manager containing image tabs
     * @param notifications the system notification service
     */
    public ImageServerController(TabManager tabs, NotificationService notifications) {
        this.tabs = tabs;
        this.notifications = notifications;
    }

    /**
     * Displays a dialog for selecting images to share.
     * Starts the local web server with snapshots of selected tabs.
     */
    public void start() {
        if (tabs.getTabPane().getTabs().isEmpty()) {
            if (server != null) stop();
            new Alert(Alert.AlertType.INFORMATION, "There are no image tabs to share.").showAndWait();
            return;
        }

        Alert selectDialog = new Alert(Alert.AlertType.CONFIRMATION);
        selectDialog.setTitle("Share Images");
        selectDialog.setHeaderText("Choose the tabs to share on the local web server:");

        VBox choices = new VBox(8);
        choices.setStyle("-fx-padding: 10px;");

        List<CheckBox> boxes = new ArrayList<>();

        for (Tab item : tabs.getTabPane().getTabs()) {
            CheckBox box = new CheckBox(item.getText());
            box.setSelected(item == tabs.currentTab());
            boxes.add(box);
            choices.getChildren().add(box);
        }

        selectDialog.getDialogPane().setContent(new ScrollPane(choices));

        if (selectDialog.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        Map<String, byte[]> images = new LinkedHashMap<>();

        try {
            for (int i = 0; i < boxes.size(); i++) {
                if (!boxes.get(i).isSelected()) continue;

                PaintTab tab = (PaintTab) tabs.getTabPane().getTabs().get(i);
                WritableImage snapshot = tab.getDrawing().getCanvas().snapshot(null, null);
                BufferedImage buffered = SwingFXUtils.fromFXImage(snapshot, null);
                ByteArrayOutputStream output = new ByteArrayOutputStream();

                if (!ImageIO.write(buffered, "png", output)) {
                    throw new IOException("PNG writer is unavailable.");
                }

                images.put((i + 1) + ". " + tab.getText(), output.toByteArray());
            }

            if (images.isEmpty()) {
                if (server != null) stop();

                new Alert(Alert.AlertType.INFORMATION,
                        "No images were selected, so image sharing is offline.").showAndWait();
                return;
            }

            if (server == null) server = new ImageServer();

            server.start(images, 8080);
            Logging.log("GRPaint", "Web server started with " + images.size() + " images");

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Image Server Started");
            alert.setHeaderText("Your selected tabs are available locally");
            alert.setContentText("Opened http://127.0.0.1:8080/ in the browser of your choice. "
                + "The server shares snapshots taken when you started sharing.");
            alert.showAndWait();

        } catch (IOException ex) {
            new Alert(Alert.AlertType.ERROR,
                    "Could not start the image server: " + ex.getMessage()).showAndWait();
        }
    }

    /**
     * Stops the active image web server.
     * Logs the action and displays a system notification.
     */
    public void stop() {
        if (server == null) return;

        server.stop();
        server = null;
        Logging.log("GRPaint", "Web server stopped");
        notifications.show("GRPaint", "Image server stopped.");
    }

    /**
     * Shuts down the web server when GRPaint closes.
     * Does not display a notification during shutdown.
     */
    public void close() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }
}
