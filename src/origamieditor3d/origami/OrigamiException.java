package origamieditor3d.origami;

/**
 * @author Attila Bágyoni (ba-sz-at@users.sourceforge.net)
 */
public class OrigamiException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public OrigamiException(String ex) {
        super(ex);
    }
    final static public OrigamiException H001 = new OrigamiException("H001: Unexpected concave polygon");
    final static public OrigamiException H002 = new OrigamiException("H002: Unable to save");
    final static public OrigamiException H003 = new OrigamiException("H003: Undefined control sequence");
    final static public OrigamiException H004 = new OrigamiException("H004: Unknown file version");
    final static public OrigamiException H005 = new OrigamiException("H005: Failed to read the file");
    final static public OrigamiException H006 = new OrigamiException("H006: No such command");
    final static public OrigamiException H007 = new OrigamiException("H007: Invalid number of arguments");
    final static public OrigamiException H008 = new OrigamiException("H008: The selected 3 points are collinear");
    final static public OrigamiException H009 = new OrigamiException("H009: No such paper size");
    final static public OrigamiException H010 = new OrigamiException("H010: Some parameters are missing");
    final static public OrigamiException H011 = new OrigamiException("H011: Access denied");
    final static public OrigamiException H012 = new OrigamiException("H012: Bisecting zero angle is not allowed");
    final static public OrigamiException H013 = new OrigamiException("H013: Cannot read image with alpha channel");
}
