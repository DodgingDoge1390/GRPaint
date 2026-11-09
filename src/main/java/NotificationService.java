
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Displays native OS notifications for GRPaint.
 * Manages notifications and the system tray icon.
 */
public class NotificationService {

    /** System tray icon used for displaying notifications. */
    private TrayIcon notificationTrayIcon;

    /**
     * Creates the notification service.
     */
    public NotificationService() { }

    /**
     * Displays an operating system notification on a separate thread.
     * @param title the notification title
     * @param message the notification message
     */
    public void show(String title, String message) {
        Thread task = new Thread(() -> {
            String os = System.getProperty("os.name", "").toLowerCase();

            if (os.contains("mac")) {
                try {
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

                            BufferedImage icon = new BufferedImage(
                                16, 16, BufferedImage.TYPE_INT_ARGB
                            );

                            java.awt.Graphics2D graphics = icon.createGraphics();
                            graphics.setColor(new java.awt.Color(30, 120, 65));
                            graphics.fillOval(0, 0, 15, 15);
                            graphics.setColor(java.awt.Color.WHITE);
                            graphics.drawString("G", 3, 12);
                            graphics.dispose();

                            notificationTrayIcon = new TrayIcon(icon, "GRPaint");
                            notificationTrayIcon.setImageAutoSize(true);
                            tray.add(notificationTrayIcon);

                            try {
                                Thread.sleep(350);
                            } catch (InterruptedException interrupted) {
                                Thread.currentThread().interrupt();
                                return;
                            }
                        }

                        System.out.println(
                            "Sending system tray notification: " + title + " - " + message
                        );

                        notificationTrayIcon.displayMessage(
                            title, message, TrayIcon.MessageType.INFO
                        );
                    }
                } else {
                    System.out.println(
                        "OS notifications unavailable: " + title + ": " + message
                    );
                }

            } catch (Exception ex) {
                System.err.println("OS notification failed: " + ex.getMessage());
            }

        }, "GRPaint-Notifications");

        task.setDaemon(true);
        task.start();
    }

    /**
     * Escapes special characters for macOS AppleScript notifications.
     * @param value the original notification text
     * @return the properly escaped AppleScript string
     */
    private String appleScriptQuote(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", " ").replace("\r", " ") + "\"";
    }

    /**
     * Removes the system tray icon when GRPaint closes.
     * Prevents the notification icon from remaining in the tray.
     */
    public void close() {
        if (notificationTrayIcon != null && SystemTray.isSupported()) {
            SystemTray.getSystemTray().remove(notificationTrayIcon);
            notificationTrayIcon = null;
        }
    }
}
