import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * Public class that shares selected tab images through a small local web server.
 */
public class ImageServer {
    private HttpServer server;

    /**
     * Public method that starts serving one PNG image on the local computer.
     * @param pngBytes the image saved as PNG bytes
     * @param port the port used by the web server
     * @throws IOException if the server cannot start
     */
    public void start(byte[] pngBytes, int port) throws IOException {
        Map<String, byte[]> images = new LinkedHashMap<>();
        images.put("Current Tab", pngBytes);
        start(images, port);
    }

    /**
     * Public method that shares one or more selected images on a local web page.
     * Automatically opens the page in the default web browser when possible.
     * @param images the tab names and their saved PNG image bytes
     * @param port the port used by the web server
     * @throws IOException if the server cannot start
     */
    public void start(Map<String, byte[]> images, int port) throws IOException {
        if (images == null || images.isEmpty()) {
            stop();
            throw new IllegalArgumentException("Select at least one image to share.");
        }
        stop();
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);

        StringBuilder html = new StringBuilder("""
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <title>GRPaint Image Server</title>
        </head>
        <body style="font-family: sans-serif; padding: 20px;">
            <h1 style="color: green;">GRPaint Image Server</h1>
            <p>Images selected in GRPaint are shown below.</p>
        """);

        int index = 0;
        for (Map.Entry<String, byte[]> item : images.entrySet()) {
            String imagePath = "/image/" + index + ".png";
            byte[] pngBytes = item.getValue();
            server.createContext(imagePath, exchange -> sendImage(exchange, pngBytes));
            html.append("<figure style=\"margin: 22px 0;\">")
                .append("<figcaption><strong>")
                .append(escapeHtml(item.getKey()))
                .append("</strong></figcaption>")
                .append("<a href=\"").append(imagePath).append("\">")
                .append("<img style=\"max-width: 100%; border: 1px solid #bbb;\" src=\"")
                .append(imagePath).append("\" alt=\"GRPaint drawing\">")
                .append("</a></figure>");
            index++;
        }
        html.append("</body></html>");
        byte[] pageBytes = html.toString().getBytes(StandardCharsets.UTF_8);
        server.createContext("/", exchange -> sendPage(exchange, pageBytes));
        server.start();

        if (Desktop.isDesktopSupported()
                && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(URI.create("http://127.0.0.1:" + port + "/"));
            } catch (IOException e) {
                System.err.println("Could not open the browser: " + e.getMessage());
            }
        }
    }

    /**
     * Private method that escapes tab titles before displaying them on the page.
     * @param title the tab name
     * @return the title without unsafe HTML characters
     */
    private String escapeHtml(String title) {
        return title.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /**
     * Private method that sends the list of shared images to a browser.
     * @param exchange the browser request
     * @param pageBytes the web page to send
     * @throws IOException if the web page cannot be sent
     */
    private void sendPage(HttpExchange exchange, byte[] pageBytes) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(200, pageBytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(pageBytes);
        }
    }

    /**
     * Private method that sends a selected PNG image to a browser.
     * @param exchange the browser request
     * @param pngBytes the PNG image to send
     * @throws IOException if the image cannot be sent
     */
    private void sendImage(HttpExchange exchange, byte[] pngBytes) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "image/png");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(200, pngBytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(pngBytes);
        }
    }

    /**
     * Public method that stops the web server if it is running.
     */
    public void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    /**
     * Public method that checks whether the server is running.
     * @return true if the server is running
     */
    public boolean isRunning() {
        return server != null;
    }
}
