
import javafx.scene.control.CheckMenuItem;

/**
 * Keeps the Settings menu synchronized with the active tab.
 * Updates autosave and countdown menu options.
 */
public final class SettingsMenuHelper {

    /** Prevents creating instances of this utility class. */
    private SettingsMenuHelper() { }

    /**
     * Updates autosave and countdown settings for the selected tab.
     * @param tab the currently selected drawing tab
     * @param autosaveItem the autosave menu checkbox
     * @param timerItem the countdown visibility checkbox
     */
    public static void updateAutoSaveMenuItems(PaintTab tab, CheckMenuItem autosaveItem, CheckMenuItem timerItem) {
        boolean hasTab = tab != null;

        autosaveItem.setDisable(!hasTab);
        timerItem.setDisable(!hasTab);

        autosaveItem.setSelected(hasTab && tab.getAutoSave().isEnabled());
        timerItem.setSelected(hasTab && tab.getAutoSave().isCountdownVisible());
    }
}
