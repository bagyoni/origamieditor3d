package origamieditor3d.resources;

/**
 *
 * @author Attila Bágyoni (ba-sz-at@users.sourceforge.net)
 */
public class Instructor {
    
    static private java.util.ResourceBundle messages;
    
    static {
        setLocale(new java.util.Locale(System.getProperty("user.language"), System.getProperty("user.country")));
    }
    
    static public String getString(String key, Object... obj) {
        try {
            return String.format(messages.getString(key), obj);
        } catch (Exception ex) {
            return key;
        }
    }
    
    static public void setLocale(java.util.Locale locale) {
        if (new Instructor().getClass().getResource("/diagram" + "_" + locale.getLanguage() + ".properties") != null) {
            messages = java.util.ResourceBundle.getBundle("diagram", locale);
            System.out.println("Diagramming language set to " + locale.getDisplayName(java.util.Locale.ENGLISH));
        } else {
            messages = java.util.ResourceBundle.getBundle("diagram", new java.util.Locale("en", "US"));
            System.out.println("Could not set diagramming language to " + locale.getDisplayName(java.util.Locale.ENGLISH));
        }
    }
}
