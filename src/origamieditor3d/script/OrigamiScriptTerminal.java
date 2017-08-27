package origamieditor3d.script;

import java.util.List;

public interface OrigamiScriptTerminal {

	public enum AccessMode {
		SCRIPT, USER, DEBUG
	}
	
	List<String> getHistory();

	int getPaperColor();

	java.awt.image.BufferedImage getPaperTexture();

	void clearHistory();

	Integer getVersion();

	AccessMode getAccessMode();

	void execute(String code) throws Exception;

	void executeWithTimeout(String code) throws Exception;

	void execute(String code, AccessMode access) throws Exception;

	void executeWithTimeout(String code, AccessMode access) throws Exception;

}