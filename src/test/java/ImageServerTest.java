import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import org.junit.jupiter.api.Test;

/**
 * Public class that tests the selection rules for image sharing.
 */
public class ImageServerTest {
    /**
     * Checks that the server cannot share an empty image selection.
     */
    @Test
    public void testNoImagesSelected() {
        ImageServer server = new ImageServer();
        assertThrows(IllegalArgumentException.class,
            () -> server.start(Collections.emptyMap(), 8080));
        assertFalse(server.isRunning());
    }
}
