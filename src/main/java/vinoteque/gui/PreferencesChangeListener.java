package vinoteque.gui;

import java.util.Properties;

/**
 * Listener for preferences change event
 * @author gushakov
 */
public interface PreferencesChangeListener {
    public Properties getPreferences();
    public void preferencesChanged(Properties preferences);
}
