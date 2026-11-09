import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Public class that tests autosave settings and the unsaved changes flag.
 */
public class AutoSaveTest {

    /**
     * Checks whether editing marks a document as unsaved.
     */
    @Test
    public void testUnsavedChanges() {
        AutoSave autoSave = new AutoSave(() -> {});
        assertFalse(autoSave.hasUnsavedChanges());
        autoSave.markUnsaved();
        assertTrue(autoSave.hasUnsavedChanges());
        autoSave.markSaved();
        assertFalse(autoSave.hasUnsavedChanges());
    }

    /**
     * Checks whether the optional countdown setting can be turned off.
     */
    @Test
    public void testCountdownSetting() {
        AutoSave autoSave = new AutoSave(() -> {});
        assertFalse(autoSave.isCountdownVisible());
        autoSave.setCountdownVisible(false);
        assertFalse(autoSave.isCountdownVisible());
    }
}
