package origamieditor3d.resources;

import java.util.ResourceBundle;
import java.util.Locale;

/**
 *
 * @author Attila Bágyoni (ba-sz-at@users.sourceforge.net)
 */
public class Dictionary {

    static private ResourceBundle messages;

    static {
        setLocale(new Locale(System.getProperty("user.language"), System.getProperty("user.country")));
    }

    static public String getString(String key, Object... obj) {
        try {
            return String.format(messages.getString(key), obj);
        } catch (Exception ex) {
            return key;
        }
    }

    static public void setLocale(Locale locale) {
        if (new Dictionary().getClass().getResource("/language" + "_" + locale.getLanguage() + ".properties") != null) {
            messages = ResourceBundle.getBundle("language", locale);
            System.out.println("User language set to " + locale.getDisplayName(java.util.Locale.ENGLISH));
        } else {
            messages = ResourceBundle.getBundle("language", new Locale("en", "US"));
            System.out.println("Could not set user language to " + locale.getDisplayName(java.util.Locale.ENGLISH));
        }
    }
}
